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

package com.epam.reportportal.core.events.domain;

import static com.epam.reportportal.core.events.domain.ActivityTestHelper.checkActivity;

import com.epam.reportportal.ws.rabbit.activity.converter.TicketPostedEventConverter;
import com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil;
import com.epam.reportportal.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TicketPostedEventTest {

  private static final String EXISTED_TICKETS =
      "1:http:/example.com/ticket/1,2:http:/example.com/ticket/2";
  private static final String NEW_TICKET_ID = "125";
  private static final String NEW_TICKET_URL = "http:/example.com/ticket/125";

  private static Activity getExpectedActivity() {
    Activity activity = new Activity();
    activity.setAction(EventAction.POST);
    activity.setEventName("postIssue");
    activity.setPriority(EventPriority.LOW);
    activity.setObjectType(EventObject.ITEM_ISSUE);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setOrganizationId(1L);
    activity.setProjectId(3L);
    activity.setObjectId(2L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName("name");
    activity.setDetails(new ActivityDetails());
    activity.getDetails().setHistory(getExpectedHistory(
        Pair.of(EXISTED_TICKETS, EXISTED_TICKETS + "," + NEW_TICKET_ID + ":" + NEW_TICKET_URL)));
    return activity;
  }

  private static Ticket getTicket() {
    Ticket ticket = new Ticket();
    ticket.setId(TicketPostedEventTest.NEW_TICKET_ID);
    ticket.setTicketUrl(TicketPostedEventTest.NEW_TICKET_URL);
    ticket.setStatus("status");
    ticket.setSummary("summary");
    return ticket;
  }

  private static TestItemActivityResource getTestItem() {
    TestItemActivityResource testItem = new TestItemActivityResource();
    testItem.setProjectId(3L);
    testItem.setStatus("FAILED");
    testItem.setIssueTypeLongName("Product Bug");
    testItem.setIssueDescription("Description");
    testItem.setIgnoreAnalyzer(false);
    testItem.setAutoAnalyzed(true);
    testItem.setName("name");
    testItem.setId(2L);
    testItem.setTickets(TicketPostedEventTest.EXISTED_TICKETS);
    return testItem;
  }

  @Test
  void toActivity() {
    TicketPostedEvent event = new TicketPostedEvent(getTicket(), 1L, "user", getTestItem(), 1L);
    TicketPostedEventConverter converter = new TicketPostedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity();
    checkActivity(expected, actual);
  }

  private static List<HistoryField> getExpectedHistory(Pair<String, String> tickets) {
    return Lists.newArrayList(
        HistoryField.of(ActivityDetailsUtil.TICKET_ID, tickets.getLeft(), tickets.getRight()));
  }
}
