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

package com.epam.reportportal.base.core.integration.plugin.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.events.domain.ImportFinishedEvent;
import com.epam.reportportal.base.core.integration.ExecuteIntegrationHandler;
import com.epam.reportportal.base.core.integration.plugin.ImportPluginCommandHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.launch.LaunchImportRQ;
import com.epam.reportportal.base.util.ProjectExtractor;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default implementation of {@link ImportPluginCommandHandler}.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
@RequiredArgsConstructor
public class ImportPluginCommandHandlerImpl implements ImportPluginCommandHandler {

  private static final String IMPORT_COMMAND = "import";

  private final ExecuteIntegrationHandler executeIntegrationHandler;
  private final ProjectExtractor projectExtractor;
  private final LaunchRepository launchRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public Object execute(ReportPortalUser user, String projectKey, String pluginName,
      MultipartFile file, LaunchImportRQ launchImportRq) {
    LaunchImportRQ importRq = ofNullable(launchImportRq).orElseGet(LaunchImportRQ::new);
    MembershipDetails membershipDetails = projectExtractor.extractMembershipDetails(user, projectKey);

    validateTargetLaunch(importRq, membershipDetails, user);

    Map<String, Object> executionParams = new HashMap<>();
    executionParams.put("file", file);
    executionParams.put(ENTITY_PARAM, importRq);
    executionParams.put("projectName", membershipDetails.getProjectKey());

    PluginCommandRQ pluginCommandRq = new PluginCommandRQ()
        .context(new PluginCommandContext(membershipDetails.getOrgId(), membershipDetails.getProjectId(), null))
        .arguments(executionParams);

    var importResult = executeIntegrationHandler.executeExtensionCommand(pluginName, IMPORT_COMMAND, pluginCommandRq);
    applicationEventPublisher.publishEvent(new ImportFinishedEvent(user.getUserId(),
        user.getUsername(),
        membershipDetails.getProjectId(),
        file.getOriginalFilename(),
        membershipDetails.getOrgId()
    ));

    return importResult;
  }

  private void validateTargetLaunch(LaunchImportRQ launchImportRq,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    if (StringUtils.isBlank(launchImportRq.getLaunchUuid())) {
      return;
    }

    Launch launch = launchRepository.findByUuid(launchImportRq.getLaunchUuid())
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND,
            launchImportRq.getLaunchUuid()));

    if (user.getUserRole() == UserRole.ADMINISTRATOR) {
      return;
    }

    expect(launch.getProjectId(), equalTo(membershipDetails.getProjectId())).verify(ACCESS_DENIED,
        "Target launch is not under specified project.");

    if (OrganizationRole.MANAGER.equals(membershipDetails.getOrgRole())) {
      return;
    }

    if (membershipDetails.getProjectRole().lowerThan(ProjectRole.EDITOR)) {
      expect(launch.getUserId(), equalTo(user.getUserId())).verify(ACCESS_DENIED,
          "You are not launch owner.");
    }
  }
}
