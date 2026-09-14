/*
 * Copyright 2026 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.ta.reportportal.core.jasper.constants.LaunchReportConstants;
import com.epam.ta.reportportal.core.jasper.impl.LaunchJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.impl.ProjectJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.impl.UserJasperReportHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class JasperReportRenderTest {

  private static JasperReportRender render;
  private static UserJasperReportHandler userHandler;
  private static ProjectJasperReportHandler projectHandler;
  private static LaunchJasperReportHandler launchHandler;

  @BeforeAll
  static void compileTemplates() throws Exception {
    render = new JasperReportRender(new DefaultResourceLoader());
    userHandler = new UserJasperReportHandler(render);
    projectHandler = new ProjectJasperReportHandler(render);
    launchHandler = new LaunchJasperReportHandler(render);
  }

  @Test
  void shouldExportUsersCsv() {
    Map<String, Object> row = new HashMap<>();
    row.put("Full name", "Jane Doe");
    row.put("Type", "INTERNAL");
    row.put("Login", "jane");
    row.put("Email", "jane@example.com");
    row.put("Last login", "2026-01-01T00:00:00Z");
    row.put("Projects and Roles", "demo - MEMBER");

    var print = userHandler.getJasperPrint(null, new JRBeanCollectionDataSource(List.of(row)));
    byte[] csv = userHandler.exportReportBytes(ReportFormat.CSV, print);
    assertNotNull(csv);
    assertFalse(csv.length == 0);
  }

  @Test
  void shouldExportProjectsCsv() {
    Map<String, Object> row = new HashMap<>();
    row.put("Project name", "demo");
    row.put("Project type", "INTERNAL");
    row.put("Organization", "epam");
    row.put("Members", 2);
    row.put("Launches", 3);
    row.put("Last launch date", "2026-01-01T00:00:00Z");

    var print = projectHandler.getJasperPrint(null, new JRBeanCollectionDataSource(List.of(row)));
    byte[] csv = projectHandler.exportReportBytes(ReportFormat.CSV, print);
    assertNotNull(csv);
    assertFalse(csv.length == 0);
  }

  @Test
  void shouldExportLaunchPdfHtmlAndXls() {
    LaunchItemRow item = new LaunchItemRow();

    Map<String, Object> params = new HashMap<>();
    params.put(LaunchReportConstants.LAUNCH_NAME, "Demo #1");
    params.put(LaunchReportConstants.LAUNCH_DESC, "nightly");
    params.put(LaunchReportConstants.OWNER, "jane");
    params.put(LaunchReportConstants.DURATION, "1s");
    params.put(LaunchReportConstants.TOTAL, 1);
    params.put(LaunchReportConstants.PASSED, 1);
    params.put(LaunchReportConstants.FAILED, 0);
    params.put(LaunchReportConstants.SKIPPED, 0);
    params.put(LaunchReportConstants.AB, 0);
    params.put(LaunchReportConstants.PB, 0);
    params.put(LaunchReportConstants.SI, 0);
    params.put(LaunchReportConstants.ND, 0);
    params.put(LaunchReportConstants.TI, 0);
    params.put(LaunchReportConstants.TEST_ITEMS, List.of(item));

    var print = launchHandler.getJasperPrint(params, new JREmptyDataSource());
    assertNotNull(print);

    byte[] pdf = launchHandler.exportReportBytes(ReportFormat.PDF, print);
    byte[] html = launchHandler.exportReportBytes(ReportFormat.HTML, print);
    byte[] xls = launchHandler.exportReportBytes(ReportFormat.XLS, print);
    assertFalse(pdf.length == 0);
    assertFalse(html.length == 0);
    assertFalse(xls.length == 0);
  }

  public static class LaunchItemRow {

    public String getType() {
      return "STEP";
    }

    public String getName() {
      return "login test";
    }

    public Double getDuration() {
      return 1.5d;
    }

    public String getStatus() {
      return "PASSED";
    }

    public Integer getTotal() {
      return 1;
    }

    public Integer getPassed() {
      return 1;
    }

    public Integer getFailed() {
      return 0;
    }

    public Integer getSkipped() {
      return 0;
    }

    public Integer getAutomationBug() {
      return 0;
    }

    public Integer getProductBug() {
      return 0;
    }

    public Integer getSystemIssue() {
      return 0;
    }

    public Integer getNoDefect() {
      return 0;
    }

    public Integer getToInvestigate() {
      return 0;
    }
  }
}
