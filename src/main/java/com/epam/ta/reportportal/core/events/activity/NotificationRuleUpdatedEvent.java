/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.ATTRIBUTES_OPERATOR;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.ENABLED;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.LAUNCH_NAMES;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RECIPIENTS;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RULE_DETAILS;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.SEND_CASE;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TYPE;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processBoolean;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processList;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processMap;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processName;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processString;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.model.activity.NotificationRuleActivityResource;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Activity event for update of a project notification rule.
 */
public class NotificationRuleUpdatedEvent extends AroundEvent<NotificationRuleActivityResource>
    implements ActivityEvent {

  private final Long orgId;

  public NotificationRuleUpdatedEvent(NotificationRuleActivityResource before,
      NotificationRuleActivityResource after,
      Long userId,
      String userLogin,
      Long orgId) {
    super(userId, userLogin, before, after);
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_NOTIFICATION_RULE.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(getAfter().getId())
        .addObjectName(getAfter().getName())
        .addObjectType(EventObject.NOTIFICATION_RULE)
        .addProjectId(getAfter().getProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processName(getBefore().getName(), getAfter().getName()))
        .addHistoryField(processList(RECIPIENTS, getBefore().getRecipients(), getAfter().getRecipients()))
        .addHistoryField(processList(LAUNCH_NAMES, getBefore().getLaunchNames(), getAfter().getLaunchNames()))
        .addHistoryField(processAttributesSet(getBefore().getAttributes(), getAfter().getAttributes()))
        .addHistoryField(processBoolean(ENABLED, getBefore().isEnabled(), getAfter().isEnabled()))
        .addHistoryField(processString(SEND_CASE, getBefore().getSendCase(), getAfter().getSendCase()))
        .addHistoryField(processString(TYPE, getBefore().getType(), getAfter().getType()))
        .addHistoryField(
            processString(ATTRIBUTES_OPERATOR, getBefore().getAttributesOperator(), getAfter().getAttributesOperator()))
        .addHistoryField(processMap(RULE_DETAILS, getBefore().getRuleDetails(), getAfter().getRuleDetails()))
        .get();
  }

  private static Optional<HistoryField> processAttributesSet(Set<ItemAttributeResource> before,
      Set<ItemAttributeResource> after) {

    List<String> left = normalizeAttributes(before);
    List<String> right = normalizeAttributes(after);
    if (!left.equals(right)) {
      return Optional.of(HistoryField.of(
          com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.ATTRIBUTES, String.join(", ", left),
          String.join(", ", right)));
    }
    return Optional.empty();
  }

  private static List<String> normalizeAttributes(
      Set<com.epam.ta.reportportal.ws.reporting.ItemAttributeResource> attrs) {
    if (attrs == null) {
      return List.of();
    }
    return attrs.stream()
        .filter(Objects::nonNull)
        .map(a -> (a.getKey() == null ? EMPTY_STRING : a.getKey()) + ":"
            + (a.getValue() == null ? EMPTY_STRING : a.getValue()))
//        .sorted()
        .toList();
  }
}


