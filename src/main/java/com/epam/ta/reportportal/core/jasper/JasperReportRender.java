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

package com.epam.ta.reportportal.core.jasper;

import static com.google.common.base.Preconditions.checkArgument;

import com.epam.ta.reportportal.entity.jasper.ReportType;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Jasper Report render based on provided JRXML template.<br>
 *
 * @author Andrei_Ramanchuk
 * @author Andrei Varabyeu Performance improvements. Load JasperReport only once since it is immutable
 */
@Service("jasperRender")
public class JasperReportRender {

  private static final Logger LOGGER = LoggerFactory.getLogger(JasperReportRender.class);

  private static final String ORGANIZATIONS_REPORT_JRXML_TEMPLATE = "classpath:/templates/report/organizations.jrxml";
  private static final String PROJECTS_REPORT_JRXML_TEMPLATE = "classpath:/templates/report/projects.jrxml";
  private static final String USERS_REPORT_JRXML_TEMPLATE = "classpath:/templates/report/users.jrxml";
  private static final String LAUNCH_REPORT_JRXML_TEMPLATE = "classpath:/templates/report/report.jrxml";
  private static final Map<ReportType, String> reportTypeTemplatePathMapping =
      ImmutableMap.<ReportType, String>builder()
          .put(ReportType.ORGANIZATION, ORGANIZATIONS_REPORT_JRXML_TEMPLATE)
          .put(ReportType.PROJECT, PROJECTS_REPORT_JRXML_TEMPLATE)
          .put(ReportType.USER, USERS_REPORT_JRXML_TEMPLATE)
          .put(ReportType.LAUNCH, LAUNCH_REPORT_JRXML_TEMPLATE)
          .build();

  private final Map<ReportType, JasperReport> reportTemplatesMapping;


  /**
   * Constructs a JasperReportRender and preloads compiled JasperReport templates for each ReportType.
   *
   * @param resourceLoader The Spring ResourceLoader used to load JRXML template files from the classpath.
   * @throws JRException If a JasperReports error occurs during compilation.
   * @throws IOException If an I/O error occurs while reading template files.
   */
  @Autowired
  public JasperReportRender(ResourceLoader resourceLoader) throws JRException, IOException {

    ImmutableMap.Builder<ReportType, JasperReport> reportTypeJasperReportBuilder = ImmutableMap.builder();

    for (Map.Entry<ReportType, String> entry : reportTypeTemplatePathMapping.entrySet()) {
      Resource reportTemplate = resourceLoader.getResource(entry.getValue());
      checkArgument(reportTemplate.exists());
      try (InputStream inputStream = reportTemplate.getInputStream()) {
        JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
        reportTypeJasperReportBuilder.put(entry.getKey(),
            JasperCompileManager.compileReport(jasperDesign));
      }
    }

    reportTemplatesMapping = reportTypeJasperReportBuilder.build();

  }

  /**
   * Generates a JasperPrint object for the specified report type using the provided parameters and data source.
   *
   * @param reportType The type of report to generate.
   * @param params     The parameters to pass to the report.
   * @param datasource The data source for the report.
   * @return A filled JasperPrint object, or an empty JasperPrint if an error occurs.
   */
  public JasperPrint generateReportPrint(ReportType reportType, Map<String, Object> params, JRDataSource datasource) {
    try {
      return JasperFillManager.fillReport(reportTemplatesMapping.get(reportType), params, datasource);
    } catch (JRException e) {
      LOGGER.error("Unable to generate Report", e);
      return new JasperPrint();
    }
  }
}
