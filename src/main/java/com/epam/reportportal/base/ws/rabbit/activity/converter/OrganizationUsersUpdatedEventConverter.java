/*
 * Copyright 2026 EPAM Systems
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

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.USERS;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectId;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectName;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectType;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.processList;

import com.epam.reportportal.base.core.events.domain.OrganizationUsersUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import org.springframework.stereotype.Component;

/**
 * Converter for OrganizationUsersUpdatedEvent to Activity.
 */
@Component
public class OrganizationUsersUpdatedEventConverter implements EventToActivityConverter<OrganizationUsersUpdatedEvent> {

  @Override
  public Activity convert(OrganizationUsersUpdatedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(event.getAction())
        .addEventName(ActivityAction.UPDATE_ORGANIZATION_USERS.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(event.getOrganizationId())
        .addObjectName(event.getOrganizationName())
        .addObjectType(EventObject.ORGANIZATION)
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(getSubjectId(event))
        .addSubjectName(getSubjectName(event))
        .addSubjectType(getSubjectType(event))
        .addHistoryField(processList(USERS, event.getBefore(), event.getAfter()))
        .get();
  }

  @Override
  public Class<OrganizationUsersUpdatedEvent> getEventClass() {
    return OrganizationUsersUpdatedEvent.class;
  }
}
