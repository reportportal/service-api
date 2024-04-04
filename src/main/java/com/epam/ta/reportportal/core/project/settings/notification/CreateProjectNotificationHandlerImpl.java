/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.project.settings.notification;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.NotificationsConfigUpdatedEvent;
import com.epam.ta.reportportal.core.project.validator.notification.ProjectNotificationValidator;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.project.ProjectResource;
import com.epam.ta.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.converter.converters.NotificationConfigConverter;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
public class CreateProjectNotificationHandlerImpl implements CreateProjectNotificationHandler {

  private final SenderCaseRepository senderCaseRepository;
  private final MessageBus messageBus;
  private final ProjectConverter projectConverter;
  private final ProjectNotificationValidator projectNotificationValidator;

  public CreateProjectNotificationHandlerImpl(SenderCaseRepository senderCaseRepository,
      MessageBus messageBus, ProjectConverter projectConverter,
      ProjectNotificationValidator projectNotificationValidator) {
    this.senderCaseRepository = senderCaseRepository;
    this.messageBus = messageBus;
    this.projectConverter = projectConverter;
    this.projectNotificationValidator = projectNotificationValidator;
  }

  @Override
  public EntryCreatedRS createNotification(Project project, SenderCaseDTO createNotificationRQ,
      ReportPortalUser user) {
    expect(senderCaseRepository.findByProjectIdAndRuleNameIgnoreCase(project.getId(),
        createNotificationRQ.getRuleName()
    ), Optional::isEmpty).verify(
        ErrorType.RESOURCE_ALREADY_EXISTS, createNotificationRQ.getRuleName());

    projectNotificationValidator.validateCreateRQ(project, createNotificationRQ);

    SenderCase senderCase = NotificationConfigConverter.TO_CASE_MODEL.apply(createNotificationRQ);
    senderCase.setId(null);
    senderCase.setProject(project);
    senderCaseRepository.save(senderCase);

    ProjectResource projectResource = projectConverter.TO_PROJECT_RESOURCE.apply(project);
    ProjectNotificationConfigDTO projectNotificationConfigDTO =
        projectResource.getConfiguration().getProjectConfig();
    projectNotificationConfigDTO.getSenderCases().add(createNotificationRQ);

    messageBus.publishActivity(new NotificationsConfigUpdatedEvent(projectResource,
        projectResource.getConfiguration().getProjectConfig(), user.getUserId(), user.getUsername()
    ));

    return new EntryCreatedRS(senderCase.getId());
  }

}
