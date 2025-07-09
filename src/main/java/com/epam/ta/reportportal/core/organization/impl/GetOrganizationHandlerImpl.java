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

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_USER_ID;
import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;
import static com.epam.ta.reportportal.util.SecurityContextUtils.getPrincipal;
import static com.epam.ta.reportportal.ws.converter.converters.OrganizationConverter.ORG_PROFILE_TO_ORG_INFO;

import com.epam.reportportal.api.model.OrgType;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.reportportal.api.model.OrganizationStatsRelationships;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsLaunches;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsLaunchesMeta;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsProjects;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsProjectsMeta;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsUsers;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsUsersMeta;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.jasper.impl.OrganizationJasperReportHandler;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.organization.OrganizationFilter;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.Lists;
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
  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationJasperReportHandler organizationJasperReportHandler;


  /**
   * Constructs a new instance of GetOrganizationHandlerImpl.
   *
   * @param organizationRepositoryCustom    Custom repository for organization queries.
   * @param organizationUserRepository      Repository for organization user operations.
   * @param organizationJasperReportHandler Handler for Jasper report generation.
   */
  @Autowired
  public GetOrganizationHandlerImpl(OrganizationRepositoryCustom organizationRepositoryCustom,
      OrganizationUserRepository organizationUserRepository,
      OrganizationJasperReportHandler organizationJasperReportHandler) {
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.organizationUserRepository = organizationUserRepository;
    this.organizationJasperReportHandler = organizationJasperReportHandler;
  }

  @Override
  public OrganizationInfo getOrganizationById(Long organizationId) {
    Filter filter = new Filter(OrganizationFilter.class, Lists.newArrayList());
    filter.withCondition(
        new FilterCondition(Condition.EQUALS, false, organizationId.toString(), CRITERIA_ID));
    return organizationRepositoryCustom.findByFilter(filter)
        .stream()
        .map(orgProfile -> ORG_PROFILE_TO_ORG_INFO.apply(orgProfile))
        .findFirst()
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, organizationId));
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
        .map(orgProfile -> new OrganizationInfo()
            .id(orgProfile.getId())
            .createdAt(orgProfile.getCreatedAt())
            .name(orgProfile.getName())
            .updatedAt(orgProfile.getUpdatedAt())
            .slug(orgProfile.getSlug())
            .type(OrgType.fromValue(orgProfile.getType()))
            .externalId(orgProfile.getExternalId())
            .relationships(new OrganizationStatsRelationships()
                .users(new OrganizationStatsRelationshipsUsers()
                    .meta(new OrganizationStatsRelationshipsUsersMeta()
                        .count(orgProfile.getUsersQuantity())))
                .projects(new OrganizationStatsRelationshipsProjects()
                    .meta(new OrganizationStatsRelationshipsProjectsMeta()
                        .count(orgProfile.getProjectsQuantity())))
                .launches(new OrganizationStatsRelationshipsLaunches()
                    .meta(new OrganizationStatsRelationshipsLaunchesMeta()
                        .count(orgProfile.getLaunchesQuantity())
                        .lastOccurredAt(orgProfile.getLastRun())))))
        .toList();

    organizationProfilesPage.items(items);

    return responseWithPageParameters(organizationProfilesPage, pageable,
        organizationProfiles.getTotalElements());
  }


  @Override
  public void exportOrganizations(Queryable filter, Pageable pageable, ReportFormat reportFormat,
      OutputStream outputStream) {
    var orgs = organizationRepositoryCustom.findByFilter(filter, pageable);

    List<? extends Map<String, ?>> data = orgs.stream()
        .map(organizationJasperReportHandler::convertParams)
        .collect(Collectors.toList());

    JRDataSource jrDataSource = new JRBeanCollectionDataSource(data);

    //don't provide any params to not overwrite params from the Jasper template
    JasperPrint jasperPrint = organizationJasperReportHandler.getJasperPrint(null, jrDataSource);

    organizationJasperReportHandler.writeReport(reportFormat, outputStream, jasperPrint);
  }

}
