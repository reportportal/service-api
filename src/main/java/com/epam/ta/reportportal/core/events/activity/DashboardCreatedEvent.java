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
import com.epam.ta.reportportal.entity.dashboard.Dashboard;

import java.time.LocalDateTime;

/**
 * @author pavel_bortnik
 */
public class DashboardCreatedEvent implements ActivityEvent {

	private Dashboard dashboard;
	private Long createdBy;

	public DashboardCreatedEvent() {
	}

	public DashboardCreatedEvent(Dashboard dashboard, Long createdBy) {
		this.dashboard = dashboard;
		this.createdBy = createdBy;
	}

	public Dashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setEntity(Activity.Entity.DASHBOARD);
		activity.setAction(ActivityAction.CREATE_DASHBOARD.getValue());
		activity.setProjectId(dashboard.getProjectId());
		//add user id after acl implementation
		activity.setUserId(createdBy);
		activity.setObjectId(dashboard.getId());
		activity.setDetails(new ActivityDetails(dashboard.getName()));
		return activity;
	}
}
