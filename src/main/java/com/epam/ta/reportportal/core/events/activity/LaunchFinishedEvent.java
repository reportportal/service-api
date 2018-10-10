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
