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

import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class TestItemStatusChangedEventTest {

	private static final String BEFORE_STATUS = "PASSED";
	private static final String AFTER_STATUS = "FAILED";

	@Test
	public void toActivity() {

		final Activity actual = new TestItemStatusChangedEvent(getTestItem(BEFORE_STATUS), getTestItem(AFTER_STATUS), USER_ID).toActivity();
		final Activity expected = getExpectedActivity();
		expected.getDetails().setHistory(getExpectedHistory(Pair.of(BEFORE_STATUS, AFTER_STATUS)));
		assertActivity(expected, actual);
	}

	private static TestItemActivityResource getTestItem(String status) {
		TestItemActivityResource testItem = new TestItemActivityResource();
		testItem.setProjectId(PROJECT_ID);
		testItem.setStatus(status);
		testItem.setIssueTypeLongName("Product Bug");
		testItem.setIssueDescription("Description");
		testItem.setIgnoreAnalyzer(false);
		testItem.setAutoAnalyzed(true);
		testItem.setName(NEW_NAME);
		testItem.setId(OBJECT_ID);
		testItem.setTickets("1:http:/example.com/ticket/1,2:http:/example.com/ticket/2");
		return testItem;
	}

	private static Activity getExpectedActivity() {
		Activity activity = new Activity();
		activity.setAction(ActivityAction.UPDATE_ITEM.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(NEW_NAME));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> status) {
		return Lists.newArrayList(HistoryField.of(STATUS_FIELD, status.getLeft(), status.getRight()));
	}
}