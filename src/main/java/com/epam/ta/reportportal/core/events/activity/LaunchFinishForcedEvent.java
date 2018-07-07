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
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.launch.Launch;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class LaunchFinishForcedEvent implements ActivityEvent {

	private Launch launch;
	private Long forcedBy;

	public LaunchFinishForcedEvent(Launch launch, Long forcedBy) {
		this.launch = launch;
		this.forcedBy = forcedBy;
	}

	LaunchFinishForcedEvent() {

	}

	public Launch getLaunch() {
		return launch;
	}

	public Long getForcedBy() {
		return forcedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		//		activity.setAction();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setEntity(Activity.Entity.LAUNCH);
		activity.setUserId(forcedBy);
		activity.setDetails(new LaunchFinishedEvent.LaunchActivityDetails(launch.getId()));
		return activity;
	}
}
