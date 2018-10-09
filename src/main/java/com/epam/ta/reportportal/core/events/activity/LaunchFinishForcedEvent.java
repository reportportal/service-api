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
