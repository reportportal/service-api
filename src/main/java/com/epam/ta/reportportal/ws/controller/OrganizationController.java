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
import static org.springframework.data.domain.Sort.Direction.ASC;

import com.epam.reportportal.api.OrganizationApi;
import com.epam.reportportal.api.model.OrganizationBase;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.reportportal.api.model.OrganizationSettings;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.filter.SearchCriteriaService;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.core.organization.OrganizationExtensionPoint;
import com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsHandler;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.entity.organization.OrganizationFilter;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
@RequiredArgsConstructor
public class OrganizationController extends BaseController implements OrganizationApi {

  private final GetOrganizationHandler getOrganizationHandler;
  private final SearchCriteriaService searchCriteriaService;
  private final Pf4jPluginBox pluginBox;
  private final OrganizationSettingsHandler organizationSettingsHandler;

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
  public ResponseEntity<OrganizationPage> postOrganizationsSearches(String accept, SearchCriteriaRQ criteriaRq) {
    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(criteriaRq, OrganizationFilter.class);

    var pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(criteriaRq.getSort()) ? criteriaRq.getSort() : "name",
        criteriaRq.getOrder() != null ? criteriaRq.getOrder().toString() : ASC.toString(),
        criteriaRq.getOffset(),
        criteriaRq.getLimit());

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

  @Override
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Transactional(readOnly = true)
  public ResponseEntity<OrganizationSettings> getOrgSettingsByOrgId(Long orgId) {
    return ResponseEntity.ok(organizationSettingsHandler.getOrganizationSettings(orgId));
  }

  private OrganizationExtensionPoint getOrgExtension() {
    return pluginBox.getInstance(OrganizationExtensionPoint.class)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.PAYMENT_REQUIRED,
            "Organization management is not available. Please install the 'organization' plugin."
        ));
  }

}
