/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.core.jasper.constants.UserReportConstants;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.jasper.ReportType;
import com.epam.ta.reportportal.entity.user.User;
import com.google.common.collect.Sets;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.converter.builders.UserBuilder.USER_LAST_LOGIN;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("userJasperReportHandler")
public class UserJasperReportHandler extends AbstractJasperReportHandler<User> {

	private static final String UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION = "Report format - {} is not supported for user reports.";

	private final Set<ReportFormat> availableReportFormats;

	private final JasperReportRender reportRender;

	@Autowired
	public UserJasperReportHandler(JasperReportRender reportRender) {
		super(UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION);
		this.reportRender = reportRender;
		availableReportFormats = Sets.immutableEnumSet(ReportFormat.CSV);
	}

	@Override
	public JasperPrint getJasperPrint(Map<String, Object> params, JRDataSource dataSource) {

		return reportRender.generateReportPrint(ReportType.USER, params, dataSource);
	}

	@Override
	public Map<String, Object> convertParams(User user) {
		Map<String, Object> params = new HashMap<>();

		params.put(UserReportConstants.FULL_NAME, user.getFullName());
		params.put(UserReportConstants.LOGIN, user.getLogin());
		params.put(UserReportConstants.EMAIL, user.getEmail());

		params.put(UserReportConstants.PROJECTS_AND_ROLES, user.getProjects().stream().collect(Collectors.toMap(
				projectUser -> projectUser.getProject().getName(),
				projectUser -> projectUser.getProjectRole().name(),
				(prev, curr) -> prev
		)).entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue()).collect(Collectors.joining(", ")));

		ofNullable(user.getMetadata()).ifPresent(metadata -> ofNullable(metadata.getMetadata()).ifPresent(meta -> ofNullable(meta.get(
				USER_LAST_LOGIN)).ifPresent(lastLogin -> {
			try {
				long epochMilli = Long.parseLong(String.valueOf(lastLogin));
				Instant instant = Instant.ofEpochMilli(epochMilli);
				params.put(
						UserReportConstants.LAST_LOGIN,
						DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC))
				);
			} catch (NumberFormatException e) {
				//do nothing, null value will be put in the result
			}

		})));

		return params;
	}

	@Override
	public Set<ReportFormat> getAvailableReportFormats() {
		return availableReportFormats;
	}
}
