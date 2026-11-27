package com.epam.reportportal.base.core.tms.mapper.exporter;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Stream;

public interface TmsTestCaseExporter {

  TmsTestCaseExportFormat getSupportedFormat();

  /**
   * Writes the test cases to the response. The stream is consumed once, element by element, so
   * that large exports do not have to be held in memory.
   */
  void export(Stream<TmsTestCaseRS> testCases, boolean includeAttachments,
      HttpServletResponse response);
}
