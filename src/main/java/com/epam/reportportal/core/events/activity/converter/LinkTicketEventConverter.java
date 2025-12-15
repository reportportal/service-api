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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;

import com.epam.reportportal.core.events.domain.LinkTicketEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

/**
 * Converter for LinkTicketEvent to Activity. System events are persisted with APPLICATION subject
 * type.
 */
@Component
public class LinkTicketEventConverter implements EventToActivityConverter<LinkTicketEvent> {

  @Override
  public Activity convert(LinkTicketEvent event) {
    ActivityBuilder builder = new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.LINK)
        .addEventName(event.isSystemEvent()
            ? ActivityAction.LINK_ISSUE_AA.getValue()
            : ActivityAction.LINK_ISSUE.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(event.getAfter().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.isSystemEvent() ? null : event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(event.isSystemEvent() ? EventSubject.APPLICATION : EventSubject.USER);

    if (event.getAfter() != null) {
      String oldValue = event.getBefore().getTickets();
      String newValue = event.getAfter().getTickets();
      //no changes with tickets
      if (Strings.isNullOrEmpty(oldValue) && newValue.isEmpty() || oldValue.equalsIgnoreCase(
          newValue)) {
        return null;
      }
      if (!oldValue.isEmpty() && !newValue.isEmpty() || !oldValue.equalsIgnoreCase(newValue)) {
        if (oldValue.length() > newValue.length()) {
          builder.addAction(EventAction.UNLINK);
          builder.addEventName(ActivityAction.UNLINK_ISSUE.getValue());
        }
        builder.addHistoryField(TICKET_ID, oldValue, newValue);
      }
    }

    return builder.get();
  }

  @Override
  public Class<LinkTicketEvent> getEventClass() {
    return LinkTicketEvent.class;
  }
}
