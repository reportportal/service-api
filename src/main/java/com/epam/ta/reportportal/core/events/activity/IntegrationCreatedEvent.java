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
import com.epam.ta.reportportal.entity.integration.Integration;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class IntegrationCreatedEvent implements ActivityEvent {

	private final Integration integration;
	private final Long createdBy;

	public IntegrationCreatedEvent(Integration integration, Long createdBy) {
		this.integration = integration;
		this.createdBy = createdBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.CREATE_BTS.getValue());
		activity.setEntity(Activity.Entity.INTEGRATION);
		activity.setProjectId(integration.getProject().getId());
		activity.setUserId(createdBy);
		activity.setObjectId(integration.getId());
		activity.setDetails(new ActivityDetails(integration.getType().getName() + ":" + integration.getProject().getName()));
		return activity;
	}
}
