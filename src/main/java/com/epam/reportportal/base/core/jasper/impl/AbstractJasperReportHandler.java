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

package com.epam.reportportal.base.core.jasper.impl;

import com.epam.reportportal.base.core.jasper.GetJasperReportHandler;
import com.epam.reportportal.base.core.jasper.ReportFormat;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jasper Reports provider. Basic implementation of
 * {@link com.epam.reportportal.base.core.jasper.GetJasperReportHandler}
 *
 * @param <T> report data model type
 * @author Andrei_Ramanchuk
 */
public abstract class AbstractJasperReportHandler<T> implements GetJasperReportHandler<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJasperReportHandler.class);

  private final String unsupportedReportFormatExceptionMessage;

  /**
   * Constructor for AbstractJasperReportHandler.
   *
   * @param unsupportedReportFormatExceptionMessage Message to use when an unsupported report format is encountered.
   */
  public AbstractJasperReportHandler(String unsupportedReportFormatExceptionMessage) {
    this.unsupportedReportFormatExceptionMessage = unsupportedReportFormatExceptionMessage;
  }

  @Override
  public ReportFormat getReportFormat(String view) {
    ReportFormat reportFormat = ReportFormat.findByValue(view)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            Suppliers.formattedSupplier("Unexpected report format: {}", view)
        ));

    BusinessRule.expect(reportFormat, getAvailableReportFormats()::contains)
        .verify(ErrorType.BAD_REQUEST_ERROR,
            Suppliers.formattedSupplier(unsupportedReportFormatExceptionMessage,
                reportFormat.name())
        );

    return reportFormat;
  }

  @Override
  public byte[] exportReportBytes(ReportFormat format, JasperPrint jasperPrint) {
    try {
      switch (format) {
        case PDF:
          return JasperExportManager.exportReportToPdf(jasperPrint);
        case HTML:
          ByteArrayOutputStream htmlOutput = new ByteArrayOutputStream();
          HtmlExporter exporter = new HtmlExporter();
          exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
          exporter.setExporterOutput(new SimpleHtmlExporterOutput(htmlOutput));

          SimpleHtmlReportConfiguration htmlConfig = new SimpleHtmlReportConfiguration();
          htmlConfig.setWhitePageBackground(false);
          htmlConfig.setRemoveEmptySpaceBetweenRows(true);
          exporter.setConfiguration(htmlConfig);
          exporter.exportReport();
          return htmlOutput.toByteArray();
        case XLS:
          ByteArrayOutputStream xlsOutput = new ByteArrayOutputStream();

          SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
          configuration.setOnePagePerSheet(false);
          configuration.setDetectCellType(true);
          configuration.setCollapseRowSpan(false);
          configuration.setIgnoreGraphics(true);

          JRXlsExporter xlsExporter = new JRXlsExporter();
          xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
          xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(xlsOutput));
          xlsExporter.setConfiguration(configuration);
          xlsExporter.exportReport();
          return xlsOutput.toByteArray();
        case CSV:
        case TEXT_CSV:
          ByteArrayOutputStream csvOutput = new ByteArrayOutputStream();

          JRCsvExporter jrCsvExporter = new JRCsvExporter();
          jrCsvExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
          jrCsvExporter.setExporterOutput(new SimpleWriterExporterOutput(csvOutput));

          SimpleCsvExporterConfiguration csvExporterConfiguration = new SimpleCsvExporterConfiguration();
          jrCsvExporter.setConfiguration(csvExporterConfiguration);
          jrCsvExporter.exportReport();
          return csvOutput.toByteArray();
        default:
          throw new UnsupportedOperationException(format.getValue());
      }

    } catch (JRException ex) {
      LOGGER.error("Unable to generate report!", ex);
      BusinessRule.fail()
          .withError(ErrorType.FORBIDDEN_OPERATION,
              Suppliers.formattedSupplier(
                  " Unexpected issue during report output stream creation: {}",
                  ex.getLocalizedMessage()
              )
          );
    }
    return new byte[]{};
  }

  /**
   * Returns the set of available report formats supported by this handler.
   *
   * @return a set of supported {@link ReportFormat} values
   */
  public abstract Set<ReportFormat> getAvailableReportFormats();
}
