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

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class LaunchEventsTest {

	@Test
	public void started() {
		final String name = "name";
		final Activity actual = new LaunchStartedEvent(getLaunch(name), 1L).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.START_LAUNCH, name);
		checkActivity(expected, actual);
	}

	@Test
	public void finished() {
		final String name = "name";
		final Activity actual = new LaunchFinishedEvent(getLaunch(name), 1L).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.FINISH_LAUNCH, name);
		checkActivity(expected, actual);
	}

	@Test
	public void forceFinished() {
		final String name = "name";
		final Activity actual = new LaunchFinishForcedEvent(getLaunch(name), 1L).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.FINISH_LAUNCH, name);
		checkActivity(expected, actual);
	}

	@Test
	public void deleted() {
		final String name = "name";
		final Activity actual = new LaunchDeletedEvent(getLaunch(name), 1L).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_LAUNCH, name);
		checkActivity(expected, actual);
	}

	private static LaunchActivityResource getLaunch(String name) {
		LaunchActivityResource launch = new LaunchActivityResource();
		launch.setId(2L);
		launch.setName(name);
		launch.setProjectId(3L);
		return launch;
	}

	private static Activity getExpectedActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.LAUNCH);
		activity.setUserId(1L);
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}
}