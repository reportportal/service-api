/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.activity.details.ActivityDetails;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processDescription;
import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processName;

/**
 * @author Andrei Varabyeu
 */
public class DashboardUpdatedEvent extends BeforeEvent<Dashboard> implements ActivityEvent {

	private final Dashboard updated;
	private final Long updateBy;

	public DashboardUpdatedEvent(Dashboard before, Dashboard updated, Long updateBy) {
		super(before);
		this.updated = updated;
		this.updateBy = updateBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		//add user id after acl implementation
		activity.setCreatedAt(LocalDateTime.now());
		activity.setEntity(Activity.Entity.DASHBOARD);
		activity.setAction(ActivityAction.UPDATE_DASHBOARD.getValue());
		activity.setProjectId(updated.getProjectId());
		activity.setUserId(updateBy);

		ActivityDetails details = new ActivityDetails();
		processName(details, getBefore().getName(), updated.getName());
		processDescription(details, getBefore().getDescription(), updated.getDescription());

		activity.setDetails(details);
		return activity;
	}
}
