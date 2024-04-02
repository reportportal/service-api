/*
 *
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
 *
 */

package com.epam.ta.reportportal.core.project.settings.notification;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.NotificationsConfigUpdatedEvent;
import com.epam.ta.reportportal.core.project.validator.notification.ProjectNotificationValidator;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.model.project.ProjectResource;
import com.epam.ta.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.converter.converters.NotificationConfigConverter;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
public class UpdateProjectNotificationHandlerImpl implements UpdateProjectNotificationHandler {

  private final SenderCaseRepository senderCaseRepository;
  private final MessageBus messageBus;
  private final ProjectConverter projectConverter;
  private final ProjectNotificationValidator projectNotificationValidator;

  public UpdateProjectNotificationHandlerImpl(SenderCaseRepository senderCaseRepository,
      MessageBus messageBus, ProjectConverter projectConverter,
      ProjectNotificationValidator projectNotificationValidator) {
    this.senderCaseRepository = senderCaseRepository;
    this.messageBus = messageBus;
    this.projectConverter = projectConverter;
    this.projectNotificationValidator = projectNotificationValidator;
  }

  @Override
  public OperationCompletionRS updateNotification(Project project,
      SenderCaseDTO updateNotificationRQ, ReportPortalUser user) {
    expect(updateNotificationRQ.getId(), Objects::nonNull).verify(ErrorType.BAD_REQUEST_ERROR,
        "Please specify notification Id"
    );
    expect(senderCaseRepository.findById(updateNotificationRQ.getId()),
        (notification) -> notification.map(
            ntf -> Objects.equals(ntf.getProject().getId(), project.getId())).orElse(false)
    ).verify(ErrorType.BAD_REQUEST_ERROR, Suppliers.formattedSupplier(
        "Notification '{}' not found. Did you use correct Notification ID?",
        updateNotificationRQ.getId()
    ).get());
    projectNotificationValidator.validateUpdateRQ(project, updateNotificationRQ);
    SenderCase notification = NotificationConfigConverter.TO_CASE_MODEL.apply(updateNotificationRQ);
    notification.setProject(project);
    senderCaseRepository.save(notification);

    ProjectResource projectResource = projectConverter.TO_PROJECT_RESOURCE.apply(project);
    ProjectNotificationConfigDTO projectNotificationConfigDTO =
        projectResource.getConfiguration().getProjectConfig();
    projectNotificationConfigDTO.getSenderCases().add(updateNotificationRQ);

    messageBus.publishActivity(new NotificationsConfigUpdatedEvent(projectResource,
        projectResource.getConfiguration().getProjectConfig(), user.getUserId(), user.getUsername()
    ));

    return new OperationCompletionRS("Notification rule was updated successfully.");
  }
}
