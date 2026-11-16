package com.epam.reportportal.base.core.tms.mapper.factory;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.reportportal.base.core.tms.mapper.importer.TmsTestCaseImporter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TmsTestCaseImporterFactory {

  private final Map<TmsTestCaseImportFormat, TmsTestCaseImporter> importers;

  public TmsTestCaseImporterFactory(List<TmsTestCaseImporter> importers) {
    this.importers = importers
        .stream()
        .collect(
            Collectors.toMap(TmsTestCaseImporter::getSupportedFormat, Function.identity())
        );
  }

  public TmsTestCaseImporter getImporter(TmsTestCaseImportFormat format) {
    var importer = importers.get(format);
    if (importer == null) {
      throw new UnsupportedOperationException("Unsupported import format: " + format);
    }
    return importer;
  }

  public TmsTestCaseImporter getImporter(String fileName) {
    var format = TmsTestCaseImportFormat.fromFileName(fileName);
    return getImporter(format);
  }
}
