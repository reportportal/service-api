package com.epam.ta.reportportal.core.tms.mapper.factory;

import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestFolderExporter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TmsTestFolderExporterFactory {

  private Map<TmsTestFolderExportFileType, TmsTestFolderExporter> tmsTestFolderExporters;

  @Autowired
  public void setTmsTestFolderExporters(
      List<TmsTestFolderExporter> tmsTestFolderExporters) {
    this.tmsTestFolderExporters = tmsTestFolderExporters
        .stream()
        .collect(
            Collectors.toMap(TmsTestFolderExporter::getTmsTestFolderExportFileType,
                Function.identity())
        );
  }

  public TmsTestFolderExporter getExporter(TmsTestFolderExportFileType fileType) {
    var parser = tmsTestFolderExporters.get(fileType);
    if (Objects.isNull(parser)) {
      throw new UnsupportedOperationException("Unsupported file type.");
    } else {
      return parser;
    }
  }
}
