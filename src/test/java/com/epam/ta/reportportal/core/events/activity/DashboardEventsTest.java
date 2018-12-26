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
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class DashboardEventsTest {

	@Test
	public void created() {
		final Activity actual = new DashboardCreatedEvent(getTestDashboard(NEW_NAME, NEW_SHARED, NEW_DESCRIPTION), USER_ID).toActivity();
		final Activity expected = getExpectedDashboardActivity(ActivityAction.CREATE_DASHBOARD, NEW_NAME);
		assertActivity(actual, expected);
	}

	@Test
	public void deleted() {
		final Activity actual = new DashboardDeletedEvent(getTestDashboard(OLD_NAME, OLD_SHARED, OLD_DESCRIPTION), USER_ID).toActivity();
		final Activity expected = getExpectedDashboardActivity(ActivityAction.DELETE_DASHBOARD, OLD_NAME);
		assertActivity(actual, expected);
	}

	@Test
	public void updated() {
		final Activity actual = new DashboardUpdatedEvent(getTestDashboard(OLD_NAME, OLD_SHARED, OLD_DESCRIPTION),
				getTestDashboard(NEW_NAME, NEW_SHARED, NEW_DESCRIPTION),
				USER_ID
		).toActivity();
		final Activity expected = getExpectedDashboardActivity(ActivityAction.UPDATE_DASHBOARD, NEW_NAME);
		expected.getDetails()
				.setHistory(getExpectedHistory(Pair.of(OLD_NAME, NEW_NAME),
						Pair.of(OLD_SHARED, NEW_SHARED),
						Pair.of(OLD_DESCRIPTION, NEW_DESCRIPTION)
				));
		assertActivity(actual, expected);
	}

	private static DashboardActivityResource getTestDashboard(String name, boolean shared, String description) {
		DashboardActivityResource dashboard = new DashboardActivityResource();
		dashboard.setShared(shared);
		dashboard.setDescription(description);
		dashboard.setProjectId(PROJECT_ID);
		dashboard.setName(name);
		dashboard.setId(OBJECT_ID);
		return dashboard;
	}

	private static Activity getExpectedDashboardActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.DASHBOARD);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}

	private static List<HistoryField> getExpectedHistory(Pair<String, String> name, Pair<Boolean, Boolean> shared,
			Pair<String, String> description) {
		return Lists.newArrayList(HistoryField.of(NAME_FIELD, name.getLeft(), name.getRight()),
				HistoryField.of(SHARE_FIELD, shared.getLeft().toString(), shared.getRight().toString()),
				HistoryField.of(DESCRIPTION_FIELD, description.getLeft(), description.getRight())
		);
	}

}