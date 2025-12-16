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

import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.INTERRUPT_JOB_TIME;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.KEEP_LAUNCHES;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.KEEP_LOGS;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.KEEP_SCREENSHOTS;

import com.epam.reportportal.core.events.domain.ProjectUpdatedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

/**
 * Converter for ProjectUpdatedEvent to Activity.
 *
 */
@Component
public class ProjectUpdatedEventConverter implements EventToActivityConverter<ProjectUpdatedEvent> {

  @Override
  public Activity convert(ProjectUpdatedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_PROJECT.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectId(event.getBefore().getProjectId())
        .addObjectName(event.getBefore().getProjectName())
        .addObjectType(EventObject.PROJECT)
        .addProjectId(event.getBefore().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(
            processParameter(event.getBefore().getConfig(), event.getAfter().getConfig(),
                INTERRUPT_JOB_TIME.getAttribute()))
        .addHistoryField(
            processParameter(event.getBefore().getConfig(), event.getAfter().getConfig(),
                KEEP_SCREENSHOTS.getAttribute()))
        .addHistoryField(
            processParameter(event.getBefore().getConfig(), event.getAfter().getConfig(),
                KEEP_LOGS.getAttribute()))
        .addHistoryField(
            processParameter(event.getBefore().getConfig(), event.getAfter().getConfig(),
                KEEP_LAUNCHES.getAttribute()))
        .get();
  }

  @Override
  public Class<ProjectUpdatedEvent> getEventClass() {
    return ProjectUpdatedEvent.class;
  }
}

