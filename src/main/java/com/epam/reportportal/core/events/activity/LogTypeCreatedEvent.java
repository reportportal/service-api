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
import com.epam.reportportal.model.activity.LogTypeActivityResource;

/**
 * Event published when a log type is created.
 *
 */
public class LogTypeCreatedEvent extends AbstractEvent implements ActivityEvent {

  private final LogTypeActivityResource logTypeActivityResource;

  public LogTypeCreatedEvent(LogTypeActivityResource logTypeActivityResource, Long userId,
      String userLogin) {
    super(userId, userLogin);
    this.logTypeActivityResource = logTypeActivityResource;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.CREATE)
        .addEventName(ActivityAction.CREATE_LOG_TYPE.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(logTypeActivityResource.getId())
        .addObjectName(logTypeActivityResource.getName())
        .addObjectType(EventObject.LOG_TYPE)
        .addProjectId(logTypeActivityResource.getProjectId())
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }
}
