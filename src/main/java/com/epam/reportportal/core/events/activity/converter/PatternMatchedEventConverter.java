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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.PATTERN_NAME;

import com.epam.reportportal.core.events.domain.PatternMatchedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Converter for PatternMatchedEvent to Activity.
 */
@Component
public class PatternMatchedEventConverter implements EventToActivityConverter<PatternMatchedEvent> {

  @Override
  public Activity convert(PatternMatchedEvent event) {
    HistoryField patternNameField = new HistoryField();
    patternNameField.setField(PATTERN_NAME);
    patternNameField.setNewValue(event.getPatternTemplateActivityResource().getName());

    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.MATCH)
        .addEventName(ActivityAction.PATTERN_MATCHED.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getItemId())
        .addObjectName(event.getItemName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(event.getPatternTemplateActivityResource().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectName("Pattern Analysis")
        .addSubjectType(EventSubject.RULE)
        .addHistoryField(Optional.of(patternNameField))
        .get();
  }

  @Override
  public Class<PatternMatchedEvent> getEventClass() {
    return PatternMatchedEvent.class;
  }
}
