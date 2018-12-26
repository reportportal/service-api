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
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class LaunchEventsTest {

	@Test
	public void started() {
		final Activity actual = new LaunchStartedEvent(getLaunch(NEW_NAME), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.START_LAUNCH, NEW_NAME);
		assertActivity(expected, actual);
	}

	@Test
	public void finished() {
		final Activity actual = new LaunchFinishedEvent(getLaunch(NEW_NAME), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.FINISH_LAUNCH, NEW_NAME);
		assertActivity(expected, actual);
	}

	@Test
	public void forceFinished() {
		final Activity actual = new LaunchFinishForcedEvent(getLaunch(NEW_NAME), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.FINISH_LAUNCH, NEW_NAME);
		assertActivity(expected, actual);
	}

	@Test
	public void deleted() {
		final Activity actual = new LaunchDeletedEvent(getLaunch(OLD_NAME), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_LAUNCH, OLD_NAME);
		assertActivity(expected, actual);
	}

	private static LaunchActivityResource getLaunch(String name) {
		LaunchActivityResource launch = new LaunchActivityResource();
		launch.setId(OBJECT_ID);
		launch.setName(name);
		launch.setProjectId(PROJECT_ID);
		return launch;
	}

	private static Activity getExpectedActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.LAUNCH);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}
}