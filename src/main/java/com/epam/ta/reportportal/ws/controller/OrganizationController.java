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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.model.OrganizationResource;
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
@RequestMapping("/v1/organization")
@Tag(name = "organizations-controller", description = "Organizations Controller")
public class OrganizationController {

  private final GetOrganizationHandler getOrganizationHandler;

  @Autowired
  public OrganizationController(GetOrganizationHandler getOrganizationHandler) {
    this.getOrganizationHandler = getOrganizationHandler;
  }

  @Transactional
  @GetMapping("/{organizationId}")
  @Operation(summary = "Get information about organization")
  public OrganizationResource getOrganization(@PathVariable Long organizationId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getOrganizationHandler.getResource(organizationId, user);
  }

  @Transactional
  @GetMapping("/list")
  @Operation(summary = "Get list of all organizations")
  public Iterable<OrganizationResource> getAllOrganizations(
      @AuthenticationPrincipal ReportPortalUser user,
      @FilterFor(Organization.class) Filter filter,
      @FilterFor(Organization.class) Queryable predefinedFilter,
      @SortFor(Organization.class) Pageable pageable
  ) {
    return getOrganizationHandler.getOrganizations(
        new CompositeFilter(Operator.AND, filter, predefinedFilter),
        pageable);
  }
}
