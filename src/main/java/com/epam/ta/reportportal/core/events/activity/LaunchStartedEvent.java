/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.LAUNCH;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.START_LAUNCH;

/**
 * @author Andrei Varabyeu
 */
public class LaunchStartedEvent implements ActivityEvent {

	private LaunchActivityResource launchActivityResource;
	private Long userId;
	private String userLogin;

	public LaunchStartedEvent() {
	}

	public LaunchStartedEvent(LaunchActivityResource launchActivityResource, Long userId, String userLogin) {
		this.launchActivityResource = launchActivityResource;
		this.userId = userId;
		this.userLogin = userLogin;
	}

	public LaunchActivityResource getLaunchActivityResource() {
		return launchActivityResource;
	}

	public void setLaunchActivityResource(LaunchActivityResource launchActivityResource) {
		this.launchActivityResource = launchActivityResource;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(START_LAUNCH)
				.addActivityEntityType(LAUNCH).addUserId(userId).addUserName(userLogin)
				.addObjectId(launchActivityResource.getId())
				.addObjectName(launchActivityResource.getName())
				.addProjectId(launchActivityResource.getProjectId())
				.get();
	}
}
