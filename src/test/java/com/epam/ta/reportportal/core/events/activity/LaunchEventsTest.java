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
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LaunchEventsTest {

	private static Activity getExpectedActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.LAUNCH.getValue());
		activity.setUserId(1L);
		activity.setUsername("user");
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}

	@Test
	void started() {
		final String name = "name";
		final Activity actual = new LaunchStartedEvent(getLaunch(name), 1L, "user").toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.START_LAUNCH, name);
		checkActivity(expected, actual);
	}

	@Test
	void finished() {
		final String name = "name";
		final Activity actual = new LaunchFinishedEvent(getLaunch(name), 1L, "user").toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.FINISH_LAUNCH, name);
		checkActivity(expected, actual);
	}

	@Test
	void forceFinished() {
		final String name = "name";
		final Activity actual = new LaunchFinishForcedEvent(getLaunch(name), 1L, "user").toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.FINISH_LAUNCH, name);
		checkActivity(expected, actual);
	}

	private static LaunchActivityResource getLaunch(String name) {
		LaunchActivityResource launch = new LaunchActivityResource();
		launch.setId(2L);
		launch.setName(name);
		launch.setProjectId(3L);
		return launch;
	}

	@Test
	void deleted() {
		final String name = "name";
		final Activity actual = new LaunchDeletedEvent(getLaunch(name), 1L, "user").toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_LAUNCH, name);
		checkActivity(expected, actual);
	}
}