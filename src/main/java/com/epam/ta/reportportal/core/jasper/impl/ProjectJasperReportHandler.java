/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.core.jasper.JasperReportRender;
import com.epam.ta.reportportal.core.jasper.constants.ProjectReportConstants;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.jasper.ReportType;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.google.common.collect.Sets;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("projectJasperReportHandler")
public class ProjectJasperReportHandler extends AbstractJasperReportHandler<ProjectInfo> {

	private static final String UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION = "Report format - {} is not supported for project reports.";

	private final Set<ReportFormat> availableReportFormats;

	private final JasperReportRender reportRender;

	@Autowired
	public ProjectJasperReportHandler(JasperReportRender reportRender) {
		super(UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION);
		this.reportRender = reportRender;
		availableReportFormats = Sets.immutableEnumSet(ReportFormat.CSV);
	}

	@Override
	public JasperPrint getJasperPrint(Map<String, Object> params, JRDataSource dataSource) {

		return reportRender.generateReportPrint(ReportType.PROJECT, params, dataSource);
	}

	@Override
	public Map<String, Object> convertParams(ProjectInfo project) {
		Map<String, Object> params = new HashMap<>();

		params.put(ProjectReportConstants.PROJECT_TYPE, project.getProjectType());
		params.put(ProjectReportConstants.PROJECT_NAME, project.getName());
		params.put(ProjectReportConstants.ORGANIZATION, ofNullable(project.getOrganization()).orElse(EMPTY_STRING));
		params.put(ProjectReportConstants.MEMBERS, project.getUsersQuantity());
		params.put(ProjectReportConstants.LAUNCHES, project.getLaunchesQuantity());

		ofNullable(project.getLastRun()).ifPresent(lastRun -> params.put(ProjectReportConstants.LAST_LAUNCH_DATE,
				DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.of(lastRun, ZoneOffset.UTC))
		));

		return params;
	}

	@Override
	public Set<ReportFormat> getAvailableReportFormats() {
		return availableReportFormats;
	}
}
