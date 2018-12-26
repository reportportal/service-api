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
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemIssueTypeDefinedEventTest {

	@Test
	public void toActivity() {
		final Activity actual = new ItemIssueTypeDefinedEvent(getTestItem(OLD_NAME, OLD_DESCRIPTION, true),
				getTestItem(NEW_NAME, NEW_DESCRIPTION, false),
				USER_ID
		).toActivity();
		final Activity expected = getExpectedActivity();
		expected.getDetails()
				.setHistory(getExpectedHistory(Pair.of(OLD_DESCRIPTION, NEW_DESCRIPTION),
						Pair.of(OLD_NAME, NEW_NAME),
						Pair.of("true", "false")
				));
		assertActivity(expected, actual);
	}

	private static TestItemActivityResource getTestItem(String name, String description, boolean ignoreAnalyzer) {
		TestItemActivityResource testItem = new TestItemActivityResource();
		testItem.setProjectId(PROJECT_ID);
		testItem.setStatus("FAILED");
		testItem.setIssueTypeLongName(name);
		testItem.setIssueDescription(description);
		testItem.setIgnoreAnalyzer(ignoreAnalyzer);
		testItem.setAutoAnalyzed(false);
		testItem.setName("name");
		testItem.setId(OBJECT_ID);
		testItem.setTickets("1:http:/example.com/ticket/1,2:http:/example.com/ticket/2");
		return testItem;
	}

	private static Activity getExpectedActivity() {
		Activity activity = new Activity();
		activity.setAction(ActivityAction.UPDATE_ITEM.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM_ISSUE);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails("name"));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> description, Pair<String, String> issueType,
			Pair<String, String> ignoreAnalyzer) {
		return Lists.newArrayList(HistoryField.of(COMMENT_FIELD, description.getLeft(), description.getRight()),
				HistoryField.of(ISSUE_TYPE_FIELD, issueType.getLeft(), issueType.getRight()),
				HistoryField.of(IGNORE_ANALYZER_FIELD, ignoreAnalyzer.getLeft(), ignoreAnalyzer.getRight())
		);
	}
}