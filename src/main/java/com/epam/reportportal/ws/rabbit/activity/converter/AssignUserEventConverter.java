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

import static com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction.ASSIGN_USER;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectName;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectType;

import com.epam.reportportal.core.events.domain.AssignUserEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import org.springframework.stereotype.Component;

/**
 * Converter for AssignUserEvent to Activity.
 *
 */
@Component
public class AssignUserEventConverter implements EventToActivityConverter<AssignUserEvent> {

  @Override
  public Activity convert(AssignUserEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.ASSIGN)
        .addEventName(ASSIGN_USER.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectId(event.getUserActivityResource().getId())
        .addObjectName(event.getUserActivityResource().getFullName())
        .addObjectType(EventObject.USER)
        .addProjectId(event.getUserActivityResource().getDefaultProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.isSystemEvent() ? null : event.getUserId())
        .addSubjectName(getSubjectName(event))
        .addSubjectType(getSubjectType(event))
        .get();
  }

  @Override
  public Class<AssignUserEvent> getEventClass() {
    return AssignUserEvent.class;
  }
}
