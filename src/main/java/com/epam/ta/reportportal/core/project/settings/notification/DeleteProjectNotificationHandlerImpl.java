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
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.NotificationsConfigUpdatedEvent;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.model.project.ProjectResource;
import com.epam.ta.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
public class DeleteProjectNotificationHandlerImpl implements DeleteProjectNotificationHandler {

  private final SenderCaseRepository senderCaseRepository;
  private final MessageBus messageBus;
  private final ProjectConverter projectConverter;

  public DeleteProjectNotificationHandlerImpl(SenderCaseRepository senderCaseRepository,
      MessageBus messageBus, ProjectConverter projectConverter) {
    this.senderCaseRepository = senderCaseRepository;
    this.messageBus = messageBus;
    this.projectConverter = projectConverter;
  }

  @Override
  public OperationCompletionRS deleteNotification(Project project, Long notificationId,
      ReportPortalUser user) {
    Optional<SenderCase> senderCase = senderCaseRepository.findById(notificationId);
    expect(senderCase, (notification) -> notification.map(
        ntf -> Objects.equals(ntf.getProject().getId(), project.getId())).orElse(false)).verify(
        ErrorType.BAD_REQUEST_ERROR, Suppliers.formattedSupplier(
                "Notification '{}' not found. Did you use correct Notification ID?", notificationId)
            .get());
    senderCaseRepository.deleteSenderCaseById(notificationId);

    ProjectResource projectResource = projectConverter.TO_PROJECT_RESOURCE.apply(project);
    ProjectNotificationConfigDTO projectNotificationConfigDTO =
        projectResource.getConfiguration().getProjectConfig();
    ofNullable(projectNotificationConfigDTO.getSenderCases()).ifPresent(
        scs -> projectNotificationConfigDTO.setSenderCases(
            scs.stream().filter(sc -> !Objects.equals(sc.getId(), notificationId))
                .collect(Collectors.toList())));

    messageBus.publishActivity(new NotificationsConfigUpdatedEvent(projectResource,
        projectResource.getConfiguration().getProjectConfig(), user.getUserId(), user.getUsername()
    ));

    return new OperationCompletionRS("Notification rule was deleted successfully.");
  }
}
