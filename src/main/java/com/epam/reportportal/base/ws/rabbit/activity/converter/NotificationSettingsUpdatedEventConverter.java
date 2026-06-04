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

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_ENABLED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_SLACK_ENABLED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_TELEGRAM_ENABLED;

import com.epam.reportportal.base.core.events.domain.NotificationSettingsUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Converter for NotificationSettingsUpdatedEvent to Activity.
 *
 */
@Component
public class NotificationSettingsUpdatedEventConverter
    implements EventToActivityConverter<NotificationSettingsUpdatedEvent> {

  @Override
  public Activity convert(NotificationSettingsUpdatedEvent event) {
    final ActivityBuilder activityBuilder = new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_NOTIFICATION_SETTINGS.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(event.getBefore().getProjectId())
        .addObjectName("notifications_settings")
        .addObjectType(EventObject.NOTIFICATION_RULE)
        .addProjectId(event.getBefore().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER);

    Stream.of(NOTIFICATIONS_ENABLED, NOTIFICATIONS_EMAIL_ENABLED, NOTIFICATIONS_TELEGRAM_ENABLED,
            NOTIFICATIONS_SLACK_ENABLED)
        .map(type -> processParameter(event.getBefore().getConfig(), event.getAfter().getConfig(),
            type.getAttribute()))
        .forEach(activityBuilder::addHistoryField);

    return activityBuilder.get();
  }

  @Override
  public Class<NotificationSettingsUpdatedEvent> getEventClass() {
    return NotificationSettingsUpdatedEvent.class;
  }
}

