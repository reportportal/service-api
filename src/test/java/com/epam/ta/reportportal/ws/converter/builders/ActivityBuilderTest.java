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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.LAUNCH;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.FINISH_LAUNCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ActivityBuilderTest {

	@Test
	void activityBuilderTest() {
		final ActivityAction action = FINISH_LAUNCH;
		final Activity.ActivityEntityType entity = LAUNCH;
		final String objectName = "objectName";
		final Long projectId = 1L;
		final Long userId = 2L;
		final String username = "username";
		final LocalDateTime localDateTime = LocalDateTime.of(2019, 2, 6, 18, 25);
		final Long objectId = 3L;
		Activity activity = new ActivityBuilder().addAction(action)
				.addActivityEntityType(entity)
				.addObjectName(objectName)
				.addProjectId(projectId)
				.addUserId(userId)
				.addObjectId(objectId)
				.addUserName(username)
				.addCreatedAt(localDateTime)
				.addHistoryField("field", "before", "after")
				.get();

		assertEquals(action.getValue(), activity.getAction());
		assertEquals(entity.getValue(), activity.getActivityEntityType());
		assertEquals(objectName, activity.getDetails().getObjectName());
		assertEquals(projectId, activity.getProjectId());
		assertEquals(userId, activity.getUserId());
		assertEquals(objectId, activity.getObjectId());
		assertEquals(username, activity.getUsername());
		assertEquals(localDateTime, activity.getCreatedAt());
	}

	@Test
	void addDetailsTest() {
		ActivityDetails details = new ActivityDetails();
		details.setObjectName("name");
		final HistoryField historyFiled = HistoryField.of("field", "before", "after");
		details.addHistoryField(historyFiled);

		final Activity activity = new ActivityBuilder().addDetails(details).addCreatedNow().get();

		assertEquals("name", activity.getDetails().getObjectName());
		assertEquals(1, activity.getDetails().getHistory().size());
		assertEquals(historyFiled, activity.getDetails().getHistory().get(0));
		assertNotNull(activity.getCreatedAt());
	}
}