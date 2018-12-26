/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class TicketPostedEventTest {

	private static final String EXISTED_TICKETS = "1:http:/example.com/ticket/1,2:http:/example.com/ticket/2";
	private static final String NEW_TICKET_ID = "125";
	private static final String NEW_TICKET_URL = "http:/example.com/ticket/125";

	@Test
	public void toActivity() {
		final Activity actual = new TicketPostedEvent(getTicket(), USER_ID, getTestItem()).toActivity();
		final Activity expected = getExpectedActivity();
		assertActivity(expected, actual);
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
		testItem.setProjectId(PROJECT_ID);
		testItem.setStatus("FAILED");
		testItem.setIssueTypeLongName("Product Bug");
		testItem.setIssueDescription("Description");
		testItem.setIgnoreAnalyzer(false);
		testItem.setAutoAnalyzed(true);
		testItem.setName(NEW_NAME);
		testItem.setId(OBJECT_ID);
		testItem.setTickets(TicketPostedEventTest.EXISTED_TICKETS);
		return testItem;
	}

	private static Activity getExpectedActivity() {
		Activity activity = new Activity();
		activity.setAction(ActivityAction.POST_ISSUE.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.TICKET);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(NEW_NAME));
		activity.getDetails()
				.setHistory(getExpectedHistory(Pair.of(EXISTED_TICKETS, EXISTED_TICKETS + "," + NEW_TICKET_ID + ":" + NEW_TICKET_URL)));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> tickets) {
		return Lists.newArrayList(HistoryField.of(TICKET_ID_FIELD, tickets.getLeft(), tickets.getRight()));
	}
}