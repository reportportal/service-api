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

package com.epam.reportportal.core.events.activity;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.settings.ServerSettingsResource;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SettingsUpdatedEvent extends AroundEvent<ServerSettingsResource> implements ActivityEvent {

  public SettingsUpdatedEvent(ServerSettingsResource before, ServerSettingsResource after, Long userId,
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
        .addObjectName("instanceSetting")
        .addObjectType(EventObject.INSTANCE)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(getAfter().getKey(), getBefore().getValue(), getAfter().getValue());
    return builder.get();
  }
}
