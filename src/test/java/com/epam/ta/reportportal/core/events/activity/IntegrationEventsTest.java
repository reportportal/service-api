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
import com.epam.ta.reportportal.ws.model.activity.IntegrationActivityResource;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class IntegrationEventsTest {

	private static final String TYPE_NAME = "VK";

	@Test
	public void created() {
		final Activity actual = new IntegrationCreatedEvent(getIntegration(), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.CREATE_BTS);
		assertActivity(expected, actual);
	}

	@Test
	public void deleted() {
		final Activity actual = new IntegrationDeletedEvent(getIntegration(), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_BTS);
		assertActivity(expected, actual);
	}

	@Test
	public void updated() {
		final Activity actual = new IntegrationUpdatedEvent(getIntegration(), USER_ID).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_BTS);
		assertActivity(expected, actual);
	}

	private static IntegrationActivityResource getIntegration() {
		IntegrationActivityResource integration = new IntegrationActivityResource();
		integration.setId(OBJECT_ID);
		integration.setProjectId(PROJECT_ID);
		integration.setTypeName(TYPE_NAME);
		integration.setProjectName(PROJECT_NAME);
		return integration;
	}

	private static Activity getExpectedActivity(ActivityAction action) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.INTEGRATION);
		activity.setUserId(USER_ID);
		activity.setProjectId(PROJECT_ID);
		activity.setObjectId(OBJECT_ID);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails(TYPE_NAME + ":" + PROJECT_NAME));
		return activity;
	}
}