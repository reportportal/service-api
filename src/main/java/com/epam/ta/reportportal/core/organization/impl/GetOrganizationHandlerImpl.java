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

package com.epam.ta.reportportal.core.organization.impl;

import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;

import com.epam.reportportal.api.model.OrganizationProfile;
import com.epam.reportportal.api.model.OrganizationProfilesPage;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.entity.organization.OrganizationFilter;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


/**
 * @author Andrei Piankouski
 */
@Service
public class GetOrganizationHandlerImpl implements GetOrganizationHandler {

  private final OrganizationRepositoryCustom organizationRepositoryCustom;

  @Autowired
  public GetOrganizationHandlerImpl(OrganizationRepositoryCustom organizationRepositoryCustom) {
    this.organizationRepositoryCustom = organizationRepositoryCustom;
  }

  @Override
  public OrganizationProfile getOrganizationById(Long organizationId, ReportPortalUser user) {
    Filter filter = new Filter(OrganizationFilter.class, Lists.newArrayList());
    filter.withCondition(
        new FilterCondition(Condition.EQUALS, false, organizationId.toString(), "id"));
    return organizationRepositoryCustom.findByFilter(filter).stream().findFirst()
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, organizationId));
  }

  @Override
  public OrganizationProfilesPage getOrganizations(Queryable filter, Pageable pageable) {
    var organizationProfilesPage = organizationRepositoryCustom.findByFilter(filter, pageable);

    OrganizationProfilesPage organizationProfilesList =
        new OrganizationProfilesPage()
            .items(organizationProfilesPage.getContent());

    return responseWithPageParameters(organizationProfilesList, pageable,
        organizationProfilesPage.getTotalElements());
  }

}
