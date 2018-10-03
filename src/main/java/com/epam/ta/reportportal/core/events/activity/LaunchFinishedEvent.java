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
import com.epam.ta.reportportal.entity.JsonbObject;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.base.Preconditions;

import java.time.LocalDateTime;

/**
 * Lifecycle events.
 *
 * @author Andrei Varabyeu
 */
public class LaunchFinishedEvent implements ActivityEvent {

	private Launch launch;

	LaunchFinishedEvent() {

	}

	public LaunchFinishedEvent(Launch launch) {
		this.launch = Preconditions.checkNotNull(launch, "Should not be null");
	}

	public Launch getLaunch() {
		return launch;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setUserId(this.launch.getUser().getId());
		activity.setEntity(Activity.Entity.LAUNCH);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setProjectId(launch.getProjectId());
		activity.setDetails(new LaunchActivityDetails(launch.getId()));
		return activity;
	}

	public static class LaunchActivityDetails extends JsonbObject {
		private Long launchId;

		public LaunchActivityDetails() {

		}

		public LaunchActivityDetails(Long launchId) {
			this.launchId = launchId;
		}

		public Long getLaunchId() {
			return launchId;
		}

		public void setLaunchId(Long launchId) {
			this.launchId = launchId;
		}
	}
}
