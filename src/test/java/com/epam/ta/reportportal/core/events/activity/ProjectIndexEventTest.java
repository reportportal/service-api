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
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ProjectIndexEventTest {

	private static Activity getExpectedActivity(ActivityAction action) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.PROJECT.getValue());
		activity.setUserId(1L);
		activity.setUsername("user");
		activity.setProjectId(3L);
		activity.setObjectId(3L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails("test_project"));
		return activity;
	}

	@Test
	void generate() {
		final Activity actual = new ProjectIndexEvent(1L, "user", 3L, "test_project", true).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.GENERATE_INDEX);
		checkActivity(expected, actual);
	}

	@Test
	void delete() {
		final Activity actual = new ProjectIndexEvent(1L, "user", 3L, "test_project", false).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_INDEX);
		checkActivity(expected, actual);
	}
}