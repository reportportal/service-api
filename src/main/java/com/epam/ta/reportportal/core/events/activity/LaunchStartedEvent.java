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
import com.epam.ta.reportportal.store.database.entity.Activity;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class LaunchStartedEvent implements ActivityEvent {

	private Launch launch;

	LaunchStartedEvent() {

	}

	public LaunchStartedEvent(Launch launch) {
		this.launch = launch;
	}

	public Launch getLaunch() {
		return launch;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setUserId(this.launch.getUserId());
		activity.setEntity(Activity.Entity.LAUNCH);
		activity.setAction(ActivityAction.START_LAUNCH.getValue());
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new LaunchFinishedEvent.LaunchActivityDetails(launch.getId()));
		return activity;
	}
}
