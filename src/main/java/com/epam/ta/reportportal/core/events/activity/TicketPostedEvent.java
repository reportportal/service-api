/*
 * Copyright 2019 EPAM Systems
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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.TestItemActivityResource;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Andrei Varabyeu
 */
@Setter
@Getter
public class TicketPostedEvent extends AbstractEvent implements ActivityEvent {

  private Ticket ticket;
  private TestItemActivityResource testItemActivityResource;
  private Long orgId;

  public TicketPostedEvent() {
  }

  public TicketPostedEvent(Ticket ticket, Long userId, String userLogin,
      TestItemActivityResource testItemActivityResource, Long  orgId) {
    super(userId, userLogin);
    this.ticket = ticket;
    this.testItemActivityResource = testItemActivityResource;

  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.POST)
        .addEventName(ActivityAction.POST_ISSUE.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(testItemActivityResource.getId())
        .addObjectName(testItemActivityResource.getName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(testItemActivityResource.getProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin()).addSubjectType(EventSubject.USER)
        .addHistoryField(TICKET_ID, Strings.isNullOrEmpty(testItemActivityResource.getTickets()) ? EMPTY_STRING :
                testItemActivityResource.getTickets(),
            Strings.isNullOrEmpty(testItemActivityResource.getTickets()) ?
                ticket.getId() + ":" + ticket.getTicketUrl() :
                testItemActivityResource.getTickets() + "," + ticket.getId() + ":"
                    + ticket.getTicketUrl()
        ).get();
  }
}
