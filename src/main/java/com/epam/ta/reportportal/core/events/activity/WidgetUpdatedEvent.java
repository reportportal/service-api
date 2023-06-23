/*
 * Copyright 2019 EPAM Systems
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
package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.CONTENT_FIELDS;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.ITEMS_COUNT;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.WIDGET_OPTIONS;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processDescription;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processName;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andrei Varabyeu
 */
public class WidgetUpdatedEvent extends AroundEvent<WidgetActivityResource> implements
    ActivityEvent {

  private String widgetOptionsBefore;
  private String widgetOptionsAfter;

  public WidgetUpdatedEvent() {
  }

  public WidgetUpdatedEvent(WidgetActivityResource before, WidgetActivityResource after,
      String widgetOptionsBefore,
      String widgetOptionsAfter, Long userId, String userLogin) {
    super(userId, userLogin, before, after);
    this.widgetOptionsBefore = widgetOptionsBefore;
    this.widgetOptionsAfter = widgetOptionsAfter;
  }

  public String getWidgetOptionsBefore() {
    return widgetOptionsBefore;
  }

  public void setWidgetOptionsBefore(String widgetOptionsBefore) {
    this.widgetOptionsBefore = widgetOptionsBefore;
  }

  public String getWidgetOptionsAfter() {
    return widgetOptionsAfter;
  }

  public void setWidgetOptionsAfter(String widgetOptionsAfter) {
    this.widgetOptionsAfter = widgetOptionsAfter;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_WIDGET.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(getAfter().getId())
        .addObjectName(getAfter().getName())
        .addObjectType(EventObject.WIDGET)
        .addProjectId(getAfter().getProjectId())
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processName(getBefore().getName(), getAfter().getName()))
        .addHistoryField(
            processDescription(getBefore().getDescription(), getAfter().getDescription()))
        .addHistoryField(processItemsCount(getBefore().getItemsCount(), getAfter().getItemsCount()))
        .addHistoryField(
            processFields(getBefore().getContentFields(), getAfter().getContentFields()))
        .addHistoryField(
            Optional.of(HistoryField.of(WIDGET_OPTIONS, widgetOptionsBefore, widgetOptionsAfter)))
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
}
