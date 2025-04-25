package com.epam.ta.reportportal.core.tms.mapper.exporter;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import jakarta.servlet.http.HttpServletResponse;

public interface TmsTestFolderExporter {

  TmsTestFolderExportFileType getTmsTestFolderExportFileType();

  void export(TmsTestFolder tmsTestFolder, HttpServletResponse response);
}
