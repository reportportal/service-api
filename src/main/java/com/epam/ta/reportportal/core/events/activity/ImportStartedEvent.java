/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.ActivityDetails;

import java.time.LocalDateTime;

/**
 * @author Pavel Bortnik
 */
public class ImportStartedEvent implements ActivityEvent {

	private final Long projectId;
	private final Long userId;
	private final String fileName;

	public ImportStartedEvent(Long projectId, Long userId, String fileName) {
		this.projectId = projectId;
		this.userId = userId;
		this.fileName = fileName;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.START_IMPORT.getValue());
		activity.setEntity(Activity.Entity.IMPORT);
		activity.setUserId(userId);
		activity.setProjectId(projectId);

		activity.setDetails(new ActivityDetails(fileName));
		return activity;
	}
}
