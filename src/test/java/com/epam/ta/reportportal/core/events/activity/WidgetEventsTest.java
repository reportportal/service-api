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
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class WidgetEventsTest {

	private static Activity getExpectedActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.WIDGET.getValue());
		activity.setUserId(1L);
		activity.setUsername("user");
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> name, Pair<Boolean, Boolean> shared,
			Pair<String, String> description, Pair<Integer, Integer> itemsCount, Pair<Set<String>, Set<String>> contentFields,
			Pair<String, String> options) {
		return Lists.newArrayList(HistoryField.of(NAME, name.getLeft(), name.getRight()),
				HistoryField.of(SHARE, shared.getLeft().toString(), shared.getRight().toString()),
				HistoryField.of(DESCRIPTION, description.getLeft(), description.getRight()),
				HistoryField.of(ITEMS_COUNT, itemsCount.getLeft().toString(), itemsCount.getRight().toString()),
				HistoryField.of(CONTENT_FIELDS, String.join(", ", contentFields.getLeft()), String.join(", ", contentFields.getRight())),
				HistoryField.of(WIDGET_OPTIONS, options.getLeft(), options.getRight())
		);
	}

	@Test
	void created() {
		final String name = "name";
		final boolean shared = true;
		final String description = "description";

		final Activity actual = new WidgetCreatedEvent(getWidget(name, shared, description, 2, getBeforeContentFields()),
				1L,
				"user"
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.CREATE_WIDGET, name);
		checkActivity(expected, actual);
	}

	private static WidgetActivityResource getWidget(String name, boolean shared, String description, int itemsCount,
			Set<String> contentFields) {
		WidgetActivityResource widget = new WidgetActivityResource();
		widget.setName(name);
		widget.setId(2L);
		widget.setDescription(description);
		widget.setShared(shared);
		widget.setProjectId(3L);
		widget.setItemsCount(itemsCount);
		widget.setContentFields(contentFields);
		return widget;
	}

	private static Set<String> getBeforeContentFields() {
		return Sets.newHashSet("field1", "field2", "field3");
	}

	private static Set<String> getAfterContentFields() {
		return Sets.newHashSet("field1", "field4", "field5", "field6");
	}

	private static String getBeforeOptions() {
		return "{ \"option1\": \"content\", \"option2\": \"enabled\"}";
	}

	private static String getAfterOptions() {
		return "{\n" + "  \"option1\": \"content\",\n" + "  \"option5\": \"disabled\",\n" + "  \"superOption\": \"superContent\"\n" + "}";
	}

	@Test
	void deleted() {
		final String name = "name";
		final boolean shared = true;
		final String description = "description";

		final Activity actual = new WidgetDeletedEvent(getWidget(name, shared, description, 3, getBeforeContentFields()),
				1L,
				"user"
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_WIDGET, name);
		checkActivity(expected, actual);
	}

	@Test
	void update() {
		final String oldName = "oldName";
		final boolean oldShared = false;
		final String oldDescription = "oldDescription";
		final String newName = "newName";
		final boolean newShared = true;
		final String newDescription = "newDescription";

		final Activity actual = new WidgetUpdatedEvent(getWidget(oldName, oldShared, oldDescription, 2, getBeforeContentFields()),
				getWidget(newName, newShared, newDescription, 4, getAfterContentFields()), 1L, "user",
				getBeforeOptions(), getAfterOptions()
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_WIDGET, newName);
		expected.getDetails()
				.setHistory(getExpectedHistory(Pair.of(oldName, newName),
						Pair.of(oldShared, newShared),
						Pair.of(oldDescription, newDescription),
				Pair.of(2, 4),
				Pair.of(getBeforeContentFields(), getAfterContentFields()),
				Pair.of(getBeforeOptions(), getAfterOptions())
		));
		checkActivity(expected, actual);

	}
}