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
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.TestItemPojo;
import com.epam.ta.reportportal.core.jasper.constants.LaunchReportConstants;
import com.epam.ta.reportportal.core.jasper.util.JasperDataProvider;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.ItemPathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.user.User;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class LaunchExportHandler {

  private final GetJasperReportHandler<Launch> jasperReportHandler;
  private final UserRepository userRepository;
  private final JasperDataProvider dataProvider;
  private final TestItemRepository testItemRepository;
  private final DataStoreService dataStoreService;


  public LaunchExportHandler(UserRepository userRepository, JasperDataProvider jasperDataProvider,
      TestItemRepository testItemRepository, @Qualifier("attachmentDataStoreService") DataStoreService dataStoreService,
      @Qualifier("launchJasperReportHandler") GetJasperReportHandler<Launch> jasperReportHandler) {
    this.userRepository = userRepository;
    this.dataStoreService = dataStoreService;
    this.testItemRepository = testItemRepository;
    this.jasperReportHandler = jasperReportHandler;
    this.dataProvider = jasperDataProvider;
  }


  public void exportLaunch(Launch launch, String username, String reportFormat, HttpServletResponse response) {
    var format = jasperReportHandler.getReportFormat(reportFormat);
    var reportOutput = prepareReportBytes(launch, dataProvider.getTestItemsOfLaunch(launch), username, format);
    response.setContentType(format.getContentType());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"RP_LAUNCH_%s_Report.%s\"", format.name(),
            format.getValue()
        )
    );
    try (var outputStream = response.getOutputStream()) {
      outputStream.write(reportOutput);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Unable to write data to the response."
      );
    }
  }

  public void exportLaunchWithAttachments(Launch launch, String username, String reportFormat,
      HttpServletResponse response) {
    ReportFormat format = jasperReportHandler.getReportFormat(reportFormat);
    response.setContentType("application/zip");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"RP_LAUNCH_%s_Report.%s\"", format.name(), "zip"));

    try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()))) {
      var testItemsOfLaunch = dataProvider.getTestItemsOfLaunch(launch);
      testItemsOfLaunch.forEach(item ->
          testItemRepository.findById(item.getId()).ifPresent(testItem -> {
            String basePath = safeBuildItemPath(testItem);
            String attachments = writeAttachmentsStreamed(testItem, basePath, zipOut);
            if (StringUtils.hasText(attachments)) {
              System.out.println(attachments);
              item.setType(item.getType() + "\n" + attachments);
            }
            System.out.println(item.getType());
          })
      );
      launch.getLogs().stream().map(Log::getAttachment).filter(Objects::nonNull)
          .forEach(attachment -> writeAttachmentToZip(attachment.getFileName(), attachment, zipOut));

      writeReportToZip(launch, testItemsOfLaunch, username, format, zipOut);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unable to write data to the response.", e);
    }
  }

  private void writeReportToZip(Launch launch, List<TestItemPojo> testItems, String username, ReportFormat format,
      ZipOutputStream zipOut)
      throws IOException {
    ZipEntry reportEntry = new ZipEntry(String.format("RP_LAUNCH_%S_Report.%s", format.name(), format.getValue()));
    zipOut.putNextEntry(reportEntry);
    byte[] reportBytes = prepareReportBytes(launch, testItems, username, format);
    zipOut.write(reportBytes);
    zipOut.closeEntry();
  }

  private String safeBuildItemPath(TestItem testItem) {
    var pathMap = testItemRepository.selectPathNames(Collections.singletonList(testItem));
    var pathName = pathMap.get(testItem.getItemId());

    if (pathName == null) {
      return "unknown-path";
    }

    return pathName.getItemPaths().stream()
        .sorted(Comparator.comparing(ItemPathName::getId))
        .map(ItemPathName::getName)
        .map(this::sanitizePath)
        .collect(Collectors.joining("/"));
  }

  private String writeAttachmentsStreamed(TestItem testItem, String basePath, ZipOutputStream zipOut) {
    StringBuilder attachments = new StringBuilder();
    for (Log log : testItem.getLogs()) {
      Attachment attachment = log.getAttachment();
      if (attachment == null) {
        continue;
      }
      String fullName = String.format("%s/%s/%s",
          basePath,
          sanitizePath(testItem.getName()),
          sanitizePath(attachment.getFileName())
      );
      writeAttachmentToZip(fullName, attachment, zipOut);
      attachments.append(attachment.getFileName()).append("\n");
    }
    return attachments.toString().trim();
  }

  public void writeAttachmentToZip(String fullName, Attachment attachment, ZipOutputStream zipOut) {
    try (InputStream input = dataStoreService.load(attachment.getFileId()).orElse(null)) {
      if (input == null) {
        return;
      }
      zipOut.putNextEntry(new ZipEntry(fullName));
      byte[] buffer = new byte[8192];
      int len;
      while ((len = input.read(buffer)) != -1) {
        zipOut.write(buffer, 0, len);
      }

      zipOut.closeEntry();


    } catch (IOException e) {
    }
  }

  private String sanitizePath(String raw) {
    if (raw == null || raw.isBlank()) {
      return "unknown";
    }

    return raw
        .replaceAll("[\\\\/:*?\"<>|]", "_")
        .replaceAll("[\\p{Cntrl}]", "")
        .replaceAll("\\s+", " ")
        .trim();
  }

  private byte[] prepareReportBytes(Launch launch, List<TestItemPojo> testItemPojos, String username,
      ReportFormat reportFormat) {
    var params = jasperReportHandler.convertParams(launch);
    var owner = userRepository.findById(launch.getUserId()).map(User::getFullName);
    params.put(LaunchReportConstants.OWNER, owner.orElse(username));
    params.put(LaunchReportConstants.TEST_ITEMS, testItemPojos);
    var jasperPrint = jasperReportHandler.getJasperPrint(params, new JREmptyDataSource());
    return jasperReportHandler.exportReportBytes(reportFormat, jasperPrint);
  }
}
