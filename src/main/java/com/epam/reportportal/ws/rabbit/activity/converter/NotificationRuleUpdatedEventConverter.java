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

import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.ATTRIBUTES;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.ATTRIBUTES_OPERATOR;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.ENABLED;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.LAUNCH_NAMES;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.RECIPIENTS;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.RULE_DETAILS;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.SEND_CASE;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.TYPE;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processBoolean;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processList;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processMap;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processName;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.processString;

import com.epam.reportportal.core.events.domain.NotificationRuleUpdatedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.reporting.ItemAttributeResource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Converter for NotificationRuleUpdatedEvent to Activity.
 */
@Component
public class NotificationRuleUpdatedEventConverter implements
    EventToActivityConverter<NotificationRuleUpdatedEvent> {

  @Override
  public Activity convert(NotificationRuleUpdatedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_NOTIFICATION_RULE.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.NOTIFICATION_RULE)
        .addProjectId(event.getAfter().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processName(event.getBefore().getName(), event.getAfter().getName()))
        .addHistoryField(processList(RECIPIENTS, event.getBefore().getRecipients(),
            event.getAfter().getRecipients()))
        .addHistoryField(processList(LAUNCH_NAMES, event.getBefore().getLaunchNames(),
            event.getAfter().getLaunchNames()))
        .addHistoryField(processAttributesSet(event.getBefore().getAttributes(),
            event.getAfter().getAttributes()))
        .addHistoryField(
            processBoolean(ENABLED, event.getBefore().isEnabled(), event.getAfter().isEnabled()))
        .addHistoryField(processString(SEND_CASE, event.getBefore().getSendCase(),
            event.getAfter().getSendCase()))
        .addHistoryField(
            processString(TYPE, event.getBefore().getType(), event.getAfter().getType()))
        .addHistoryField(
            processString(ATTRIBUTES_OPERATOR, event.getBefore().getAttributesOperator(),
                event.getAfter().getAttributesOperator()))
        .addHistoryField(processMap(RULE_DETAILS, event.getBefore().getRuleDetails(),
            event.getAfter().getRuleDetails()))
        .get();
  }

  private static Optional<HistoryField> processAttributesSet(Set<ItemAttributeResource> before,
      Set<ItemAttributeResource> after) {
    List<String> left = normalizeAttributes(before);
    List<String> right = normalizeAttributes(after);
    if (!left.equals(right)) {
      return Optional.of(HistoryField.of(
          ATTRIBUTES, String.join(", ", left),
          String.join(", ", right)));
    }
    return Optional.empty();
  }

  private static List<String> normalizeAttributes(
      Set<ItemAttributeResource> attrs) {
    if (attrs == null) {
      return List.of();
    }
    return attrs.stream()
        .filter(Objects::nonNull)
        .map(a -> (a.getKey() == null ? EMPTY_STRING : a.getKey()) + ":"
            + (a.getValue() == null ? EMPTY_STRING : a.getValue()))
        .toList();
  }

  @Override
  public Class<NotificationRuleUpdatedEvent> getEventClass() {
    return NotificationRuleUpdatedEvent.class;
  }
}
