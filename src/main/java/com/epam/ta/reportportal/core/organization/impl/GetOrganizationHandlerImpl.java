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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationRepository;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.model.OrganizationResource;
import com.epam.ta.reportportal.ws.converter.converters.OrganizationConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Andrei Piankouski
 */
@Service
public class GetOrganizationHandlerImpl implements GetOrganizationHandler {

  private final OrganizationRepository organizationRepository;

  @Autowired
  public GetOrganizationHandlerImpl(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Override
  public OrganizationResource getResource(Long organizationId, ReportPortalUser user) {
    Organization organization = organizationRepository.findById(organizationId)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, organizationId));
    return OrganizationConverter.TO_ORGANIZATION_RESOURCE.apply(organization);
  }

  @Override
  public List<OrganizationResource> getAllOrganization() {
    List<Organization> organizations = organizationRepository.findAll();
    return organizations.stream().map(OrganizationConverter.TO_ORGANIZATION_RESOURCE).toList();
  }

}