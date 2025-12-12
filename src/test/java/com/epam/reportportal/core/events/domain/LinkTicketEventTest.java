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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;
import static com.epam.reportportal.core.events.domain.ActivityTestHelper.checkActivity;

import com.epam.reportportal.core.events.activity.converter.LinkTicketEventConverter;
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
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LinkTicketEventTest {

  private static final String EXISTED_TICKETS =
      "1:http:/example.com/ticket/1,2:http:/example.com/ticket/2";
  private static final String LINKED_TICKET = "125:http:/example.com/ticket/125";

  private static Activity getExpectedActivity() {
    Activity activity = new Activity();
    activity.setAction(EventAction.LINK);
    activity.setEventName("linkIssue");
    activity.setPriority(EventPriority.LOW);
    activity.setObjectType(EventObject.ITEM_ISSUE);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setProjectId(3L);
    activity.setOrganizationId(1L);
    activity.setObjectId(2L);
    activity.setObjectName("name");
    activity.setCreatedAt(Instant.now());
    activity.setDetails(new ActivityDetails(Lists.newArrayList(
        HistoryField.of(TICKET_ID, EXISTED_TICKETS, EXISTED_TICKETS + "," + LINKED_TICKET))));
    return activity;
  }

  private static TestItemActivityResource getTestItem(String tickets) {
    TestItemActivityResource testItem = new TestItemActivityResource();
    testItem.setProjectId(3L);
    testItem.setStatus("FAILED");
    testItem.setIssueTypeLongName("issueTypeName");
    testItem.setIssueDescription("desc");
    testItem.setIgnoreAnalyzer(false);
    testItem.setAutoAnalyzed(false);
    testItem.setName("name");
    testItem.setId(2L);
    testItem.setTickets(tickets);
    return testItem;
  }

  @Test
  void toActivity() {
    LinkTicketEvent event = new LinkTicketEvent(getTestItem(EXISTED_TICKETS),
        getTestItem(EXISTED_TICKETS + "," + LINKED_TICKET), 1L, "user", 1L);
    LinkTicketEventConverter converter = new LinkTicketEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity();
    checkActivity(expected, actual);
  }
}
