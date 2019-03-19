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
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.STATUS;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TestItemStatusChangedEventTest {

	@Test
	void toActivity() {

		final String beforeStatus = "PASSED";
		final String afterStatus = "FAILED";
		final Activity actual = new TestItemStatusChangedEvent(getTestItem(beforeStatus), getTestItem(afterStatus), 1L).toActivity();
		final Activity expected = getExpectedActivity();
		expected.getDetails().setHistory(getExpectedHistory(Pair.of(beforeStatus, afterStatus)));
		checkActivity(expected, actual);
	}

	private static TestItemActivityResource getTestItem(String status) {
		TestItemActivityResource testItem = new TestItemActivityResource();
		testItem.setProjectId(3L);
		testItem.setStatus(status);
		testItem.setIssueTypeLongName("Product Bug");
		testItem.setIssueDescription("Description");
		testItem.setIgnoreAnalyzer(false);
		testItem.setAutoAnalyzed(true);
		testItem.setName("name");
		testItem.setId(2L);
		testItem.setTickets("1:http:/example.com/ticket/1,2:http:/example.com/ticket/2");
		return testItem;
	}

	private static Activity getExpectedActivity() {
		Activity activity = new Activity();
		activity.setAction(ActivityAction.UPDATE_ITEM.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM.getValue());
		activity.setUserId(1L);
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails("name"));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> status) {
		return Lists.newArrayList(HistoryField.of(STATUS, status.getLeft(), status.getRight()));
	}
}