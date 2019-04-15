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
import com.epam.ta.reportportal.ws.model.activity.UserFilterActivityResource;
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
class FilterEventsTest {

	private static Activity getExpectedActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.FILTER.getValue());
		activity.setUserId(1L);
		activity.setUsername("user");
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}

	@Test
	void created() {
		final String name = "name";
		final Activity actual = new FilterCreatedEvent(getUserFilter(name, true, "description"), 1L, "user").toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.CREATE_FILTER, name);
		checkActivity(expected, actual);
	}

	@Test
	void deleted() {
		final String name = "name";
		final Activity actual = new FilterDeletedEvent(getUserFilter(name, true, "description"), 1L, "user").toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_FILTER, name);
		checkActivity(expected, actual);
	}

	private static UserFilterActivityResource getUserFilter(String name, boolean shared, String description) {
		UserFilterActivityResource userFilter = new UserFilterActivityResource();
		userFilter.setId(2L);
		userFilter.setProjectId(3L);
		userFilter.setName(name);
		userFilter.setShared(shared);
		userFilter.setDescription(description);
		return userFilter;
	}

	@Test
	void updated() {
		final String oldName = "oldName";
		final boolean oldShared = false;
		final String oldDescription = "oldDescription";
		final String newName = "newName";
		final boolean newShared = true;
		final String newDescription = "newDescription";
		final Activity actual = new FilterUpdatedEvent(getUserFilter(oldName, oldShared, oldDescription),
				getUserFilter(newName, newShared, newDescription), 1L, "user"
		).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_FILTER, newName);
		expected.getDetails()
				.setHistory(getExpectedHistory(Pair.of(oldName, newName),
						Pair.of(oldShared, newShared),
						Pair.of(oldDescription, newDescription)
				));
		checkActivity(expected, actual);
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> name, Pair<Boolean, Boolean> shared,
			Pair<String, String> description) {
		return Lists.newArrayList(HistoryField.of(NAME, name.getLeft(), name.getRight()),
				HistoryField.of(SHARE, shared.getLeft().toString(), shared.getRight().toString()),
				HistoryField.of(DESCRIPTION, description.getLeft(), description.getRight())
		);
	}
}