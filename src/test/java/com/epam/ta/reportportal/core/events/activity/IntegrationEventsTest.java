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
import com.epam.ta.reportportal.ws.model.activity.IntegrationActivityResource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationEventsTest {

	@Test
	void created() {
		final Activity actual = new IntegrationCreatedEvent(getIntegration(), 1L).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.CREATE_BTS);
		checkActivity(expected, actual);
	}

	@Test
	void deleted() {
		final Activity actual = new IntegrationDeletedEvent(getIntegration(), 1L).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.DELETE_BTS);
		checkActivity(expected, actual);
	}

	@Test
	void updated() {
		final Activity actual = new IntegrationUpdatedEvent(getIntegration(), 1L).toActivity();
		final Activity expected = getExpectedActivity(ActivityAction.UPDATE_BTS);
		checkActivity(expected, actual);
	}

	private static IntegrationActivityResource getIntegration() {
		IntegrationActivityResource integration = new IntegrationActivityResource();
		integration.setId(2L);
		integration.setProjectId(3L);
		integration.setTypeName("type");
		integration.setProjectName("test_project");
		return integration;
	}

	private static Activity getExpectedActivity(ActivityAction action) {
		Activity activity = new Activity();
		activity.setAction(action.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.INTEGRATION);
		activity.setUserId(1L);
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails("type:test_project"));
		return activity;
	}
}