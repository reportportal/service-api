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
import org.junit.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ProjectIndexEventTest {

	@Test
	public void generate() {
		final Activity actual = new ProjectIndexEvent(PROJECT_ID, PROJECT_NAME, USER_ID, true).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.GENERATE_INDEX);
		assertActivity(expected, actual);
	}

	@Test
	public void delete() {
		final Activity actual = new ProjectIndexEvent(PROJECT_ID, PROJECT_NAME, USER_ID, false).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_INDEX);
		assertActivity(expected, actual);
	}

	private static Activity getExpectedActivity(ActivityAction action) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.PROJECT);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(PROJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(PROJECT_NAME));
		return activity;
	}
}