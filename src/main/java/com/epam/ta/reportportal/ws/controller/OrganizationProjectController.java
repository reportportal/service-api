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
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.project.OrganizationProjectHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.model.OrganizationProjectsList;
import com.epam.ta.reportportal.model.Problem;
import com.epam.ta.reportportal.model.ProjectProfile;
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

@RestController
@Validated
public class OrganizationProjectController {

  private final OrganizationProjectHandler organizationProjectHandler;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;


  @Autowired
  public OrganizationProjectController(OrganizationProjectHandler organizationProjectHandler,
      OrganizationRepositoryCustom organizationRepositoryCustom) {
    this.organizationProjectHandler = organizationProjectHandler;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
  }

  // TODO: get rid of annotations here after using them from already generated interface
  //  OrganizationsApi
  // need to handle @AuthenticationPrincipal properly before overriding interface methods

  /**
   * Retrieves a list of projects associated with a specific organization. For now only sorting by
   * name is available.
   *
   * @param orgId  The ID of the organization.
   * @param limit  The maximum number of projects to return.
   * @param offset The number of projects to skip before starting to collect the result set.
   * @param sort   The property to sort the result set by.
   * @param order  The order in which to sort the result set. Can be 'asc' for ascending order or
   *               'desc' for descending order.
   * @return A ResponseEntity containing a list of projects associated with the specified
   * organization.
   */
  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Operation(summary = "Get organization projects", description = "Get a list of organization projects.  ### Authority:  - `ADMINISTRATOR` - `MANAGER` - `MEMBER`  ### Access level  - `ADMINISTRATOR` - no restrictions. - `MANAGER` - no restrictions. - `MEMBER` - restricted to viewing only their assigned projects.", security = {
      @SecurityRequirement(name = "BearerAuth")}, tags = {"Projects", "Ready for implementation"})
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationProjectsList.class))),

      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),

      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class)))})
  @RequestMapping(value = "/organizations/{org_id}/projects",
      produces = {"application/json", "application/problem+json"},
      method = RequestMethod.GET)
  public ResponseEntity<OrganizationProjectsList> getOrganizationsOrgIdProjects(
      @Parameter(in = ParameterIn.PATH, description = "Organization identifier", required = true, schema = @Schema()) @PathVariable("org_id") Long orgId,
      @Parameter(in = ParameterIn.QUERY, description = "The limit used for this page of results. This will be the same as the limit query parameter unless it exceeded the maximum value allowed for this API endpoint", schema = @Schema(defaultValue = "300")) @Valid @RequestParam(value = "limit", required = false, defaultValue = "300") Integer limit,
      @Parameter(in = ParameterIn.QUERY, description = "The offset used for this page of results", schema = @Schema(defaultValue = "0")) @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
      @Parameter(in = ParameterIn.QUERY, description = "Indicate sorting direction", schema = @Schema(allowableValues = {"ASC", "DESC"}, defaultValue = "ASC")) @Valid @RequestParam(value = "order", required = false, defaultValue = "ASC") String order,
      @Parameter(in = ParameterIn.QUERY, description = "Filter projects by name", schema = @Schema()) @Valid @RequestParam(value = "name", required = false) String name,
      @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") @Parameter(in = ParameterIn.QUERY, description = "Filter projects by slug", schema = @Schema()) @Valid @RequestParam(value = "slug", required = false) String slug,
      @Parameter(in = ParameterIn.QUERY, description = "Indicate sort by field", schema = @Schema(defaultValue = "name")) @Valid @RequestParam(value = "sort", required = false, defaultValue = "name") String sort,
      @AuthenticationPrincipal ReportPortalUser user
  ) {
    organizationRepositoryCustom.findById(orgId).orElseThrow(() -> new ReportPortalException(
        ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var pageable = ControllerUtils.getPageable(sort, order, offset, limit);
    Filter filter = new Filter(ProjectProfile.class, Lists.newArrayList())
        .withCondition(
            new FilterCondition(Condition.EQUALS, false, orgId.toString(), "organization_id"));
    if (StringUtils.isNotEmpty(name)) {
      filter.withCondition(
          new FilterCondition(Condition.CONTAINS, false, name, "name"));
    }
    if (StringUtils.isNotEmpty(slug)) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, slug, "slug"));
    }

    return ResponseEntity.status(OK)
        .body(
            organizationProjectHandler.getOrganizationProjectsList(user, orgId, filter, pageable));
  }

}