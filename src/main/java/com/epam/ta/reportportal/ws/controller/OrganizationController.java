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

import com.epam.reportportal.api.OrganizationApi;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.filter.OrganizationsSearchCriteriaService;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.entity.organization.OrganizationFilter;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationController extends BaseController implements OrganizationApi {

  private final GetOrganizationHandler getOrganizationHandler;
  private final OrganizationsSearchCriteriaService searchCriteriaService;


  @Autowired
  public OrganizationController(GetOrganizationHandler getOrganizationHandler,
      OrganizationsSearchCriteriaService searchCriteriaService) {
    this.getOrganizationHandler = getOrganizationHandler;
    this.searchCriteriaService = searchCriteriaService;
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Override
  public ResponseEntity<OrganizationInfo> getOrganizationsOrgId(Long orgId) {
    return ResponseEntity.ok()
        .body(getOrganizationHandler.getOrganizationById(orgId));
  }


  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<OrganizationPage> getOrganizations(Integer offset, Integer limit,
      String order, String name, String slug, String sort) {
    Filter filter = new Filter(OrganizationFilter.class, Lists.newArrayList());

    if (StringUtils.isNotEmpty(name)) {
      filter.withCondition(
          new FilterCondition(Condition.CONTAINS, false, name, "name"));
    }
    if (StringUtils.isNotEmpty(slug)) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, slug, "slug"));
    }

    var pageable = ControllerUtils.getPageable(sort, order, offset, limit);

    return ResponseEntity
        .ok()
        .body(getOrganizationHandler.getOrganizations(filter, pageable));
  }


  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<OrganizationPage> postOrganizationsSearches(
      SearchCriteriaRQ searchCriteria) {
    Filter filter = searchCriteriaService
        .createFilterBySearchCriteria(searchCriteria, OrganizationFilter.class);

    var pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(searchCriteria.getSort()) ? searchCriteria.getSort() : "name",
        searchCriteria.getOrder().toString(),
        searchCriteria.getOffset(),
        searchCriteria.getLimit());

    return ResponseEntity
        .ok()
        .body(getOrganizationHandler.getOrganizations(filter, pageable));

  }

}
