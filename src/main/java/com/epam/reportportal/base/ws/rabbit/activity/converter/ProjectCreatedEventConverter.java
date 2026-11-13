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

import com.epam.reportportal.base.core.events.domain.ProjectCreatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Converter for ProjectCreatedEvent to Activity.
 *
 */
@Component
public class ProjectCreatedEventConverter implements EventToActivityConverter<ProjectCreatedEvent> {

  @Override
  public Activity convert(ProjectCreatedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.CREATE)
        .addEventName(ActivityAction.CREATE_PROJECT.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(event.getProjectId())
        .addObjectName(event.getProjectName())
        .addObjectType(EventObject.PROJECT)
        .addProjectId(event.getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(
            Objects.isNull(event.getUserId()) ? EventSubject.APPLICATION : EventSubject.USER)
        .get();
  }

  @Override
  public Class<ProjectCreatedEvent> getEventClass() {
    return ProjectCreatedEvent.class;
  }
}

