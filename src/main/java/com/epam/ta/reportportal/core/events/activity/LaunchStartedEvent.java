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
import com.epam.ta.reportportal.entity.launch.Launch;

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
		activity.setUserId(this.launch.getUser().getId());
		activity.setEntity(Activity.Entity.LAUNCH);
		activity.setProjectId(launch.getProjectId());
		activity.setAction(ActivityAction.START_LAUNCH.getValue());
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new LaunchFinishedEvent.LaunchActivityDetails(launch.getId()));
		return activity;
	}
}
