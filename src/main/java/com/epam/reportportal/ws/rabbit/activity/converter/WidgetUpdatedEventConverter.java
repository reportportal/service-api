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

import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.CONTENT_FIELDS;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.ITEMS_COUNT;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.WIDGET_OPTIONS;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processDescription;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processName;

import com.epam.reportportal.core.events.domain.WidgetUpdatedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Converter for WidgetUpdatedEvent to Activity.
 *
 */
@Component
public class WidgetUpdatedEventConverter implements EventToActivityConverter<WidgetUpdatedEvent> {

  @Override
  public Activity convert(WidgetUpdatedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_WIDGET.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.WIDGET)
        .addProjectId(event.getAfter().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processName(event.getBefore().getName(), event.getAfter().getName()))
        .addHistoryField(processDescription(event.getBefore().getDescription(),
            event.getAfter().getDescription()))
        .addHistoryField(
            processItemsCount(event.getBefore().getItemsCount(), event.getAfter().getItemsCount()))
        .addHistoryField(processFields(event.getBefore().getContentFields(),
            event.getAfter().getContentFields()))
        .addHistoryField(Optional.of(HistoryField.of(WIDGET_OPTIONS, event.getWidgetOptionsBefore(),
            event.getWidgetOptionsAfter())))
        .get();
  }

  private Optional<HistoryField> processItemsCount(int before, int after) {
    if (before != after) {
      return Optional.of(
          HistoryField.of(ITEMS_COUNT, String.valueOf(before), String.valueOf(after)));
    }
    return Optional.empty();
  }

  private Optional<HistoryField> processFields(Set<String> before, Set<String> after) {
    if (before != null && after != null && !before.equals(after)) {
      String oldValue = String.join(", ", before);
      String newValue = String.join(", ", after);
      return Optional.of(HistoryField.of(CONTENT_FIELDS, oldValue, newValue));
    }
    return Optional.empty();
  }

  @Override
  public Class<WidgetUpdatedEvent> getEventClass() {
    return WidgetUpdatedEvent.class;
  }
}
