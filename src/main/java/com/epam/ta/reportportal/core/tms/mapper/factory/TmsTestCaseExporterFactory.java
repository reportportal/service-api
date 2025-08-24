package com.epam.ta.reportportal.core.tms.mapper.factory;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestCaseExporter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TmsTestCaseExporterFactory {

  private final Map<TmsTestCaseExportFormat, TmsTestCaseExporter> exporters;

  public TmsTestCaseExporterFactory(List<TmsTestCaseExporter> exporters) {
    this.exporters = exporters
        .stream()
        .collect(
            Collectors.toMap(TmsTestCaseExporter::getSupportedFormat, Function.identity())
        );
  }

  public TmsTestCaseExporter getExporter(String format) {
    var exportFormat = TmsTestCaseExportFormat.fromString(format);
    var exporter = exporters.get(exportFormat);

    if (exporter == null) {
      throw new UnsupportedOperationException("Unsupported export format: " + format);
    }

    return exporter;
  }
}
