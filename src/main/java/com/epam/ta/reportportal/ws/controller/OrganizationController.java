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

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ORGANIZATION_MANAGER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ORGANIZATION_MEMBER;

import com.epam.reportportal.api.OrganizationApi;
import com.epam.reportportal.api.model.OrganizationBase;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.filter.OrganizationsSearchCriteriaService;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.core.organization.OrganizationExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.entity.organization.OrganizationFilter;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controller for managing organizations in ReportPortal. Provides endpoints for creating, updating, retrieving, and
 * deleting organizations.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@RestController
public class OrganizationController extends BaseController implements OrganizationApi {

  private final GetOrganizationHandler getOrganizationHandler;
  private final OrganizationsSearchCriteriaService searchCriteriaService;
  private final Pf4jPluginBox pluginBox;

  /**
   * Constructs a new OrganizationController with the specified dependencies.
   *
   * @param getOrganizationHandler The handler for retrieving organization information.
   * @param searchCriteriaService  The service for handling search criteria related to organizations.
   * @param pluginBox              The plugin box for accessing organization-related extensions.
   */
  @Autowired
  public OrganizationController(
      GetOrganizationHandler getOrganizationHandler,
      OrganizationsSearchCriteriaService searchCriteriaService, Pf4jPluginBox pluginBox
  ) {
    this.getOrganizationHandler = getOrganizationHandler;
    this.searchCriteriaService = searchCriteriaService;
    this.pluginBox = pluginBox;
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Override
  public ResponseEntity<OrganizationInfo> getOrganizationsOrgId(Long orgId) {
    return ResponseEntity.ok().body(getOrganizationHandler.getOrganizationById(orgId));
  }

  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<OrganizationPage> getOrganizations(
      Integer offset,
      Integer limit,
      String order,
      String name,
      String slug,
      String sort
  ) {
    Filter filter = new Filter(OrganizationFilter.class, Lists.newArrayList());

    if (StringUtils.isNotEmpty(name)) {
      filter.withCondition(new FilterCondition(Condition.CONTAINS, false, name, "name"));
    }
    if (StringUtils.isNotEmpty(slug)) {
      filter.withCondition(new FilterCondition(Condition.EQUALS, false, slug, "slug"));
    }

    var pageable = ControllerUtils.getPageable(sort, order, offset, limit);

    return ResponseEntity.ok().body(getOrganizationHandler.getOrganizations(filter, pageable));
  }


  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<OrganizationPage> postOrganizationsSearches(SearchCriteriaRQ searchCriteria) {
    Filter filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteria, OrganizationFilter.class);

    var pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(searchCriteria.getSort()) ? searchCriteria.getSort() : "name",
        searchCriteria.getOrder().toString(),
        searchCriteria.getOffset(),
        searchCriteria.getLimit());

    return ResponseEntity.ok().body(getOrganizationHandler.getOrganizations(filter, pageable));
  }

  @Override
  @PreAuthorize(IS_ADMIN)
  @Transactional
  public ResponseEntity<OrganizationInfo> postOrganizations(OrganizationBase createRequest) {
    var org = getOrgExtension().createOrganization(createRequest);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{orgId}")
        .buildAndExpand(org.getId())
        .toUri();
    return ResponseEntity.created(location).body(org);
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> putOrganizationsOrgId(Long orgId, OrganizationBase updateRequest) {
    getOrgExtension().updateOrganization(orgId, updateRequest);
    return ResponseEntity.ok(new SuccessfulUpdate("The update was completed successfully."));
  }

  @Override
  @PreAuthorize(IS_ADMIN)
  @Transactional
  public ResponseEntity<Void> deleteOrganizationsOrgId(Long orgId) {
    getOrgExtension().deleteOrganization(orgId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private OrganizationExtensionPoint getOrgExtension() {
    return pluginBox.getInstance(OrganizationExtensionPoint.class)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED));
  }

}
