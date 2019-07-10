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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Set;

import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

/**
 * Jasper Reports provider. Basic implementation of
 * {@link com.epam.ta.reportportal.core.jasper.GetJasperReportHandler}
 *
 * @author Andrei_Ramanchuk
 */
public abstract class AbstractJasperReportHandler<T> implements GetJasperReportHandler<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJasperReportHandler.class);

	private final String unsupportedReportFormatExceptionMessage;

	public AbstractJasperReportHandler(String unsupportedReportFormatExceptionMessage) {

		this.unsupportedReportFormatExceptionMessage = unsupportedReportFormatExceptionMessage;
	}

	@Override
	public ReportFormat getReportFormat(String view) {
		ReportFormat reportFormat = ReportFormat.findByName(view)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Unexpected report format: {}", view)
				));

		BusinessRule.expect(reportFormat, getAvailableReportFormats()::contains)
				.verify(ErrorType.BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier(unsupportedReportFormatExceptionMessage, reportFormat.name())
				);

		return reportFormat;
	}

	@Override
	public void writeReport(ReportFormat format, OutputStream outputStream, JasperPrint jasperPrint) {
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
				case CSV:

					JRCsvExporter jrCsvExporter = new JRCsvExporter();
					jrCsvExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					jrCsvExporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
					SimpleCsvExporterConfiguration csvExporterConfiguration = new SimpleCsvExporterConfiguration();
					jrCsvExporter.setConfiguration(csvExporterConfiguration);
					jrCsvExporter.exportReport();
					break;
			}

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

	public abstract Set<ReportFormat> getAvailableReportFormats();
}