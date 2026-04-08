/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.organization.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_USER_ID;
import static com.epam.reportportal.base.util.OffsetUtils.responseWithPageParameters;
import static com.epam.reportportal.base.util.SecurityContextUtils.getPrincipal;
import static com.epam.reportportal.base.ws.converter.converters.OrganizationConverter.ORG_PROFILE_TO_BASE_ORG_INFO;
import static com.epam.reportportal.base.ws.converter.converters.OrganizationConverter.ORG_PROFILE_TO_ORG_INFO;

import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.reportportal.base.core.jasper.ReportFormat;
import com.epam.reportportal.base.core.jasper.impl.OrganizationJasperReportHandler;
import com.epam.reportportal.base.core.organization.GetOrganizationHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


/**
 * Service implementation for handling organization-related operations. Provides methods to retrieve, list, and export
 * organizations.
 */
@Service
public class GetOrganizationHandlerImpl implements GetOrganizationHandler {

  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final OrganizationJasperReportHandler organizationJasperReportHandler;


  /**
   * Constructs a new instance of GetOrganizationHandlerImpl.
   *
   * @param organizationRepositoryCustom    Custom repository for organization queries.
   * @param organizationJasperReportHandler Handler for Jasper report generation.
   */
  @Autowired
  public GetOrganizationHandlerImpl(OrganizationRepositoryCustom organizationRepositoryCustom,
      OrganizationJasperReportHandler organizationJasperReportHandler) {
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.organizationJasperReportHandler = organizationJasperReportHandler;
  }

  @Override
  public OrganizationInfo getOrganizationById(Long organizationId) {
    var rpUser = getPrincipal();
    Long userId = rpUser.getUserRole().equals(UserRole.ADMINISTRATOR) ? null : rpUser.getUserId();
    return organizationRepositoryCustom.findOrganizationByIdAndUserId(organizationId, userId)
        .map(ORG_PROFILE_TO_ORG_INFO)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, organizationId));
  }

  @Override
  public OrganizationPage getOrganizations(Queryable filter, Pageable pageable) {
    var rpUser = getPrincipal();
    OrganizationPage organizationProfilesPage = new OrganizationPage();

    if (!rpUser.getUserRole().equals(UserRole.ADMINISTRATOR)) {
      filter.getFilterConditions()
          .add(new FilterCondition(Condition.EQUALS, false, rpUser.getUserId().toString(), CRITERIA_ORG_USER_ID));
    }

    var organizationProfiles = organizationRepositoryCustom.findByFilter(filter, pageable);
    var items = organizationProfiles.getContent()
        .stream()
        .map(ORG_PROFILE_TO_BASE_ORG_INFO)
        .toList();

    organizationProfilesPage.items(items);

    return responseWithPageParameters(organizationProfilesPage, pageable,
        organizationProfiles.getTotalElements());
  }


  @Override
  public byte[] exportOrganizations(Queryable filter, Pageable pageable, ReportFormat reportFormat,
      OutputStream outputStream) {
    var orgs = organizationRepositoryCustom.findByFilter(filter, pageable);

    List<? extends Map<String, ?>> data = orgs.stream()
        .map(organizationJasperReportHandler::convertParams)
        .collect(Collectors.toList());

    JRDataSource jrDataSource = new JRBeanCollectionDataSource(data);

    //don't provide any params to not overwrite params from the Jasper template
    JasperPrint jasperPrint = organizationJasperReportHandler.getJasperPrint(null, jrDataSource);

    return organizationJasperReportHandler.exportReportBytes(reportFormat, jasperPrint);
  }

}
