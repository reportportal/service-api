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

import com.epam.reportportal.core.events.domain.OrganizationDeletedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Converter for OrganizationDeletedEvent to Activity.
 *
 */
@Component
public class OrganizationDeletedEventConverter implements
    EventToActivityConverter<OrganizationDeletedEvent> {

  @Override
  public Activity convert(OrganizationDeletedEvent event) {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_ORGANIZATION.getValue())
        .addPriority(EventPriority.CRITICAL)
        .addObjectId(event.getOrganizationId())
        .addObjectName(event.getOrganizationName())
        .addObjectType(EventObject.ORGANIZATION)
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(
            Objects.isNull(event.getUserId()) ? EventSubject.APPLICATION : EventSubject.USER)
        .get();
  }

  @Override
  public Class<OrganizationDeletedEvent> getEventClass() {
    return OrganizationDeletedEvent.class;
  }
}

