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
import org.junit.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ImportEventsTest {

	private static final String FILE_NAME = "C:\\Windows\\winhlp32.exe";

	@Test
	public void started() {
		final Activity actual = new ImportStartedEvent(PROJECT_ID, USER_ID, FILE_NAME).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.START_IMPORT);
		assertActivity(expected, actual);
	}

	@Test
	public void finished() {
		final Activity actual = new ImportFinishedEvent(PROJECT_ID, USER_ID, FILE_NAME).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.FINISH_IMPORT);
		assertActivity(expected, actual);
	}

	private static Activity getExpectedActivity(ActivityAction action) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.IMPORT);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(ImportEventsTest.FILE_NAME));
		return activity;
	}
}