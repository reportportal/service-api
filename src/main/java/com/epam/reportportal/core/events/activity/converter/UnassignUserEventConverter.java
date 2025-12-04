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

package com.epam.reportportal.core.events.activity.converter;

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;
import static com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction.UNASSIGN_USER;

import com.epam.reportportal.core.events.domain.UnassignUserEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

/**
 * Converter for UnassignUserEvent to Activity.
 *
 */
@Component
public class UnassignUserEventConverter implements EventToActivityConverter<UnassignUserEvent> {

  @Override
  public Activity convert(UnassignUserEvent event) {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UNASSIGN)
        .addEventName(UNASSIGN_USER.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(event.getUserActivityResource().getId())
        .addObjectName(event.getUserActivityResource().getFullName())
        .addObjectType(EventObject.USER)
        .addProjectId(event.getUserActivityResource().getDefaultProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.isSystemEvent() ? null : event.getUserId())
        .addSubjectName(event.isSystemEvent() ? RP_SUBJECT_NAME : event.getUserLogin())
        .addSubjectType(event.isSystemEvent() ? EventSubject.APPLICATION : EventSubject.USER)
        .get();
  }

  @Override
  public Class<UnassignUserEvent> getEventClass() {
    return UnassignUserEvent.class;
  }
}

