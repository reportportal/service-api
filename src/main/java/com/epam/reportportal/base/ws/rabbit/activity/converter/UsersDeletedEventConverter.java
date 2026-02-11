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

import com.epam.reportportal.base.core.events.domain.UsersDeletedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Converter for UsersDeletedEvent to Activity. Handles both user-initiated and system events.
 *
 */
@Component
public class UsersDeletedEventConverter implements EventToActivityConverter<UsersDeletedEvent> {

  public static final String JOBS_SERVICE = "Jobs Service";

  @Override
  public Activity convert(UsersDeletedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.BULK_DELETE)
        .addEventName(ActivityAction.BULK_DELETE_USERS.getValue())
        .addObjectName(formatObjectName(event.getCount()))
        .addObjectType(EventObject.USER)
        .addSubjectId(event.isSystemEvent() ? null : event.getUserId())
        .addSubjectName(ActivityDetailsUtil.getSubjectName(event))
        .addSubjectType(ActivityDetailsUtil.getSubjectType(event))
        .addPriority(determinePriority(event))
        .get();
  }

  private EventPriority determinePriority(UsersDeletedEvent event) {
    String eventSource = event.getEventSource();
    if (Objects.nonNull(eventSource) && eventSource.equals(JOBS_SERVICE)) {
      return EventPriority.HIGH;
    }
    return EventPriority.CRITICAL;
  }

  private String formatObjectName(int count) {
    return String.format("%d deleted %s",
        count,
        count == 1 ? "user" : "users"
    );
  }

  @Override
  public Class<UsersDeletedEvent> getEventClass() {
    return UsersDeletedEvent.class;
  }
}
