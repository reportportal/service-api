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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.NAME;

import com.epam.reportportal.core.events.activity.util.IntegrationActivityPriorityResolver;
import com.epam.reportportal.core.events.domain.IntegrationDeletedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Converter for IntegrationDeletedEvent to Activity.
 */
@Component
public class IntegrationDeletedEventConverter implements
    EventToActivityConverter<IntegrationDeletedEvent> {

  @Override
  public Activity convert(IntegrationDeletedEvent event) {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_INTEGRATION.getValue())
        .addPriority(IntegrationActivityPriorityResolver.resolvePriority(
            event.getIntegrationActivityResource()))
        .addObjectId(event.getIntegrationActivityResource().getId())
        .addObjectName(event.getIntegrationActivityResource().getTypeName())
        .addObjectType(EventObject.INTEGRATION)
        .addProjectId(event.getIntegrationActivityResource().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(Optional.of(
            HistoryField.of(NAME, event.getIntegrationActivityResource().getName(), null)))
        .get();
  }

  @Override
  public Class<IntegrationDeletedEvent> getEventClass() {
    return IntegrationDeletedEvent.class;
  }
}
