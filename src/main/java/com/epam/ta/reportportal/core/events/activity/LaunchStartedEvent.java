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

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class LaunchStartedEvent implements ActivityEvent {

	private Long launchId;
	private Long userId;
	private Long projectId;

	public LaunchStartedEvent() {
	}

	public LaunchStartedEvent(Long launchId, Long userId, Long projectId) {
		this.launchId = launchId;
		this.userId = userId;
		this.projectId = projectId;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setUserId(userId);
		activity.setEntity(Activity.Entity.LAUNCH);
		activity.setProjectId(projectId);
		activity.setAction(ActivityAction.START_LAUNCH.getValue());
		activity.setCreatedAt(LocalDateTime.now());
		activity.setDetails(new LaunchFinishedEvent.LaunchActivityDetails(launchId));
		return activity;
	}
}
