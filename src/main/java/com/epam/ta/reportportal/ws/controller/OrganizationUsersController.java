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
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_FULL_NAME;
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.api.OrganizationUserApi;
import com.epam.reportportal.api.model.Order;
import com.epam.reportportal.api.model.OrgUserAssignment;
import com.epam.reportportal.api.model.OrganizationUsersPage;
import com.epam.reportportal.api.model.UserAssignmentResponse;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.organization.OrganizationUsersHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.entity.organization.OrganizationUserFilter;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationUsersController extends BaseController implements OrganizationUserApi {

  private final OrganizationRepositoryCustom organizationRepositoryCustom;

  private final OrganizationUsersHandler organizationUsersHandler;

  @Autowired
  public OrganizationUsersController(OrganizationRepositoryCustom organizationRepositoryCustom,
      OrganizationUsersHandler organizationUsersHandler) {
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.organizationUsersHandler = organizationUsersHandler;
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Transactional(readOnly = true)
  public ResponseEntity<OrganizationUsersPage> getOrganizationsOrgIdUsers(Long orgId,
      Integer offset, Integer limit, Order order, String sort, String fullName) {
    organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    Filter filter = new Filter(OrganizationUserFilter.class, Lists.newArrayList());
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
  @Transactional
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<UserAssignmentResponse> postOrganizationsOrgIdUsers(
      @PathVariable("org_id") Long orgId, OrgUserAssignment request) {
    var user = getLoggedUser();
    return ResponseEntity
        .status(OK)
        .body(organizationUsersHandler.assignUser(orgId, request, user));
  }
}
