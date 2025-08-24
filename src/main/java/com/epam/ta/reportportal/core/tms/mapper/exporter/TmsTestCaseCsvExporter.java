package com.epam.ta.reportportal.core.tms.mapper.exporter;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmsTestCaseCsvExporter implements TmsTestCaseExporter {

  private static final String[] CSV_HEADERS = {
      "ID", "Name", "Description", "Test Folder", "Priority"
  };

  @Override
  public TmsTestCaseExportFormat getSupportedFormat() {
    return TmsTestCaseExportFormat.CSV;
  }

  @Override
  @SneakyThrows
  public void export(List<TmsTestCaseRS> testCases, boolean includeAttachments, HttpServletResponse response) {
    configureHttpResponse(response);

    try (var writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
        var csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
            .setHeader(CSV_HEADERS)
            .build())) {

      for (var testCase : testCases) {
        csvPrinter.printRecord(
            testCase.getId(),
            testCase.getName(),
            testCase.getDescription(),
            testCase.getTestFolder() != null ? testCase.getTestFolder().getId() : "",
            testCase.getPriority()
        );
      }
      csvPrinter.flush();
    }
  }

  private void configureHttpResponse(HttpServletResponse response) {
    response.setContentType("text/csv");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setHeader("Content-Disposition", "attachment; filename=\"test_cases_export.csv\"");
  }
}
