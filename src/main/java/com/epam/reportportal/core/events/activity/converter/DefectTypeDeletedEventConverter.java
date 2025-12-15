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

import com.epam.reportportal.core.events.domain.DefectTypeDeletedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

/**
 * Converter for DefectTypeDeletedEvent to Activity.
 */
@Component
public class DefectTypeDeletedEventConverter implements
    EventToActivityConverter<DefectTypeDeletedEvent> {

  @Override
  public Activity convert(DefectTypeDeletedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_DEFECT.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(event.getBefore().getId())
        .addObjectName(event.getBefore().getLongName())
        .addObjectType(EventObject.DEFECT_TYPE)
        .addProjectId(event.getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }

  @Override
  public Class<DefectTypeDeletedEvent> getEventClass() {
    return DefectTypeDeletedEvent.class;
  }
}
