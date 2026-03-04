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

package com.epam.reportportal.base.core.project.settings.notification;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;

import com.epam.reportportal.base.core.events.domain.NotificationRuleUpdatedEvent;
import com.epam.reportportal.base.core.project.validator.notification.ProjectNotificationValidator;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.SenderCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.email.SenderCase;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.model.project.ProjectResource;
import com.epam.reportportal.base.model.project.email.ProjectNotificationConfigDTO;
import com.epam.reportportal.base.model.project.email.SenderCaseDTO;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.base.ws.converter.converters.NotificationConfigConverter;
import com.epam.reportportal.base.ws.converter.converters.NotificationRuleConverter;
import com.epam.reportportal.base.ws.converter.converters.ProjectConverter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
@RequiredArgsConstructor
public class UpdateProjectNotificationHandlerImpl implements UpdateProjectNotificationHandler {

  private final SenderCaseRepository senderCaseRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ProjectConverter projectConverter;
  private final ProjectNotificationValidator projectNotificationValidator;

  @Override
  public OperationCompletionRS updateNotification(Project project,
      SenderCaseDTO updateNotificationRQ, ReportPortalUser user) {
    expect(updateNotificationRQ.getId(), Objects::nonNull).verify(ErrorType.BAD_REQUEST_ERROR,
        "Please specify notification Id"
    );
    var beforeOptional = senderCaseRepository.findById(updateNotificationRQ.getId());
    expect(beforeOptional, (notification) -> notification.map(
        ntf -> Objects.equals(ntf.getProject().getId(), project.getId())).orElse(false)
    ).verify(ErrorType.BAD_REQUEST_ERROR, Suppliers.formattedSupplier(
        "Notification '{}' not found. Did you use correct Notification ID?",
        updateNotificationRQ.getId()
    ).get());
    projectNotificationValidator.validateUpdateRQ(project, updateNotificationRQ);

    ProjectResource projectResource = projectConverter.TO_PROJECT_RESOURCE.apply(project);
    ProjectNotificationConfigDTO projectNotificationConfigDTO =
        projectResource.getConfiguration().getProjectConfig();

    var beforeActivity = NotificationRuleConverter.TO_ACTIVITY_RESOURCE.apply(
        beforeOptional.orElseThrow());

    SenderCase notification = NotificationConfigConverter.TO_CASE_MODEL.apply(updateNotificationRQ);
    notification.setProject(project);
    SenderCase saved = senderCaseRepository.save(notification);
    projectNotificationConfigDTO.getSenderCases().add(updateNotificationRQ);

    var afterActivity = NotificationRuleConverter.TO_ACTIVITY_RESOURCE.apply(saved);

    eventPublisher.publishEvent(
        new NotificationRuleUpdatedEvent(beforeActivity, afterActivity, user.getUserId(),
            user.getUsername(),
            project.getOrganizationId()));

    return new OperationCompletionRS("Notification rule was updated successfully.");
  }
}
