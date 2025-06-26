package com.epam.ta.reportportal.core.tms.mapper.exporter;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmsTestCaseJsonExporter implements TmsTestCaseExporter {

  private final ObjectMapper objectMapper;

  @Override
  public TmsTestCaseExportFormat getSupportedFormat() {
    return TmsTestCaseExportFormat.JSON;
  }

  @Override
  @SneakyThrows
  public void export(List<TmsTestCaseRS> testCases, boolean includeAttachments,
      HttpServletResponse response) {
    configureHttpResponse(response, includeAttachments);

    var jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(testCases);
    response.getOutputStream().write(jsonContent.getBytes(StandardCharsets.UTF_8));
    response.getOutputStream().flush();
  }

  private void configureHttpResponse(HttpServletResponse response, boolean includeAttachments) {
    response.setContentType("application/json");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    var suffix = includeAttachments ? "_with_attachments" : "";
    var filename = "test_cases_export" + suffix + ".json";
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
  }
}
