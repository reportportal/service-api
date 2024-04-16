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

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.model.organization.OrganizationInfoResource;
import com.epam.ta.reportportal.model.organization.OrganizationResource;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.OrganizationConverter;
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
  public OrganizationResource getResource(Long organizationId, ReportPortalUser user) {
    Organization organization = organizationRepositoryCustom.findById(organizationId)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, organizationId));
    return OrganizationConverter.TO_ORGANIZATION_RESOURCE.apply(organization);
  }


  @Override
  public Iterable<OrganizationResource> getOrganizations(Queryable filter, Pageable pageable) {
    return PagedResourcesAssembler.pageConverter(OrganizationConverter.TO_ORGANIZATION_RESOURCE)
        .apply(organizationRepositoryCustom.findByFilter(filter, pageable));
  }

  @Override
  public Iterable<OrganizationInfoResource> getOrganizationsInfo(Queryable filter,
      Pageable pageable) {
    return PagedResourcesAssembler
        .pageConverter(OrganizationConverter.TO_ORGANIZATION_INFO_RESOURCE)
        .apply(organizationRepositoryCustom.findOrganizationInfoByFilter(filter, pageable));
  }

}
