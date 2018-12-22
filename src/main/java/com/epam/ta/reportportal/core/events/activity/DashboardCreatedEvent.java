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

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.DASHBOARD;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.CREATE_DASHBOARD;

/**
 * @author pavel_bortnik
 */
public class DashboardCreatedEvent implements ActivityEvent {

	private DashboardActivityResource dashboardActivityResource;
	private Long createdBy;

	public DashboardCreatedEvent() {
	}

	public DashboardCreatedEvent(DashboardActivityResource dashboardActivityResource, Long createdBy) {
		this.dashboardActivityResource = dashboardActivityResource;
		this.createdBy = createdBy;
	}

	public DashboardActivityResource getDashboardActivityResource() {
		return dashboardActivityResource;
	}

	public void setDashboardActivityResource(DashboardActivityResource dashboardActivityResource) {
		this.dashboardActivityResource = dashboardActivityResource;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(CREATE_DASHBOARD)
				.addActivityEntityType(DASHBOARD)
				.addUserId(createdBy)
				.addProjectId(dashboardActivityResource.getProjectId())
				.addObjectId(dashboardActivityResource.getId())
				.addObjectName(dashboardActivityResource.getName())
				.get();
	}
}
