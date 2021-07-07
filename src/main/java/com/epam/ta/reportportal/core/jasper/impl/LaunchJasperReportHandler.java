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
import com.epam.ta.reportportal.core.jasper.constants.LaunchReportConstants;
import com.epam.ta.reportportal.core.jasper.util.ExportUtils;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.jasper.ReportType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.google.common.collect.Sets;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("launchJasperReportHandler")
public class LaunchJasperReportHandler extends AbstractJasperReportHandler<Launch> {

	private static final String UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION = "Report format - {} is not supported for launch reports.";

	private final Set<ReportFormat> availableReportFormats;

	private final JasperReportRender reportRender;

	@Autowired
	public LaunchJasperReportHandler(JasperReportRender reportRender) {
		super(UNSUPPORTED_REPORT_FORMAT_MESSAGE_EXCEPTION);
		this.reportRender = reportRender;
		availableReportFormats = Sets.immutableEnumSet(ReportFormat.HTML, ReportFormat.PDF, ReportFormat.XLS);
	}

	@Override
	public JasperPrint getJasperPrint(Map<String, Object> params, JRDataSource dataSource) {

		return reportRender.generateReportPrint(ReportType.LAUNCH, params, dataSource);
	}

	@Override
	public Map<String, Object> convertParams(Launch launch) {
		Map<String, Object> params = new HashMap<>();

		params.put(LaunchReportConstants.LAUNCH_NAME, launch.getName() + " #" + launch.getNumber());
		params.put(LaunchReportConstants.LAUNCH_DESC, launch.getDescription() == null ? "" : launch.getDescription());
		params.put(LaunchReportConstants.LAUNCH_TAGS,
				launch.getAttributes()
						.stream()
						.map(it -> it.getKey() == null ? it.getValue() : it.getKey().concat(it.getValue()))
						.collect(Collectors.toList())
		);

		String duration = ofNullable(launch.getEndTime()).map(endTime -> ExportUtils.durationToShortDHMS(
				Duration.between(launch.getStartTime(), endTime))).orElse(StringUtils.EMPTY);
		params.put(LaunchReportConstants.DURATION, duration);

		Set<Statistics> statistics = launch.getStatistics();
		params.put(LaunchReportConstants.TOTAL, ExportUtils.getStatisticsCounter(statistics, EXECUTIONS_TOTAL));
		params.put(LaunchReportConstants.PASSED, ExportUtils.getStatisticsCounter(statistics, EXECUTIONS_PASSED));
		params.put(LaunchReportConstants.FAILED, ExportUtils.getStatisticsCounter(statistics, EXECUTIONS_FAILED));
		params.put(LaunchReportConstants.SKIPPED, ExportUtils.getStatisticsCounter(statistics, EXECUTIONS_SKIPPED));
		params.put(LaunchReportConstants.UNTESTED, ExportUtils.getStatisticsCounter(statistics, EXECUTIONS_UNTESTED));

		params.put(LaunchReportConstants.AB, ExportUtils.getStatisticsCounter(statistics, DEFECTS_AUTOMATION_BUG_TOTAL));
		params.put(LaunchReportConstants.PB, ExportUtils.getStatisticsCounter(statistics, DEFECTS_PRODUCT_BUG_TOTAL));
		params.put(LaunchReportConstants.SI, ExportUtils.getStatisticsCounter(statistics, DEFECTS_SYSTEM_ISSUE_TOTAL));
		params.put(LaunchReportConstants.ND, ExportUtils.getStatisticsCounter(statistics, DEFECTS_NO_DEFECT_TOTAL));
		params.put(LaunchReportConstants.TI, ExportUtils.getStatisticsCounter(statistics, DEFECTS_TO_INVESTIGATE_TOTAL));

		return params;
	}

	@Override
	public Set<ReportFormat> getAvailableReportFormats() {
		return availableReportFormats;
	}

}
