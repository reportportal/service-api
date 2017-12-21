/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.jasper;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.user.User;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
		this.reportRender = com.google.common.base.Preconditions.checkNotNull(reportRender);
		this.userRepository = com.google.common.base.Preconditions.checkNotNull(userRepository);
		this.dataProvider = com.google.common.base.Preconditions.checkNotNull(dataProvider);
		this.launchRepository = com.google.common.base.Preconditions.checkNotNull(launchRepository);
	}

	@Override
	public JasperPrint getLaunchDetails(String launchId, String username) {
		Launch launch = launchRepository.findOne(launchId);
		BusinessRule.expect(launch, Predicates.notNull()).verify(ErrorType.LAUNCH_NOT_FOUND, launchId);
		BusinessRule.expect(launch, Predicates.not(Preconditions.IN_PROGRESS))
				.verify(ErrorType.FORBIDDEN_OPERATION,
						Suppliers.formattedSupplier("Launch '{}' has IN_PROGRESS status. Impossible to export such elements.", launchId)
				);
		User user = userRepository.findOne(username);
		BusinessRule.expect(user, Predicates.notNull()).verify(ErrorType.USER_NOT_FOUND, username);
		Map<String, Object> params = processLaunchParams(launch);
		User owner = userRepository.findOne(launch.getUserRef());
		/* Check if launch owner still in system if not - setup principal */
		if (null != owner) {
			params.put(OWNER, owner.getFullName());
		} else {
			params.put(OWNER, user.getFullName());
		}
		params.put(TEST_ITEMS, dataProvider.getReportSource(launch));
		return reportRender.generateReportPrint(params, new JREmptyDataSource());
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Override
	public ReportFormat getReportFormat(String view) {
		Optional<ReportFormat> format = ReportFormat.findByName(view);
		BusinessRule.expect(format, Preconditions.IS_PRESENT)
				.verify(ErrorType.BAD_REQUEST_ERROR, Suppliers.formattedSupplier("Unexpected report format: {}", view));
		return format.get();
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

	/**
	 * Format launch duration from long to human readable format.
	 *
	 * @param duration - input duration as long value
	 * @return String - formatted output
	 */
	private static String millisToShortDHMS(long duration) {
		String res;
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
		if (days == 0) {
			res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} else {
			res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
		}
		return res;
	}

	private Map<String, Object> processLaunchParams(Launch launch) {
		Map<String, Object> params = new HashMap<>();

		params.put(LAUNCH_NAME, launch.getName() + " #" + launch.getNumber());
		params.put(LAUNCH_DESC, launch.getDescription() == null ? "" : launch.getDescription());
		params.put(LAUNCH_TAGS, launch.getTags());

						/* Possible NPE for IN_PROGRESS launches */
		params.put(DURATION, millisToShortDHMS(launch.getEndTime().getTime() - launch.getStartTime().getTime()));

		ExecutionCounter exec = launch.getStatistics().getExecutionCounter();
		params.put(TOTAL, exec.getTotal());
		params.put(PASSED, exec.getPassed());
		params.put(FAILED, exec.getFailed());
		params.put(SKIPPED, exec.getSkipped());

		IssueCounter issue = launch.getStatistics().getIssueCounter();
		params.put(AB, issue.getAutomationBugTotal());
		params.put(PB, issue.getProductBugTotal());
		params.put(SI, issue.getSystemIssueTotal());
		params.put(ND, issue.getNoDefectTotal());
		params.put(TI, issue.getToInvestigateTotal());

		return params;
	}
}