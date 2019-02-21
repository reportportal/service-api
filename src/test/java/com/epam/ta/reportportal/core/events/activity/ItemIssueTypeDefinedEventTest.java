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
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ItemIssueTypeDefinedEventTest {

	@Test
	void toActivity() {
		final boolean oldIgnoreAnalyzer = true;
		final String oldName = "oldName";
		final String oldDescription = "oldDescription";
		final boolean newIgnoreAnalyzer = false;
		final String newDescription = "newDescription";
		final String newName = "newName";

		final Activity actual = new ItemIssueTypeDefinedEvent(
				getTestItem(oldName, oldDescription, oldIgnoreAnalyzer),
				getTestItem(newName, newDescription, newIgnoreAnalyzer),
				1L
		).toActivity();
		final Activity expected = getExpectedActivity();
		expected.getDetails().setHistory(getExpectedHistory(
				Pair.of(oldDescription, newDescription),
				Pair.of(oldName, newName),
				Pair.of(String.valueOf(oldIgnoreAnalyzer), String.valueOf(newIgnoreAnalyzer))
		));
		checkActivity(expected, actual);
	}

	private static TestItemActivityResource getTestItem(String name, String description, boolean ignoreAnalyzer) {
		TestItemActivityResource testItem = new TestItemActivityResource();
		testItem.setProjectId(3L);
		testItem.setStatus("FAILED");
		testItem.setIssueTypeLongName(name);
		testItem.setIssueDescription(description);
		testItem.setIgnoreAnalyzer(ignoreAnalyzer);
		testItem.setAutoAnalyzed(false);
		testItem.setName("name");
		testItem.setId(2L);
		testItem.setTickets("1:http:/example.com/ticket/1,2:http:/example.com/ticket/2");
		return testItem;
	}

	private static Activity getExpectedActivity() {
		Activity activity = new Activity();
		activity.setAction(ActivityAction.UPDATE_ITEM.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM_ISSUE);
		activity.setUserId(1L);
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails("name"));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> description, Pair<String, String> issueType,
			Pair<String, String> ignoreAnalyzer) {
		return Lists.newArrayList(
				HistoryField.of(COMMENT, description.getLeft(), description.getRight()),
				HistoryField.of(ISSUE_TYPE, issueType.getLeft(), issueType.getRight()),
				HistoryField.of(IGNORE_ANALYZER, ignoreAnalyzer.getLeft(), ignoreAnalyzer.getRight())
		);
	}
}