/*
 * Copyright 2024 EPAM Systems
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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.VALUE;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SettingsUpdatedEvent extends AroundEvent<ServerSettings> implements ActivityEvent {

  public SettingsUpdatedEvent(ServerSettings before, ServerSettings after, Long userId,
      String userLogin) {
    super(userId, userLogin, before, after);
  }

  @Override
  public Activity toActivity() {
    ActivityBuilder builder = new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_INSTANCE.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectName(getAfter().getKey())
        .addObjectType(EventObject.INSTANCE)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(VALUE, getBefore().getValue(), getAfter().getValue());
    return builder.get();
  }
}
