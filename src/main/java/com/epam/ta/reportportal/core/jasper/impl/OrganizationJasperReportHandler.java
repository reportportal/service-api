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

package com.epam.ta.reportportal.core.jasper.impl;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.jasper.JasperReportRender;
import com.epam.ta.reportportal.core.jasper.constants.OrganizationReportConstants;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.jasper.ReportType;
import com.epam.ta.reportportal.entity.organization.OrganizationProfile;
import com.google.common.collect.Sets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;


/**
 * Handler for generating Jasper reports for organizations. Extends {@link AbstractJasperReportHandler} for
 * {@link OrganizationProfile}. Provides available report formats and parameter conversion for organization reports.
 */
@Service
public class OrganizationJasperReportHandler extends AbstractJasperReportHandler<OrganizationProfile> {

  private static final String UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION =
      "Report format - {} is not supported for user reports.";

  private final Set<ReportFormat> availableReportFormats;

  private final JasperReportRender reportRender;

  /**
   * Constructs an OrganizationJasperReportHandler with the specified JasperReportRender.
   *
   * @param reportRender The JasperReportRender used to generate JasperPrint objects.
   */
  public OrganizationJasperReportHandler(JasperReportRender reportRender) {
    super(UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION);
    this.reportRender = reportRender;
    this.availableReportFormats = Sets.immutableEnumSet(ReportFormat.CSV, ReportFormat.TEXT_CSV);
  }

  @Override
  public Set<ReportFormat> getAvailableReportFormats() {
    return availableReportFormats;
  }

  @Override
  public JasperPrint getJasperPrint(Map<String, Object> params, JRDataSource dataSource) {
    return reportRender.generateReportPrint(ReportType.ORGANIZATION, params, dataSource);
  }

  @Override
  public Map<String, Object> convertParams(OrganizationProfile org) {
    Map<String, Object> params = new HashMap<>();

    params.put(OrganizationReportConstants.NAME, org.getName());
    params.put(OrganizationReportConstants.TYPE, org.getType());
    params.put(OrganizationReportConstants.PROJECTS, ofNullable(org.getProjectsQuantity()).orElse(0));
    params.put(OrganizationReportConstants.MEMBERS, ofNullable(org.getUsersQuantity()).orElse(0));
    params.put(OrganizationReportConstants.LAUNCHES, ofNullable(org.getLaunchesQuantity()).orElse(0));
    ofNullable(org.getLastRun())
        .ifPresent(lastRun -> params.put(
            OrganizationReportConstants.LAST_LAUNCH_DATE,
            DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.ofInstant(lastRun, ZoneOffset.UTC))
        ));

    return params;
  }
}
