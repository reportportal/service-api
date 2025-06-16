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
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.constants.LaunchReportConstants;
import com.epam.ta.reportportal.core.jasper.util.JasperDataProvider;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.User;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class LaunchExportHandler {

  private final GetJasperReportHandler<Launch> jasperReportHandler;
  private final UserRepository userRepository;
  private final JasperDataProvider dataProvider;


  public LaunchExportHandler(UserRepository userRepository, JasperDataProvider jasperDataProvider,
      @Qualifier("launchJasperReportHandler") GetJasperReportHandler<Launch> jasperReportHandler) {
    this.userRepository = userRepository;
    this.jasperReportHandler = jasperReportHandler;
    this.dataProvider = jasperDataProvider;
  }


  public void exportLaunch(Launch launch, String username, String reportFormat, HttpServletResponse response) {
    var format = jasperReportHandler.getReportFormat(reportFormat);
    var reportOutput = prepareReportBytes(launch, username, format);
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
    var format = jasperReportHandler.getReportFormat(reportFormat);
    var reportOutput = prepareReportBytes(launch, username, format);
    response.setHeader("Content-Disposition", "attachment; filename=report.zip");

    try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
      ZipEntry reportEntry = new ZipEntry(String.format("RP_LAUNCH_Report.%s", format.name()));
      zipOut.putNextEntry(reportEntry);
      zipOut.write(reportOutput);
      zipOut.closeEntry();
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Unable to write data to the response."
      );
    }
  }

  private byte[] prepareReportBytes(Launch launch, String username, ReportFormat reportFormat) {
    var params = jasperReportHandler.convertParams(launch);
    fillWithAdditionalParams(params, launch, username);
    var jasperPrint = jasperReportHandler.getJasperPrint(params, new JREmptyDataSource());
    return jasperReportHandler.exportReportBytes(reportFormat, jasperPrint);
  }

  private void fillWithAdditionalParams(Map<String, Object> params, Launch launch,
      String userFullName) {
    var owner = userRepository.findById(launch.getUserId()).map(User::getFullName);
    /* Check if launch owner still in system if not - setup principal */
    params.put(LaunchReportConstants.OWNER, owner.orElse(userFullName));
    params.put(LaunchReportConstants.TEST_ITEMS, dataProvider.getTestItemsOfLaunch(launch));
  }
}
