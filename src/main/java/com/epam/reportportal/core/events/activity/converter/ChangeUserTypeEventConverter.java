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

import com.epam.reportportal.core.events.domain.ChangeUserTypeEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import org.springframework.stereotype.Component;

/**
 * Converter for ChangeUserTypeEvent to Activity.
 *
 */
@Component
public class ChangeUserTypeEventConverter implements EventToActivityConverter<ChangeUserTypeEvent> {

  @Override
  public Activity convert(ChangeUserTypeEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE_USER_ROLE)
        .addEventName(EventAction.UPDATE_USER_ROLE.getValue())
        .addPriority(UserRole.ADMINISTRATOR.equals(event.getNewType()) ? EventPriority.CRITICAL
            : EventPriority.HIGH)
        .addObjectId(event.getUserId())
        .addObjectName(event.getUserName())
        .addObjectType(EventObject.USER)
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField("userRole", event.getOldType().getAuthority(),
            event.getNewType().getAuthority())
        .get();
  }

  @Override
  public Class<ChangeUserTypeEvent> getEventClass() {
    return ChangeUserTypeEvent.class;
  }
}

