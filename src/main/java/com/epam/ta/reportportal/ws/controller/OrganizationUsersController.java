/*
 * Copyright 2024 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ORGANIZATION_MANAGER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ORGANIZATION_MEMBER;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_FULL_NAME;
import static com.epam.ta.reportportal.util.SecurityContextUtils.getPrincipal;
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.api.OrganizationUsersApi;
import com.epam.reportportal.api.model.OrgUserAssignment;
import com.epam.reportportal.api.model.OrgUserProjectPage;
import com.epam.reportportal.api.model.OrgUserUpdateRequest;
import com.epam.reportportal.api.model.OrganizationUsersPage;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UserAssignmentResponse;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.organization.OrganizationUsersHandler;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.organization.OrganizationUserFilter;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ControllerUtils;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationUsersController extends BaseController implements OrganizationUsersApi {

  private final OrganizationUsersHandler organizationUsersHandler;

  @Autowired
  public OrganizationUsersController(OrganizationUsersHandler organizationUsersHandler) {
    this.organizationUsersHandler = organizationUsersHandler;
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<OrganizationUsersPage> getOrganizationsOrgIdUsers(Long orgId,
      Integer offset, Integer limit, String order, String sort, String fullName) {
    Filter filter = new Filter(OrganizationUserFilter.class, new ArrayList<>());
    filter.withCondition(
        new FilterCondition(Condition.EQUALS, false, orgId.toString(), "organization_id"));
    if (StringUtils.isNotEmpty(fullName)) {
      filter.withCondition(
          new FilterCondition(Condition.CONTAINS, false, fullName, CRITERIA_FULL_NAME));
    }

    // sort by name only for now
    var pageable = ControllerUtils.getPageable(CRITERIA_FULL_NAME, order, offset, limit);

    return ResponseEntity
        .ok()
        .body(organizationUsersHandler.getOrganizationUsers(filter, pageable));

  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<UserAssignmentResponse> postOrganizationsOrgIdUsers(
      @PathVariable("org_id") Long orgId, OrgUserAssignment request) {
    return ResponseEntity
        .status(OK)
        .body(organizationUsersHandler.assignUser(orgId, request));
  }


  @Override
  @PreAuthorize(ORGANIZATION_MEMBER)
  public ResponseEntity<Void> deleteOrganizationsOrgIdUsersUserId(Long orgId, Long userId) {
    organizationUsersHandler.unassignUser(orgId, userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @PreAuthorize(ORGANIZATION_MEMBER)
  public ResponseEntity<OrgUserProjectPage> getOrgUserProjects(Long orgId, Long userId,
      Integer offset, Integer limit, String order, String sort) {
    ReportPortalUser principal = getPrincipal();
    BusinessRule.expect(
        principal.getUserRole().equals(UserRole.ADMINISTRATOR) || principal.getUserId()
            .equals(userId) || (principal.getOrganizationDetails().containsKey(orgId.toString())
            && OrganizationRole.MANAGER.equals(
            principal.getOrganizationDetails().get(orgId.toString()).getOrgRole())),
        Predicates.equalTo(true)).verify(ErrorType.ACCESS_DENIED);

    var pageable = ControllerUtils.getPageable(sort, order, offset, limit);

    OrgUserProjectPage userProjectsInOrganization = organizationUsersHandler.findUserProjectsInOrganization(
        userId, orgId, pageable);
    return ResponseEntity
        .status(OK)
        .body(userProjectsInOrganization);
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<SuccessfulUpdate> putOrganizationsOrgIdUsersUserId(Long orgId, Long userId,
      OrgUserUpdateRequest orgUserUpdateRequest) {
    organizationUsersHandler.updateOrganizationUserDetails(orgId, userId, orgUserUpdateRequest);
    return ResponseEntity.ok(new SuccessfulUpdate());
  }
}
