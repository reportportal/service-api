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
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.IntegrationActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.INTEGRATION;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.CREATE_INTEGRATION;

/**
 * @author Andrei Varabyeu
 */
public class IntegrationCreatedEvent extends AbstractEvent implements ActivityEvent {

	private IntegrationActivityResource integrationActivityResource;

	public IntegrationCreatedEvent() {
	}

	public IntegrationCreatedEvent(IntegrationActivityResource integrationActivityResource, Long userId, String userLogin) {
		super(userId, userLogin);
		this.integrationActivityResource = integrationActivityResource;
	}

	public IntegrationActivityResource getIntegrationActivityResource() {
		return integrationActivityResource;
	}

	public void setIntegrationActivityResource(IntegrationActivityResource integrationActivityResource) {
		this.integrationActivityResource = integrationActivityResource;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(CREATE_INTEGRATION)
				.addActivityEntityType(INTEGRATION)
				.addUserId(getUserId())
				.addUserName(getUserLogin())
				.addObjectId(integrationActivityResource.getId())
				.addObjectName(integrationActivityResource.getTypeName())
				.addProjectId(integrationActivityResource.getProjectId())
				.get();
	}
}
