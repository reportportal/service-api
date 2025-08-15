package com.epam.ta.reportportal.core.tms.mapper.exporter;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

@Component
public class TmsTestFolderCsvExporter implements TmsTestFolderExporter {

  private static final String[] CSV_HEADERS = {"Id", "Name", "Description", "Level", "Path",
      "Parent Id"};

  @Override
  public TmsTestFolderExportFileType getTmsTestFolderExportFileType() {
    return TmsTestFolderExportFileType.CSV;
  }

  @Override
  @SneakyThrows
  public void export(TmsTestFolder tmsTestFolder, HttpServletResponse response) {
    configureHttpResponse(response, tmsTestFolder.getId());

    // Create CSV printer and write data
    try (var writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
        var csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
            .setHeader(CSV_HEADERS).build())) {

      // Collect all folders into a flat list for export
      var foldersToExport = new ArrayList<FolderExportData>();
      collectFoldersForExport(tmsTestFolder, foldersToExport, 0, tmsTestFolder.getName());

      // Write each folder to CSV
      for (FolderExportData folderData : foldersToExport) {
        csvPrinter.printRecord(
            folderData.getId(),
            folderData.getName(),
            folderData.getDescription(),
            folderData.getLevel(),
            folderData.getPath(),
            folderData.getParentId()
        );

        csvPrinter.flush();
      }
    }
  }

  /**
   * Configures HTTP response for CSV file download
   *
   * @param response HTTP response
   * @param folderId ID of the folder to form the filename
   */
  private void configureHttpResponse(HttpServletResponse response, Long folderId) {
    response.setContentType("text/csv");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    var filename = "test_folder_" + folderId + "_export.csv";
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
  }

  /**
   * Recursively collects data about a folder and all its subfolders for export
   *
   * @param folder     Current folder
   * @param result     List to collect data into
   * @param level      Current nesting level (0 for root folder)
   * @param parentPath Path of the parent folder
   */
  private void collectFoldersForExport(
      TmsTestFolder folder,
      List<FolderExportData> result,
      int level,
      String parentPath) {
    var path = level == 0 ? folder.getName() : parentPath + " -> " + folder.getName();

    result.add(new FolderExportData(
        folder.getId(),
        folder.getName(),
        folder.getDescription(),
        level,
        path,
        folder.getParentTestFolder() != null ? folder.getParentTestFolder().getId() : null
    ));

    if (folder.getSubFolders() != null) {
      for (var subFolder : folder.getSubFolders()) {
        collectFoldersForExport(subFolder, result, level + 1, path);
      }
    }
  }

  /**
   * Helper class to store folder data for export
   */
  @Data
  @AllArgsConstructor
  @Builder
  private static class FolderExportData {

    private final Long id;
    private final String name;
    private final String description;
    private final int level;
    private final String path;
    private final Long parentId;
  }
}
