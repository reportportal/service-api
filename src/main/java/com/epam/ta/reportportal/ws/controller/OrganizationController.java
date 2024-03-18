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

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.USER;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationInfo;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.organization.OrganizationInfoResource;
import com.epam.ta.reportportal.model.organization.OrganizationResource;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Andrei Piankouski
 */
@RestController
@RequestMapping("/v1")
@Tag(name = "organizations-controller", description = "Organizations Controller")
public class OrganizationController {

  private final GetOrganizationHandler getOrganizationHandler;

  @Autowired
  public OrganizationController(GetOrganizationHandler getOrganizationHandler) {
    this.getOrganizationHandler = getOrganizationHandler;
  }

  @Transactional
  @GetMapping("/organizations/{organizationId}")
  @Operation(summary = "Get information about organization")
  public OrganizationResource getOrganization(@PathVariable Long organizationId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getOrganizationHandler.getResource(organizationId, user);
  }

  @Transactional
  @GetMapping("/organizations")
  @Operation(summary = "Get list of organizations associated with the user")
  public Iterable<OrganizationResource> getAllOrganizations(
      @AuthenticationPrincipal ReportPortalUser user,
      @FilterFor(Organization.class) Filter filter,
      @FilterFor(Organization.class) Queryable predefinedFilter,
      @SortFor(Organization.class) Pageable pageable) {

    modifyFilterWithUserCriteria(filter, user);

    return getOrganizationHandler.getOrganizations(
        new CompositeFilter(Operator.AND, filter, predefinedFilter),
        pageable);
  }


  @Transactional
  @GetMapping("/organizations-info")
  @Operation(summary = "Get list of organizations aggregated info associated with the user")
  public Iterable<OrganizationInfoResource> getOrganizationsInfo(
      @AuthenticationPrincipal ReportPortalUser user,
      @FilterFor(OrganizationInfo.class) Filter filter,
      @FilterFor(OrganizationInfo.class) Queryable predefinedFilter,
      @SortFor(OrganizationInfo.class) Pageable pageable) {

    modifyFilterWithUserCriteria(filter, user);

    return getOrganizationHandler.getOrganizationsInfo(
        new CompositeFilter(Operator.AND, filter, predefinedFilter),
        pageable);
  }

  /**
   * By security reasons "filter.*.user" should always be replaced with filter by current user.
   * Only Admin users can retrieve all organizations regardless organization membership
   */
  private void modifyFilterWithUserCriteria(Filter filter, ReportPortalUser user) {
    // always remove user filter
    filter.getFilterConditions()
        .removeIf(cc -> cc.getAllConditions().stream()
            .anyMatch(fc -> fc.getSearchCriteria().equalsIgnoreCase(USER)));

    if (UserRole.ADMINISTRATOR != user.getUserRole()) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, user.getUsername(), USER));
    }
  }
}
