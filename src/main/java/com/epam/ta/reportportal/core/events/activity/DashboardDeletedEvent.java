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
public class DashboardDeletedEvent extends BeforeEvent<Dashboard> implements ActivityEvent {

	private Long removedBy;

	public DashboardDeletedEvent() {
	}

	public DashboardDeletedEvent(Dashboard before, Long removedBy) {
		super(before);
		this.removedBy = removedBy;
	}

	public Long getRemovedBy() {
		return removedBy;
	}

	public void setRemovedBy(Long removedBy) {
		this.removedBy = removedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setActivityEntityType(Activity.ActivityEntityType.DASHBOARD);
		activity.setAction(ActivityAction.DELETE_DASHBOARD.getValue());
		activity.setProjectId(getBefore().getProjectId());
		//add user id after acl implementation
		activity.setUserId(removedBy);
		activity.setObjectId(getBefore().getId());
		activity.setDetails(new ActivityDetails(getBefore().getName()));
		return activity;
	}
}
