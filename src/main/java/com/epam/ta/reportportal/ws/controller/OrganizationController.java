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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ORGANIZATION_MEMBER;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.USER;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.entity.organization.OrganizationFilter;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.OrganizationProfile;
import com.epam.ta.reportportal.model.OrganizationProfilesList;
import com.epam.ta.reportportal.model.Problem;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Andrei Piankouski
 */
@RestController
@Tag(name = "organizations-controller", description = "Organizations Controller")
@Validated
public class OrganizationController {

  private final GetOrganizationHandler getOrganizationHandler;

  @Autowired
  public OrganizationController(GetOrganizationHandler getOrganizationHandler) {
    this.getOrganizationHandler = getOrganizationHandler;
  }

  @Transactional
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Operation(summary = "Get organization information", description = "Provide organization information.  ### Authority:  - `ADMINISTRATOR` - `MANAGER` - `MEMBER`", security = {
      @SecurityRequirement(name = "BearerAuth")}, tags = {"Organizations", "Ready for implementation"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.epam.ta.reportportal.api.model.OrganizationProfile.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = com.epam.ta.reportportal.api.model.Problem.class))),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = com.epam.ta.reportportal.api.model.Problem.class)))})
  @RequestMapping(value = "/organizations/{org_id}",
      produces = {"application/json", "application/problem+json"},
      method = RequestMethod.GET)
  public ResponseEntity<OrganizationProfile> getOrganizationsOrgId(
      @Parameter(in = ParameterIn.PATH, description = "Organization identifier", required = true, schema = @Schema()) @PathVariable("org_id") Long orgId,
      @AuthenticationPrincipal ReportPortalUser user
  ) {
    return ResponseEntity.ok()
        .body(getOrganizationHandler.getOrganizationById(orgId, user));
  }


  @Transactional
  @Operation(summary = "Get a list of organizations", description = "Get a list of existing organizations.  ### Authority  - `ADMINISTRATOR` - `MANAGER` - `MEMBER`  ### Access level  - `ADMINISTRATOR` - no restrictons. - `MANAGER` - limited to viewing only their assigned organizations. - `MEMBER` - limited access:   - can view only their assigned organizations.   - restricted access to organization profile relationships.", security = {
      @SecurityRequirement(name = "BearerAuth")}, tags = {"Organizations", "Ready for implementation"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationProfilesList.class))),
      @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Problem.class)))})
  @RequestMapping(value = "/organizations",
      produces = {"application/json"},
      method = RequestMethod.GET)
  public ResponseEntity<OrganizationProfilesList> getOrganizations(
      @Parameter(in = ParameterIn.QUERY, description = "The offset used for this page of results", schema = @Schema(defaultValue = "0")) @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
      @Parameter(in = ParameterIn.QUERY, description = "The limit used for this page of results. This will be the same as the limit query parameter unless it exceeded the maximum value allowed for this API endpoint", schema = @Schema(defaultValue = "10")) @Valid @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
      @Parameter(in = ParameterIn.QUERY, description = "Indicate sort by field", schema = @Schema()) @Valid @RequestParam(value = "sort", required = false) String sort,
      @Parameter(in = ParameterIn.QUERY, description = "Indicate sorting direction", schema = @Schema(allowableValues = {"ASC", "DESC"})) @Valid @RequestParam(value = "order", required = false) String order,
      @Parameter(in = ParameterIn.QUERY, description = "Filter organizations by name", schema = @Schema()) @Valid @RequestParam(value = "name", required = false) String name,
      @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") @Parameter(in = ParameterIn.QUERY, description = "Filter organizations by slug", schema = @Schema()) @Valid @RequestParam(value = "slug", required = false) String slug,
      @AuthenticationPrincipal ReportPortalUser user
  ) {

    Filter filter = new Filter(OrganizationFilter.class, Lists.newArrayList());

    if (UserRole.ADMINISTRATOR != user.getUserRole()) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, user.getUsername(), USER));
    }
    if (StringUtils.isNotEmpty(name)) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, name, "name"));
    }
    if (StringUtils.isNotEmpty(slug)) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, name, "slug"));
    }

    if (StringUtils.isEmpty(sort)) {
      sort = "name";
    }

    var pageable = ControllerUtils.getPageable(sort, order, offset, limit);

    return ResponseEntity
        .ok()
        .body(getOrganizationHandler.getOrganizations(filter, pageable));
  }

}
