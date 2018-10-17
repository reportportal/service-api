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

import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processDescription;
import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processName;

/**
 * @author Andrei Varabyeu
 */
public class DashboardUpdatedEvent extends AroundEvent<Dashboard> implements ActivityEvent {

	private Long updateBy;

	public DashboardUpdatedEvent() {
	}

	public DashboardUpdatedEvent(Dashboard before, Dashboard after, Long updateBy) {
		super(before, after);
		this.updateBy = updateBy;
	}

	public Long getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(Long updateBy) {
		this.updateBy = updateBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		//add user id after acl implementation
		activity.setCreatedAt(LocalDateTime.now());
		activity.setActivityEntityType(Activity.ActivityEntityType.DASHBOARD);
		activity.setAction(ActivityAction.UPDATE_DASHBOARD.getValue());
		activity.setProjectId(getBefore().getProjectId());
		activity.setUserId(updateBy);
		activity.setObjectId(getAfter().getId());

		ActivityDetails details = new ActivityDetails(getAfter().getName());
		//processShare
		processName(details, getBefore().getName(), getAfter().getName());
		processDescription(details, getBefore().getDescription(), getAfter().getDescription());

		activity.setDetails(details);
		return activity;
	}
}
