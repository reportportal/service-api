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

import com.epam.reportportal.core.events.domain.ProjectBulkDeletedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Converter for ProjectBulkDeletedEvent to Activity.
 *
 */
@Component
public class ProjectBulkDeletedEventConverter
    implements EventToActivityConverter<ProjectBulkDeletedEvent> {

  private static final String COMMA_SEPARATOR = ",";

  @Override
  public Activity convert(ProjectBulkDeletedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.BULK_DELETE)
        .addEventName(ActivityAction.BULK_DELETE_PROJECT.getValue())
        .addPriority(EventPriority.CRITICAL)
        .addObjectName(StringUtils.join(event.getProjectNames(), COMMA_SEPARATOR))
        .addObjectType(EventObject.PROJECT)
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }

  @Override
  public Class<ProjectBulkDeletedEvent> getEventClass() {
    return ProjectBulkDeletedEvent.class;
  }
}

