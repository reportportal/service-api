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
import com.epam.ta.reportportal.ws.model.activity.IssueTypeActivityResource;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class DefectTypeEventsTest {

	private static final String LONG_NAME = "test long name";

	@Test
	public void created() {
		final Activity actual = new DefectTypeCreatedEvent(getIssueType(), USER_ID, PROJECT_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.CREATE_DEFECT, LONG_NAME);
		assertActivity(expected, actual);
	}

	@Test
	public void deleted() {
		final Activity actual = new DefectTypeDeletedEvent(getIssueType(), USER_ID, PROJECT_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_DEFECT, LONG_NAME);
		assertActivity(expected, actual);
	}

	@Test
	public void updated() {
		final Activity actual = new DefectTypeUpdatedEvent(getIssueType(), USER_ID, PROJECT_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_DEFECT, LONG_NAME);
		assertActivity(expected, actual);
	}

	private static IssueTypeActivityResource getIssueType() {
		IssueTypeActivityResource issueType = new IssueTypeActivityResource();
		issueType.setId(1L);
		issueType.setLongName(LONG_NAME);
		return issueType;
	}

	private static Activity getExpectedActivity(ActivityAction action, String name) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.DEFECT_TYPE);
		activity.setUserId(USER_ID);
		activity.setProjectId(1L);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(name));
		return activity;
	}
}