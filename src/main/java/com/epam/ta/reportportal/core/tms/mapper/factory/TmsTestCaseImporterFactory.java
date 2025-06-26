package com.epam.ta.reportportal.core.tms.mapper.factory;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.mapper.importer.TmsTestCaseImporter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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

  public TmsTestCaseImporter getImporter(MultipartFile file) {
    var format = TmsTestCaseImportFormat.fromFileName(file.getOriginalFilename());
    var importer = importers.get(format);

    if (importer == null) {
      throw new UnsupportedOperationException("Unsupported import format: " + format);
    }

    return importer;
  }
}
