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
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class WidgetEventsTest {

	@Test
	public void created() {
		final Activity actual = new WidgetCreatedEvent(getWidget(NEW_NAME, NEW_SHARED, NEW_DESCRIPTION, 2, getBeforeContentFields()),
				USER_ID
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.CREATE_WIDGET, NEW_NAME);
		assertActivity(expected, actual);
	}

	@Test
	public void deleted() {
		final Activity actual = new WidgetDeletedEvent(getWidget(OLD_NAME, OLD_SHARED, OLD_DESCRIPTION, 3, getBeforeContentFields()),
				USER_ID
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_WIDGET, OLD_NAME);
		assertActivity(expected, actual);
	}

	@Test
	public void update() {
		final Activity actual = new WidgetUpdatedEvent(getWidget(OLD_NAME, OLD_SHARED, OLD_DESCRIPTION, 2, getBeforeContentFields()),
				getWidget(NEW_NAME, NEW_SHARED, NEW_DESCRIPTION, 4, getAfterContentFields()),
				getBeforeOptions(),
				getAfterOptions(),
				USER_ID
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_WIDGET, NEW_NAME);
		expected.getDetails().setHistory(getExpectedHistory(Pair.of(OLD_NAME, NEW_NAME),
				Pair.of(OLD_SHARED, NEW_SHARED),
				Pair.of(OLD_DESCRIPTION, NEW_DESCRIPTION),
				Pair.of(2, 4),
				Pair.of(getBeforeContentFields(), getAfterContentFields()),
				Pair.of(getBeforeOptions(), getAfterOptions())
		));
		assertActivity(expected, actual);

	}

	private static WidgetActivityResource getWidget(String name, boolean shared, String description, int itemsCount,
			Set<String> contentFields) {
		WidgetActivityResource widget = new WidgetActivityResource();
		widget.setName(name);
		widget.setId(OBJECT_ID);
		widget.setDescription(description);
		widget.setShared(shared);
		widget.setProjectId(PROJECT_ID);
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

	private static Activity getExpectedActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.WIDGET);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> name, Pair<Boolean, Boolean> shared,
			Pair<String, String> description, Pair<Integer, Integer> itemsCount, Pair<Set<String>, Set<String>> contentFields,
			Pair<String, String> options) {
		return Lists.newArrayList(HistoryField.of(NAME_FIELD, name.getLeft(), name.getRight()),
				HistoryField.of(SHARE_FIELD, shared.getLeft().toString(), shared.getRight().toString()),
				HistoryField.of(DESCRIPTION_FIELD, description.getLeft(), description.getRight()),
				HistoryField.of(ITEMS_COUNT_FIELD, itemsCount.getLeft().toString(), itemsCount.getRight().toString()),
				HistoryField.of(CONTENT_FIELDS_FIELD,
						String.join(", ", contentFields.getLeft()),
						String.join(", ", contentFields.getRight())
				),
				HistoryField.of(WIDGET_OPTIONS_FIELD, options.getLeft(), options.getRight())
		);
	}
}