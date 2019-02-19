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
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class UserCreatedEventTest {

	@Test
	void toActivity() {
		final Activity actual = new UserCreatedEvent(getUser(), 1L).toActivity();
		final Activity expected = getExpectedActivity();
		checkActivity(expected, actual);

	}

	private static UserActivityResource getUser() {
		UserActivityResource user = new UserActivityResource();
		user.setId(2L);
		user.setFullName("Jaja Juja");
		user.setDefaultProjectId(3L);
		return user;
	}

	private static Activity getExpectedActivity() {
		Activity activity = new Activity();
		activity.setAction(ActivityAction.CREATE_USER.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.USER);
		activity.setUserId(1L);
		activity.setProjectId(3L);
		activity.setObjectId(2L);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new ActivityDetails("Jaja Juja"));
		return activity;
	}
}