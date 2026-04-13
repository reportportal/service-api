/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.launch.export;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.item.NestedItemAttachment;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Component responsible for exporting launch reports, either as a single file (PDF/HTML/XLS) or as
 * a ZIP archive that includes attachments.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
@RequiredArgsConstructor
public class LaunchExportService {

  private final JasperDataProvider dataProvider;
  private final LaunchReportService reportService;
  private final AttachmentZipService zipService;
  private final PathBuilderService pathService;

  public void exportLaunch(Launch launch, String username, String reportFormat,
      HttpServletResponse response) {
    ReportFormat format = reportService.resolveFormat(reportFormat);
    byte[] report = reportService.generateReport(launch,
        dataProvider.getTestItemsOfLaunch(launch, false).values(), username, format);

    response.setContentType(format.getContentType());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"%s_%s.%s\"", launch.getName(), launch.getNumber(),
            format.getValue()));

    try (OutputStream out = response.getOutputStream()) {
      out.write(report);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to write report", e);
    }
  }

  /**
   * Exports a ZIP archive containing the launch report and all related attachments.
   *
   * @param launch          the launch to export
   * @param username        the username (used as fallback if owner's name is not found)
   * @param reportFormat    the format of the report inside the ZIP archive
   * @param flatAttachments if {@code true}, all attachments are placed in a flat
   *                        {@code attachments/} directory named {@code <id>_<name>.ext}; if
   *                        {@code false}, attachments are organized in a hierarchical directory
   *                        structure
   * @param response        the HTTP response to write the ZIP archive to
   */
  public void exportLaunchWithAttachments(Launch launch, String username, String reportFormat,
      boolean flatAttachments, HttpServletResponse response) {
    ReportFormat format = reportService.resolveFormat(reportFormat);
    prepareZipResponseHeaders(launch, response);

    try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
      Map<Long, TestItemPojo> testItems = dataProvider.getTestItemsOfLaunch(launch, true,
          flatAttachments);

      writeTestItemsAttachmentsToZip(launch, testItems, flatAttachments, zipOut);
      writeLaunchLogAttachmentsToZip(launch, flatAttachments, zipOut);
      writeReportToZip(launch, testItems.values(), username, format, zipOut);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to export ZIP", e);
    }
  }

  private void prepareZipResponseHeaders(Launch launch, HttpServletResponse response) {
    response.setContentType("application/zip");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"%s_%s.zip\"", launch.getName(), launch.getNumber()));
  }

  private Map<Long, String> buildIdNameMapping(Map<Long, TestItemPojo> testItems) {
    return testItems.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getItemName()));
  }

  private void writeTestItemsAttachmentsToZip(Launch launch, Map<Long, TestItemPojo> testItems,
      boolean flatAttachments, ZipOutputStream zipOut) throws IOException {
    Map<Long, String> idNameMapping = buildIdNameMapping(testItems);
    Set<String> uniquePaths = new HashSet<>();
    for (TestItemPojo item : testItems.values()) {
      String itemPathNames =
          flatAttachments ? null : pathService.buildItemPath(idNameMapping, item.getPath());
      writeItemAttachmentsToZip(item, itemPathNames, flatAttachments, uniquePaths, zipOut);

      if (item.isHasChildren()) {
        continue;
      }
      writeNestedStepsAttachmentsToZip(launch, item, idNameMapping, flatAttachments, uniquePaths,
          zipOut);
    }
  }

  private void writeItemAttachmentsToZip(TestItemPojo item, String itemPathNames,
      boolean flatAttachments, Set<String> uniquePaths, ZipOutputStream zipOut) {
    if (item.getAttachmentPojoList() == null) {
      return;
    }
    for (AttachmentPojo attachment : item.getAttachmentPojoList()) {
      String attachmentFullName =
          attachment.getId() + "_" + FileExtensionUtils.getFileNameWithExtension(
              attachment.getFileName(), attachment.getContentType());
      String fullPath = flatAttachments ? buildFlatAttachmentPath(attachmentFullName)
          : pathService.buildAttachmentPath(itemPathNames, attachmentFullName);
      writeToZipIfUnique(attachment.getFileId(), fullPath, uniquePaths, zipOut);
    }
  }

  private void writeNestedStepsAttachmentsToZip(Launch launch, TestItemPojo item,
      Map<Long, String> idNameMapping, boolean flatAttachments, Set<String> uniquePaths,
      ZipOutputStream zipOut) {
    List<NestedItemAttachment> nestedAttachments = dataProvider.getNestedStepsAttachments(
        launch.getId(), item.getPath(), item.getId());
    if (CollectionUtils.isEmpty(nestedAttachments)) {
      return;
    }

    nestedAttachments.forEach(it -> idNameMapping.putIfAbsent(it.getItemId(), it.getName()));

    for (NestedItemAttachment attachment : nestedAttachments) {
      if (!StringUtils.hasText(attachment.getFileId())) {
        continue;
      }
      String attachmentFullName =
          attachment.getAttachmentId() + "_" + FileExtensionUtils.getFileNameWithExtension(
              attachment.getFileName(), attachment.getContentType());
      String fullPath;
      if (flatAttachments) {
        fullPath = buildFlatAttachmentPath(attachmentFullName);
      } else {
        String nestedItemPathNames = pathService.buildItemPath(idNameMapping, attachment.getPath());
        fullPath = pathService.buildAttachmentPath(nestedItemPathNames, attachmentFullName);
      }

      if (writeToZipIfUnique(attachment.getFileId(), fullPath, uniquePaths, zipOut)) {
        appendZipFileNameToItemType(item, fullPath);
      }
    }
  }

  private void writeLaunchLogAttachmentsToZip(Launch launch, boolean flatAttachments,
      ZipOutputStream zipOut) throws IOException {
    Set<String> uniquePaths = new HashSet<>();
    for (Log log : launch.getLogs()) {
      if (log.getAttachment() == null) {
        continue;
      }
      String attachmentFullName =
          log.getAttachment().getId() + "_" + FileExtensionUtils.getFileNameWithExtension(
              log.getAttachment().getFileName(), log.getAttachment().getContentType());
      String path =
          flatAttachments ? buildFlatAttachmentPath(attachmentFullName) : attachmentFullName;
      writeToZipIfUnique(log.getAttachment().getFileId(), path, uniquePaths, zipOut);
    }
  }

  private void writeReportToZip(Launch launch, Collection<TestItemPojo> testItems, String username,
      ReportFormat format, ZipOutputStream zipOut) throws IOException {
    ZipEntry reportEntry = new ZipEntry(
        String.format("%s_%s.%s", launch.getName(), launch.getNumber(), format.getValue()));
    zipOut.putNextEntry(reportEntry);
    byte[] reportBytes = reportService.generateReport(launch, testItems, username, format);
    zipOut.write(reportBytes);
    zipOut.closeEntry();
  }

  private boolean writeToZipIfUnique(String fileId, String path, Set<String> uniquePaths,
      ZipOutputStream zipOut) {
    if (uniquePaths.add(path)) {
      zipService.writeToZip(fileId, path, zipOut);
      return true;
    }
    return false;
  }

  private static final String FLAT_ATTACHMENTS_DIR = "attachments/";

  private String buildFlatAttachmentPath(String fileNameWithExtension) {
    return FLAT_ATTACHMENTS_DIR + fileNameWithExtension;
  }

  private void appendZipFileNameToItemType(TestItemPojo item, String fullPath) {
    String zipFileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
    item.setType(item.getType() + "\n" + zipFileName);
  }
}
