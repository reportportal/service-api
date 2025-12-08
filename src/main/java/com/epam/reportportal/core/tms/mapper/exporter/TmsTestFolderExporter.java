package com.epam.reportportal.core.tms.mapper.exporter;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import jakarta.servlet.http.HttpServletResponse;

public interface TmsTestFolderExporter {

  TmsTestFolderExportFileType getTmsTestFolderExportFileType();

  void export(TmsTestFolder tmsTestFolder, HttpServletResponse response);
}
