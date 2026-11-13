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

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.NAME;

import com.epam.reportportal.base.ws.rabbit.activity.util.IntegrationActivityPriorityResolver;
import com.epam.reportportal.base.core.events.domain.IntegrationUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Converter for IntegrationUpdatedEvent to Activity.
 */
@Component
public class IntegrationUpdatedEventConverter implements
    EventToActivityConverter<IntegrationUpdatedEvent> {

  @Override
  public Activity convert(IntegrationUpdatedEvent event) {
    HistoryField integrationNameField;
    String beforeName = event.getBefore().getName();
    String afterName = event.getAfter().getName();
    if (beforeName != null && beforeName.equalsIgnoreCase(afterName)) {
      integrationNameField = HistoryField.of(NAME, null, afterName);
    } else {
      integrationNameField = HistoryField.of(NAME, beforeName, afterName);
    }

    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_INTEGRATION.getValue())
        .addPriority(IntegrationActivityPriorityResolver.resolvePriority(event.getAfter()))
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getTypeName())
        .addObjectType(EventObject.INTEGRATION)
        .addProjectId(event.getAfter().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(Optional.of(integrationNameField))
        .get();
  }

  @Override
  public Class<IntegrationUpdatedEvent> getEventClass() {
    return IntegrationUpdatedEvent.class;
  }
}
