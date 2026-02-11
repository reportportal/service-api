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

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectName;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectType;

import com.epam.reportportal.base.core.events.domain.LaunchFinishedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import org.springframework.stereotype.Component;

/**
 * Converter for LaunchFinishedEvent to Activity.
 *
 */
@Component
public class LaunchFinishedEventConverter implements EventToActivityConverter<LaunchFinishedEvent> {

  @Override
  public Activity convert(LaunchFinishedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.FINISH)
        .addEventName(ActivityAction.FINISH_LAUNCH.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getId())
        .addObjectName(event.getName())
        .addObjectType(EventObject.LAUNCH)
        .addProjectId(event.getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.isSystemEvent() ? null : event.getUserId())
        .addSubjectName(getSubjectName(event))
        .addSubjectType(getSubjectType(event))
        .get();
  }

  @Override
  public Class<LaunchFinishedEvent> getEventClass() {
    return LaunchFinishedEvent.class;
  }
}
