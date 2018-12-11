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
package com.epam.ta.reportportal.core.jasper;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.jasper.ExportUtils.durationToShortDHMS;
import static com.epam.ta.reportportal.core.jasper.ExportUtils.getStatisticsCounter;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Jasper Reports provider. Basic implementation of
 * {@link com.epam.ta.reportportal.core.jasper.IGetJasperReportHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class GetJasperReportHandler implements IGetJasperReportHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GetJasperReportHandler.class);

	/* Defined fields in JRXML template */
	private final static String LAUNCH_NAME = "LAUNCH_NAME";
	private final static String LAUNCH_DESC = "LAUNCH_DESCRIPTION";
	private final static String LAUNCH_TAGS = "LAUNCH_TAGS";
	private final static String DURATION = "LAUNCH_DURATION";
	private final static String OWNER = "LAUNCH_OWNER";

	/* Launch statistics fields */
	// TODO could be inject in report as DataSource
	private final static String TOTAL = "TOTAL";
	private final static String PASSED = "PASSED";
	private final static String FAILED = "FAILED";
	private final static String SKIPPED = "SKIPPED";
	private final static String AB = "AB";
	private final static String PB = "PB";
	private final static String SI = "SI";
	private final static String ND = "ND";
	private final static String TI = "TI";

	/* Data sets */
	private final static String TEST_ITEMS = "TEST_ITEMS";

	private final LaunchRepository launchRepository;
	private final UserRepository userRepository;
	private final JasperReportRender reportRender;
	private final JasperDataProvider dataProvider;

	@Autowired
	public GetJasperReportHandler(JasperReportRender reportRender, UserRepository userRepository, JasperDataProvider dataProvider,
			LaunchRepository launchRepository) {
		this.reportRender = checkNotNull(reportRender);
		this.userRepository = checkNotNull(userRepository);
		this.dataProvider = checkNotNull(dataProvider);
		this.launchRepository = checkNotNull(launchRepository);
	}

	@Override
	public JasperPrint getLaunchDetails(Long launchId, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		expect(launch.getStatus(), not(statusIn(IN_PROGRESS))).verify(ErrorType.FORBIDDEN_OPERATION,
				Suppliers.formattedSupplier("Launch '{}' has IN_PROGRESS status. Impossible to export such elements.", launchId)
		);
		String userFullName = userRepository.findById(user.getUserId())
				.map(User::getFullName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUserId()));
		Map<String, Object> params = processLaunchParams(launch);
		Optional<String> owner = userRepository.findById(launch.getUser().getId()).map(User::getFullName);

		/* Check if launch owner still in system if not - setup principal */
		params.put(OWNER, owner.orElse(userFullName));

		params.put(TEST_ITEMS, dataProvider.getReportSource(launchId));
		return reportRender.generateReportPrint(params, new JREmptyDataSource());
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Override
	public ReportFormat getReportFormat(String view) {
		return ReportFormat.findByName(view)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Unexpected report format: {}", view)
				));
	}

	@Override
	public void writeReport(ReportFormat format, OutputStream outputStream, JasperPrint jasperPrint) throws IOException {
		try {
			switch (format) {
				case PDF:
					JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
					break;
				case HTML:
					HtmlExporter exporter = new HtmlExporter();
					exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					HtmlExporterOutput exporterOutput = new SimpleHtmlExporterOutput(outputStream);
					exporter.setExporterOutput(exporterOutput);

					SimpleHtmlReportConfiguration htmlConfig = new SimpleHtmlReportConfiguration();
					htmlConfig.setWhitePageBackground(false);
					htmlConfig.setRemoveEmptySpaceBetweenRows(true);
					exporter.setConfiguration(htmlConfig);
					exporter.exportReport();
					break;
				case XLS:
					SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
					configuration.setOnePagePerSheet(false);
					configuration.setDetectCellType(true);
					configuration.setCollapseRowSpan(false);
					configuration.setIgnoreGraphics(true);

					JRXlsExporter exporterXLS = new JRXlsExporter();
					exporterXLS.setExporterInput(new SimpleExporterInput(jasperPrint));
					exporterXLS.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
					exporterXLS.setConfiguration(configuration);
					exporterXLS.exportReport();
					break;
			}

			outputStream.flush();
			outputStream.close();
		} catch (JRException ex) {
			LOGGER.error("Unable to generate report!", ex);
			BusinessRule.fail()
					.withError(ErrorType.FORBIDDEN_OPERATION,
							Suppliers.formattedSupplier(" Unexpected issue during report output stream creation: {}",
									ex.getLocalizedMessage()
							)
					);
		}
	}

	private Map<String, Object> processLaunchParams(Launch launch) {
		Map<String, Object> params = new HashMap<>();

		params.put(LAUNCH_NAME, launch.getName() + " #" + launch.getNumber());
		params.put(LAUNCH_DESC, launch.getDescription() == null ? "" : launch.getDescription());
		params.put(LAUNCH_TAGS,
				launch.getAttributes()
						.stream()
						.map(it -> it.getKey() == null ? it.getValue() : it.getKey().concat(it.getValue()))
						.collect(Collectors.toList())
		);

		/* Possible NPE for IN_PROGRESS launches */
		params.put(DURATION, durationToShortDHMS(Duration.between(launch.getStartTime(), launch.getEndTime())));

		Set<Statistics> statistics = launch.getStatistics();
		params.put(TOTAL, getStatisticsCounter(statistics, EXECUTIONS_TOTAL));
		params.put(PASSED, getStatisticsCounter(statistics, EXECUTIONS_PASSED));
		params.put(FAILED, getStatisticsCounter(statistics, EXECUTIONS_FAILED));
		params.put(SKIPPED, getStatisticsCounter(statistics, EXECUTIONS_SKIPPED));

		params.put(AB, getStatisticsCounter(statistics, DEFECTS_AUTOMATION_BUG_TOTAL));
		params.put(PB, getStatisticsCounter(statistics, DEFECTS_PRODUCT_BUG_TOTAL));
		params.put(SI, getStatisticsCounter(statistics, DEFECTS_SYSTEM_ISSUE_TOTAL));
		params.put(ND, getStatisticsCounter(statistics, DEFECTS_NO_DEFECT_TOTAL));
		params.put(TI, getStatisticsCounter(statistics, DEFECTS_TO_INVESTIGATE_TOTAL));

		return params;
	}
}