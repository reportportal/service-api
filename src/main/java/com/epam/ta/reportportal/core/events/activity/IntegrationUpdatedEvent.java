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
public class IntegrationUpdatedEvent implements ActivityEvent {

	private Integration integration;
	private Long updatedBy;

	public IntegrationUpdatedEvent() {
	}

	public IntegrationUpdatedEvent(Integration integration, Long updatedBy) {
		this.integration = integration;
		this.updatedBy = updatedBy;
	}

	public Integration getIntegration() {
		return integration;
	}

	public void setIntegration(Integration integration) {
		this.integration = integration;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_BTS.getValue());
		activity.setEntity(Activity.Entity.INTEGRATION);
		activity.setProjectId(integration.getProject().getId());
		activity.setUserId(updatedBy);
		activity.setObjectId(integration.getId());
		activity.setDetails(new ActivityDetails(integration.getType().getName() + ":" + integration.getProject().getName()));
		return activity;
	}
}
