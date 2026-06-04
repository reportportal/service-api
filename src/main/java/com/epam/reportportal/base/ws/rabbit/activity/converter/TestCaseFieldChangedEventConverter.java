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

import com.epam.reportportal.base.core.events.domain.tms.TestCaseFieldChangedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TestCaseFieldChangedEventConverter implements EventToActivityConverter<TestCaseFieldChangedEvent> {

  @Override
  public Activity convert(TestCaseFieldChangedEvent event) {
    var historyField = HistoryField.of(
        event.getFieldName(),
        formatValue(event.getOldValue()),
        formatValue(event.getNewValue())
    );
  
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(event.getAction())
        .addEventName(event.getActivityAction().getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.TMS_TEST_CASE)
        .addProjectId(event.getAfter().getProjectId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addOrganizationId(event.getOrganizationId())
        .addHistoryField(Optional.of(historyField))
        .get();
  }

  @Override
  public Class<TestCaseFieldChangedEvent> getEventClass() {
    return TestCaseFieldChangedEvent.class;
  }

  private String formatValue(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Collection<?> col) {
      return col.stream()
          .map(String::valueOf)
          .collect(Collectors.joining(", "));
    }
    return value.toString();
  }
}
