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
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.api.OrganizationProjectApi;
import com.epam.reportportal.api.model.Order;
import com.epam.reportportal.api.model.OrganizationProjectsPage;
import com.epam.reportportal.api.model.ProjectBase;
import com.epam.reportportal.api.model.ProjectInfo;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.filter.OrganizationsSearchCriteriaService;
import com.epam.ta.reportportal.core.project.OrganizationProjectHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.entity.project.ProjectProfile;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling organization project-related requests. * Extends the BaseController
 * and implements the ProjectsApi interface. * Provides endpoints for retrieving and managing
 * organization projects. Controller implementation for working with external systems.
 *
 * @author Siarhei Hrabko
 */
@RestController
public class OrganizationProjectController extends BaseController implements
    OrganizationProjectApi {

  private final OrganizationProjectHandler organizationProjectHandler;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final OrganizationsSearchCriteriaService searchCriteriaService;

  /**
   * Constructor for OrganizationProjectController.
   *
   * @param organizationProjectHandler   the handler for organization project-related operations
   * @param organizationRepositoryCustom the custom repository for organization-related database
   *                                     operations
   * @param searchCriteriaService        the service for creating filters based on search criteria
   */
  @Autowired
  public OrganizationProjectController(
      OrganizationProjectHandler organizationProjectHandler,
      OrganizationRepositoryCustom organizationRepositoryCustom,
      OrganizationsSearchCriteriaService searchCriteriaService) {
    this.organizationProjectHandler = organizationProjectHandler;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.searchCriteriaService = searchCriteriaService;
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Override
  public ResponseEntity<OrganizationProjectsPage> getOrganizationsOrgIdProjects(Long orgId,
      Integer offset, Integer limit, Order order, String name, String slug, String sort
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
    var user = getLoggedUser();
    return ResponseEntity.status(OK)
        .body(
            organizationProjectHandler.getOrganizationProjectsPage(user, orgId, filter, pageable));
  }

  @Override
  @Transactional
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<ProjectInfo> postOrganizationsOrgIdProjects(Long orgId,
      ProjectBase projectBase
  ) {
    var user = getLoggedUser();

    return ResponseEntity
        .status(OK)
        .body(organizationProjectHandler.createProject(orgId, projectBase, user));
  }

  @Override
  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  public ResponseEntity<OrganizationProjectsPage> postOrganizationsOrgIdProjectsSearches(
      Long orgId,
      SearchCriteriaRQ searchCriteria) {

    organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    Filter filter = searchCriteriaService
        .createFilterBySearchCriteria(searchCriteria, ProjectProfile.class)
        .withCondition(
            new FilterCondition(Condition.EQUALS, false, orgId.toString(), "organization_id"));

    var user = getLoggedUser();

    var pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(searchCriteria.getSort()) ? searchCriteria.getSort() : "name",
        Order.fromValue(searchCriteria.getOrder().toString()),
        searchCriteria.getOffset(),
        searchCriteria.getLimit());

    return ResponseEntity
        .ok()
        .body(
            organizationProjectHandler.getOrganizationProjectsPage(user, orgId, filter, pageable));
  }


  @Override
  @Transactional
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<Void> deleteOrganizationsOrgIdProjectsProjectId(Long orgId, Long prjId) {
    var user = getLoggedUser();
    organizationProjectHandler.deleteProject(user, orgId, prjId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
