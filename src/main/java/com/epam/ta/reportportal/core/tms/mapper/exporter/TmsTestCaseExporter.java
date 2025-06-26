package com.epam.ta.reportportal.core.tms.mapper.exporter;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public interface TmsTestCaseExporter {

  TmsTestCaseExportFormat getSupportedFormat();

  void export(List<TmsTestCaseRS> testCases, boolean includeAttachments, HttpServletResponse response);
}
