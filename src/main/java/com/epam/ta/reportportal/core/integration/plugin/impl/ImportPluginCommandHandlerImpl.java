/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.entity.user.UserRole.ADMINISTRATOR;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ImportFinishedEvent;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.plugin.ImportPluginCommandHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.ta.reportportal.util.ProjectExtractor;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default implementation of {@link ImportPluginCommandHandler}.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ImportPluginCommandHandlerImpl implements ImportPluginCommandHandler {

  private static final String IMPORT_COMMAND = "import";

  private final ExecuteIntegrationHandler executeIntegrationHandler;
  private final ProjectExtractor projectExtractor;
  private final LaunchRepository launchRepository;
  private final MessageBus messageBus;

  public ImportPluginCommandHandlerImpl(ExecuteIntegrationHandler executeIntegrationHandler,
      ProjectExtractor projectExtractor, LaunchRepository launchRepository, MessageBus messageBus) {
    this.executeIntegrationHandler = executeIntegrationHandler;
    this.projectExtractor = projectExtractor;
    this.launchRepository = launchRepository;
    this.messageBus = messageBus;
  }

  @Override
  public Object execute(ReportPortalUser user, String projectName, String pluginName,
      MultipartFile file, LaunchImportRQ launchImportRq) {
    LaunchImportRQ importRq = ofNullable(launchImportRq).orElseGet(LaunchImportRQ::new);
    ReportPortalUser.ProjectDetails projectDetails =
        projectExtractor.extractProjectDetails(user, projectName);

    validateTargetLaunch(importRq, projectDetails, user);

    Map<String, Object> executionParams = new HashMap<>();
    executionParams.put("file", file);
    executionParams.put(ENTITY_PARAM, importRq);

    Object importResult = executeIntegrationHandler.executeCommand(projectDetails, pluginName,
        IMPORT_COMMAND, executionParams);
    messageBus.publishActivity(new ImportFinishedEvent(user.getUserId(), user.getUsername(),
        projectDetails.getProjectId(), file.getOriginalFilename()
    ));
    return importResult;
  }

  private void validateTargetLaunch(LaunchImportRQ launchImportRq,
      ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
    if (StringUtils.isBlank(launchImportRq.getLaunchUuid())) {
      return;
    }

    Launch launch = launchRepository.findByUuid(launchImportRq.getLaunchUuid())
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND,
            launchImportRq.getLaunchUuid()));

    if (user.getUserRole() == ADMINISTRATOR) {
      return;
    }

    expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED,
        "Target launch is not under specified project.");

    if (projectDetails.getProjectRole().lowerThan(PROJECT_MANAGER)) {
      expect(launch.getUserId(), equalTo(user.getUserId())).verify(ACCESS_DENIED,
          "You are not launch owner.");
    }
  }
}
