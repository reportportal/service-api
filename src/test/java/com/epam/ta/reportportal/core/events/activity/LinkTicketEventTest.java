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

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LinkTicketEventTest {

	private static final String EXISTED_TICKETS = "1:http:/example.com/ticket/1,2:http:/example.com/ticket/2";
	private static final String LINKED_TICKET = "125:http:/example.com/ticket/125";

	@Test
	void toActivity() {
		final Activity actual = new LinkTicketEvent(getTestItem(EXISTED_TICKETS),
				getTestItem(EXISTED_TICKETS + "," + LINKED_TICKET),
				1L,
				"user"
		).toActivity();
		final Activity expected = getExpectedActivity();
		checkActivity(expected, actual);
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

	private static Activity getExpectedActivity() {
		Activity activity = new Activity();
		activity.setAction(ActivityAction.LINK_ISSUE.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.TICKET.getValue());
		activity.setUserId(1L);
		activity.setUsername("user");
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails("name"));
		activity.getDetails()
				.setHistory(Lists.newArrayList(HistoryField.of(TICKET_ID, EXISTED_TICKETS, EXISTED_TICKETS + "," + LINKED_TICKET)));
		return activity;
	}
}