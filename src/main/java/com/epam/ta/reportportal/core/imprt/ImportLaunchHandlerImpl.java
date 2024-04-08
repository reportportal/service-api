/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.imprt;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.XML_EXTENSION;
import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.ZIP_EXTENSION;
import static com.epam.reportportal.rules.exception.ErrorType.INCORRECT_REQUEST;
import static org.apache.commons.io.FileUtils.ONE_MB;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ImportFinishedEvent;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategy;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategyFactory;
import com.epam.ta.reportportal.core.imprt.impl.ImportType;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.LaunchImportCompletionRS;
import com.epam.ta.reportportal.model.LaunchImportData;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportLaunchHandlerImpl implements ImportLaunchHandler {

  private static final long MAX_FILE_SIZE = 32 * ONE_MB;

  private final ImportStrategyFactory importStrategyFactory;
  private final MessageBus messageBus;
  private final LaunchRepository launchRepository;

  @Autowired
  public ImportLaunchHandlerImpl(ImportStrategyFactory importStrategyFactory, MessageBus messageBus,
      LaunchRepository launchRepository) {
    this.importStrategyFactory = importStrategyFactory;
    this.messageBus = messageBus;
    this.launchRepository = launchRepository;
  }

  @Override
  public OperationCompletionRS importLaunch(ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user, String format, MultipartFile file, String baseUrl, LaunchImportRQ rq) {

    validate(file);
    rq = getBackCompatibleRq(rq);

    ImportType type = ImportType.fromValue(format).orElseThrow(
        () -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Unknown import type - " + format
        ));

    File tempFile = transferToTempFile(file);
    ImportStrategy strategy =
        importStrategyFactory.getImportStrategy(type, file.getOriginalFilename());
    String launchId = strategy.importLaunch(projectDetails, user, tempFile, baseUrl, rq);
    messageBus.publishActivity(
        new ImportFinishedEvent(user.getUserId(), user.getUsername(), projectDetails.getProjectId(),
            file.getOriginalFilename()
        ));
    return prepareLaunchImportResponse(launchId);
  }

  //back compatibility with ui
  private LaunchImportRQ getBackCompatibleRq(LaunchImportRQ rq) {
    return Optional.ofNullable(rq).orElse(new LaunchImportRQ());
  }

  private void validate(MultipartFile file) {
    expect(file.getOriginalFilename(), notNull()).verify(INCORRECT_REQUEST,
        "File name should be not empty."
    );

    expect(file.getOriginalFilename(),
        it -> it.endsWith(ZIP_EXTENSION) || it.endsWith(XML_EXTENSION)
    ).verify(INCORRECT_REQUEST,
        "Should be a zip archive or an xml file " + file.getOriginalFilename()
    );
    expect(file.getSize(), size -> size <= MAX_FILE_SIZE).verify(INCORRECT_REQUEST,
        "File size is more than 32 Mb."
    );
  }

  private File transferToTempFile(MultipartFile file) {
    try {
      File tmp = File.createTempFile(file.getOriginalFilename(),
          "." + FilenameUtils.getExtension(file.getOriginalFilename())
      );
      file.transferTo(tmp);
      return tmp;
    } catch (IOException e) {
      throw new ReportPortalException("Error during transferring multipart file.", e);
    }
  }

  private OperationCompletionRS prepareLaunchImportResponse(String launchId) {

    var launch = launchRepository.findByUuid(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND));

    var data = new LaunchImportData();
    data.setId(launchId);
    data.setName(launch.getName());
    data.setNumber(launch.getNumber());

    var response = new LaunchImportCompletionRS();
    response.setResultMessage("Launch with id = " + launchId + " is successfully imported.");
    response.setData(data);

    return response;
  }
}
