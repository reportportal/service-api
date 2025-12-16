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

package com.epam.reportportal.ws.rabbit.activity.converter;

import com.epam.reportportal.core.events.domain.LaunchImportanceChangedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

/**
 * Converter for LaunchImportanceChangedEvent to Activity.
 *
 */
@Component
public class LaunchImportanceChangedEventConverter implements
    EventToActivityConverter<LaunchImportanceChangedEvent> {

  @Override
  public Activity convert(LaunchImportanceChangedEvent event) {
    ActivityBuilder builder = new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(event.isImportant()
            ? ActivityAction.MARK_LAUNCH_AS_IMPORTANT.getValue()
            : ActivityAction.UNMARK_LAUNCH_AS_IMPORTANT.getValue())
        .addPriority(event.isImportant() ? EventPriority.MEDIUM : EventPriority.HIGH)
        .addObjectId(event.getLaunchActivityResource().getId())
        .addObjectName(event.getLaunchActivityResource().getName())
        .addObjectType(EventObject.LAUNCH)
        .addProjectId(event.getLaunchActivityResource().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER);

    return builder.get();
  }

  @Override
  public Class<LaunchImportanceChangedEvent> getEventClass() {
    return LaunchImportanceChangedEvent.class;
  }
}

