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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.UserActivityResource;
import java.time.Instant;

/**
 * @author Andrei Varabyeu
 */
public class UserCreatedEvent extends AbstractEvent implements ActivityEvent {

  private UserActivityResource userActivityResource;
  private final boolean isSystemEvent;
  private final Instant createdAt;

  public UserCreatedEvent(UserActivityResource userActivityResource, Long userId,
      String userLogin, boolean isSystemEvent) {
    super(userId, userLogin);
    this.userActivityResource = userActivityResource;
    this.isSystemEvent = isSystemEvent;
    this.createdAt = Instant.now();
  }

  public UserActivityResource getUserActivityResource() {
    return userActivityResource;
  }

  public void setUserActivityResource(UserActivityResource userActivityResource) {
    this.userActivityResource = userActivityResource;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedAt(createdAt)
        .addAction(EventAction.CREATE)
        .addEventName(ActivityAction.CREATE_USER.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectId(userActivityResource.getId())
        .addObjectName(userActivityResource.getFullName())
        .addObjectType(EventObject.USER)
        .addSubjectId(isSystemEvent ? null : getUserId())
        .addSubjectName(isSystemEvent ? RP_SUBJECT_NAME : getUserLogin())
        .addSubjectType(isSystemEvent ? EventSubject.APPLICATION : EventSubject.USER)
        .get();
  }
}
