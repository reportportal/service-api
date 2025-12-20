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

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processField;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processName;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.LogTypeActivityResource;

/**
 * Event published when a log type is updated.
 *
 */
public class LogTypeUpdatedEvent extends AroundEvent<LogTypeActivityResource>
    implements ActivityEvent {

  public LogTypeUpdatedEvent(LogTypeActivityResource before, LogTypeActivityResource after,
      Long userId, String userLogin) {
    super(userId, userLogin, before, after);
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_LOG_TYPE.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(getAfter().getId())
        .addObjectName(getAfter().getName())
        .addObjectType(EventObject.LOG_TYPE)
        .addProjectId(getAfter().getProjectId())
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processName(getBefore().getName(), getAfter().getName()))
        .addHistoryField(processField("level", getBefore().getLevel(), getAfter().getLevel()))
        .addHistoryField(processField("labelColor", getBefore().getLabelColor(),
            getAfter().getLabelColor()))
        .addHistoryField(processField("backgroundColor", getBefore().getBackgroundColor(),
            getAfter().getBackgroundColor()))
        .addHistoryField(processField("textColor", getBefore().getTextColor(),
            getAfter().getTextColor()))
        .addHistoryField(processField("textStyle", getBefore().getTextStyle(),
            getAfter().getTextStyle()))
        .addHistoryField(processField("isFilterable", getBefore().getIsFilterable(),
            getAfter().getIsFilterable()))
        .get();
  }
}
