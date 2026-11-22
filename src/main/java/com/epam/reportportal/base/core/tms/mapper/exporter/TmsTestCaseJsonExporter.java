package com.epam.reportportal.base.core.tms.mapper.exporter;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class TmsTestCaseJsonExporter implements TmsTestCaseExporter {

  private final JsonMapper objectMapper;

  @Override
  public TmsTestCaseExportFormat getSupportedFormat() {
    return TmsTestCaseExportFormat.JSON;
  }

  @Override
  @SneakyThrows
  public void export(Stream<TmsTestCaseRS> testCases, boolean includeAttachments,
      HttpServletResponse response) {
    configureHttpResponse(response, includeAttachments);

    // Writes the JSON array element by element instead of building it in memory. The writer is
    // deliberately not closed on failure: closing would terminate the array and finish the
    // response, making a truncated export look complete. The exception reaches the container,
    // which aborts the connection.
    var sequenceWriter = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValuesAsArray(response.getOutputStream());
    for (var testCase : (Iterable<TmsTestCaseRS>) testCases::iterator) {
      sequenceWriter.write(testCase);
    }
    sequenceWriter.close();
  }

  private void configureHttpResponse(HttpServletResponse response, boolean includeAttachments) {
    response.setContentType("application/json");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    var suffix = includeAttachments ? "_with_attachments" : "";
    var filename = "test_cases_export" + suffix + ".json";
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
  }
}
