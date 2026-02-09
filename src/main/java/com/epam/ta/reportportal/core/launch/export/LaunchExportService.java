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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
        dataProvider.getTestItemsOfLaunch(launch, false).values(),
        username,
        format);

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
   * @param launch       the launch to export
   * @param username     the username (used as fallback if owner's name is not found)
   * @param reportFormat the format of the report inside the ZIP archive
   * @param response     the HTTP response to write the ZIP archive to
   */
  public void exportLaunchWithAttachments(Launch launch, String username, String reportFormat,
      HttpServletResponse response) {
    ReportFormat format = reportService.resolveFormat(reportFormat);
    prepareZipResponse(launch, response);
    try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
      Map<Long, TestItemPojo> testItems = dataProvider.getTestItemsOfLaunch(launch, true);
      Set<String> uniquePaths = new HashSet<>();
      writeTestItemAttachmentsToZip(launch, testItems, uniquePaths, zipOut);
      writeLogAttachmentsToZip(launch, uniquePaths, zipOut);
      writeReportToZip(launch, testItems.values(), username, format, zipOut);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to export ZIP", e);
    }
  }

  private void prepareZipResponse(Launch launch, HttpServletResponse response) {
    response.setContentType("application/zip");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"%s_%s.zip\"", launch.getName(), launch.getNumber()));
  }

  private void writeTestItemAttachmentsToZip(Launch launch, Map<Long, TestItemPojo> testItems,
      Set<String> uniquePaths, ZipOutputStream zipOut) {
    for (TestItemPojo item : testItems.values()) {
      String itemPath = pathService.buildItemPath(testItems, item);
      writeItemAttachmentsToZip(item, itemPath, uniquePaths, zipOut);
      if (!item.isHasChildren()) {
        writeNestedStepAttachmentsToZip(launch.getId(), item, itemPath, uniquePaths, zipOut);
      }
    }
  }

  private void writeItemAttachmentsToZip(TestItemPojo item, String itemPath,
      Set<String> uniquePaths,
      ZipOutputStream zipOut) {
    if (CollectionUtils.isEmpty(item.getAttachmentPojoList())) {
      return;
    }
    for (AttachmentPojo attachment : item.getAttachmentPojoList()) {
      String zipPath = buildAttachmentZipPath(itemPath, attachment.getFileName(),
          attachment.getContentType());
      writeToZipIfUnique(attachment.getFileId(), zipPath, uniquePaths, zipOut);
    }
  }

  private void writeNestedStepAttachmentsToZip(Long launchId, TestItemPojo item, String itemPath,
      Set<String> uniquePaths, ZipOutputStream zipOut) {
    List<NestedItemAttachment> nestedAttachments = dataProvider.getNestedStepsAttachments(launchId,
        item.getPath(), item.getId());
    if (CollectionUtils.isEmpty(nestedAttachments)) {
      return;
    }

    for (NestedItemAttachment nestedAttachment : nestedAttachments) {
      String zipPath = buildAttachmentZipPath(itemPath, nestedAttachment.getFileName(),
          nestedAttachment.getContentType());
      if (writeToZipIfUnique(nestedAttachment.getFileId(), zipPath, uniquePaths, zipOut)) {
        appendZipFileNameToItemType(item, zipPath);
      }
    }
  }

  private void writeLogAttachmentsToZip(Launch launch, Set<String> uniquePaths,
      ZipOutputStream zipOut) {
    if (launch.getLogs() == null) {
      return;
    }
    for (Log log : launch.getLogs()) {
      if (log.getAttachment() == null) {
        continue;
      }
      String fileNameWithExtension = FileExtensionUtils.getFileNameWithExtension(
          log.getAttachment().getFileName(),
          log.getAttachment().getContentType()
      );
      writeToZipIfUnique(log.getAttachment().getFileId(), fileNameWithExtension, uniquePaths,
          zipOut);
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

  private String buildAttachmentZipPath(String basePath, String fileName, String contentType) {
    String fileNameWithExtension = FileExtensionUtils.getFileNameWithExtension(fileName,
        contentType);
    return pathService.buildAttachmentPath(basePath, fileNameWithExtension);
  }

  private boolean writeToZipIfUnique(String fileId, String zipPath, Set<String> uniquePaths,
      ZipOutputStream zipOut) {
    if (!uniquePaths.add(zipPath)) {
      return false;
    }
    zipService.writeToZip(fileId, zipPath, zipOut);
    return true;
  }

  private void appendZipFileNameToItemType(TestItemPojo item, String zipPath) {
    String zipFileName = zipPath.substring(zipPath.lastIndexOf('/') + 1);
    item.setType(item.getType() + "\n" + zipFileName);
  }
}
