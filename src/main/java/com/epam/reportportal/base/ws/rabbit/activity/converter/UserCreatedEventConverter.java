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

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectName;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.getSubjectType;

import com.epam.reportportal.base.core.events.domain.UserCreatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import org.springframework.stereotype.Component;

/**
 * Converter for UserCreatedEvent to Activity.
 *
 */
@Component
public class UserCreatedEventConverter implements EventToActivityConverter<UserCreatedEvent> {

  @Override
  public Activity convert(UserCreatedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.CREATE)
        .addEventName(ActivityAction.CREATE_USER.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectId(event.getUserActivityResource().getId())
        .addObjectName(event.getUserActivityResource().getFullName())
        .addObjectType(EventObject.USER)
        .addSubjectId(event.isSystemEvent() ? null : event.getUserId())
        .addSubjectName(getSubjectName(event))
        .addSubjectType(getSubjectType(event))
        .get();
  }

  @Override
  public Class<UserCreatedEvent> getEventClass() {
    return UserCreatedEvent.class;
  }
}
