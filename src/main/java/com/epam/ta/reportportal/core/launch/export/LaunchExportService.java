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
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Component responsible for exporting launch reports, either as a single file (PDF/HTML/XLS) or as a ZIP archive that
 * includes attachments.
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


  public void exportLaunch(Launch launch, String username, String reportFormat, HttpServletResponse response) {
    ReportFormat format = reportService.resolveFormat(reportFormat);
    byte[] report = reportService.generateReport(launch, dataProvider.getTestItemsOfLaunch(launch, false).values(),
        username,
        format);

    response.setContentType(format.getContentType());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"RP_LAUNCH_%s_Report.%s\"", format.name(), format.getValue()));

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
    response.setContentType("application/zip");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"RP_LAUNCH_%s_Report.zip\"", format.name()));

    try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
      Map<Long, TestItemPojo> testItems = dataProvider.getTestItemsOfLaunch(launch, true);

      for (TestItemPojo item : testItems.values()) {
        String itemsPathNames = pathService.buildItemPath(testItems, item);

        Set<String> uniquePaths = new HashSet<>();
        for (AttachmentPojo att : item.getAttachmentPojoList()) {
          String fullPath = pathService.buildAttachmentPath(itemsPathNames, att.getFileName());
          if (uniquePaths.add(fullPath)) {
            zipService.writeToZip(att.getFileId(), fullPath, zipOut);
          }
        }
      }

      for (Log log : launch.getLogs()) {
        if (log.getAttachment() != null) {
          zipService.writeToZip(log.getAttachment().getFileId(), log.getAttachment().getFileName(), zipOut);
        }
      }

      ZipEntry reportEntry = new ZipEntry(String.format("RP_LAUNCH_%S_Report.%s", format.name(), format.getValue()));
      zipOut.putNextEntry(reportEntry);
      byte[] reportBytes = reportService.generateReport(launch, testItems.values(), username, format);
      zipOut.write(reportBytes);
      zipOut.closeEntry();

    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to export ZIP", e);
    }
  }
}
