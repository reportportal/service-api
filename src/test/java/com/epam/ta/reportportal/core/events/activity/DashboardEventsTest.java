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
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;
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
class DashboardEventsTest {

	@Test
	void created() {
		final String name = "name";

		final Activity actual = new DashboardCreatedEvent(getTestDashboard(name, false, "description"), 1L).toActivity();
		final Activity expected = getExpectedDashboardActivity(ActivityAction.CREATE_DASHBOARD, name);
		checkActivity(actual, expected);
	}

	@Test
	void deleted() {
		final String name = "name";

		final Activity actual = new DashboardDeletedEvent(getTestDashboard(name, false, "description"), 1L).toActivity();
		final Activity expected = getExpectedDashboardActivity(ActivityAction.DELETE_DASHBOARD, name);
		checkActivity(actual, expected);
	}

	@Test
	void updated() {
		final String oldName = "oldName";
		final boolean oldShared = true;
		final String oldDescription = "oldDescription";
		final String newName = "newName";
		final boolean newShared = false;
		final String newDescription = "newDescription";

		final Activity actual = new DashboardUpdatedEvent(getTestDashboard(oldName, oldShared, oldDescription),
				getTestDashboard(newName, newShared, newDescription),
				1L
		).toActivity();
		final Activity expected = getExpectedDashboardActivity(ActivityAction.UPDATE_DASHBOARD, newName);
		expected.getDetails()
				.setHistory(getExpectedHistory(Pair.of(oldName, newName),
						Pair.of(oldShared, newShared),
						Pair.of(oldDescription, newDescription)
				));
		checkActivity(actual, expected);
	}

	private static DashboardActivityResource getTestDashboard(String name, boolean shared, String description) {
		DashboardActivityResource dashboard = new DashboardActivityResource();
		dashboard.setShared(shared);
		dashboard.setDescription(description);
		dashboard.setProjectId(3L);
		dashboard.setName(name);
		dashboard.setId(2L);
		return dashboard;
	}

	private static Activity getExpectedDashboardActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.DASHBOARD.getValue());
		activity.setUserId(1L);
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> name, Pair<Boolean, Boolean> shared,
			Pair<String, String> description) {
		return Lists.newArrayList(HistoryField.of(NAME, name.getLeft(), name.getRight()),
				HistoryField.of(SHARE, shared.getLeft().toString(), shared.getRight().toString()),
				HistoryField.of(DESCRIPTION, description.getLeft(), description.getRight())
		);
	}

}