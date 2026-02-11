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

package com.epam.reportportal.base.ws.rabbit.activity.converter;

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.EMAIL_CASES;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.EMPTY_FIELD;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.events.domain.NotificationsConfigUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.base.model.project.ProjectResource;
import com.epam.reportportal.base.model.project.email.ProjectNotificationConfigDTO;
import com.epam.reportportal.base.model.project.email.SenderCaseDTO;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Converter for NotificationsConfigUpdatedEvent to Activity.
 *
 */
@Component
public class NotificationsConfigUpdatedEventConverter implements
    EventToActivityConverter<NotificationsConfigUpdatedEvent> {

  @Override
  public Activity convert(NotificationsConfigUpdatedEvent event) {
    ProjectResource before = event.getBefore();
    ActivityDetails details = new ActivityDetails();
    processEmailConfiguration(details, before, event.getUpdateProjectNotificationConfigRq());

    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_PROJECT.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(before.getProjectId())
        .addObjectName(StringUtils.EMPTY)
        .addObjectType(EventObject.EMAIL_CONFIG)
        .addProjectId(before.getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addDetails(details)
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }

  private void processEmailConfiguration(ActivityDetails details, ProjectResource project,
      ProjectNotificationConfigDTO updateProjectNotificationConfigRQ) {
    ofNullable(project.getConfiguration().getProjectConfig()).ifPresent(cfg -> {
      List<SenderCaseDTO> before = ofNullable(cfg.getSenderCases()).orElseGet(
          Collections::emptyList);

      boolean isEmailCasesChanged = !before.equals(
          updateProjectNotificationConfigRQ.getSenderCases());

      if (!isEmailCasesChanged) {
        details.addHistoryField(HistoryField.of(EMAIL_CASES, EMPTY_FIELD, EMPTY_FIELD));
      } else {
        details.addHistoryField(HistoryField.of(
            EMAIL_CASES,
            before.stream().map(SenderCaseDTO::toString).collect(Collectors.joining(", ")),
            updateProjectNotificationConfigRQ.getSenderCases()
                .stream()
                .map(SenderCaseDTO::toString)
                .collect(Collectors.joining(", "))
        ));
      }
    });
  }

  @Override
  public Class<NotificationsConfigUpdatedEvent> getEventClass() {
    return NotificationsConfigUpdatedEvent.class;
  }
}

