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

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.TICKET_ID;

import com.epam.reportportal.base.core.events.domain.TicketPostedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

/**
 * Converter for TicketPostedEvent to Activity.
 */
@Component
public class TicketPostedEventConverter implements EventToActivityConverter<TicketPostedEvent> {

  @Override
  public Activity convert(TicketPostedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.POST)
        .addEventName(ActivityAction.POST_ISSUE.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getTestItemActivityResource().getId())
        .addObjectName(event.getTestItemActivityResource().getName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(event.getTestItemActivityResource().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(TICKET_ID,
            Strings.isNullOrEmpty(event.getTestItemActivityResource().getTickets())
                ? EMPTY_STRING
                : event.getTestItemActivityResource().getTickets(),
            Strings.isNullOrEmpty(event.getTestItemActivityResource().getTickets())
                ? event.getTicket().getId() + ":" + event.getTicket().getTicketUrl()
                : event.getTestItemActivityResource().getTickets() + "," + event.getTicket().getId()
                    + ":"
                    + event.getTicket().getTicketUrl()
        )
        .get();
  }

  @Override
  public Class<TicketPostedEvent> getEventClass() {
    return TicketPostedEvent.class;
  }
}
