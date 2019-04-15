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
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.USER;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.CREATE_USER;

/**
 * @author Andrei Varabyeu
 */
public class UserCreatedEvent implements ActivityEvent {

	private UserActivityResource userActivityResource;
	private Long creatorId;
	private String creatorLogin;

	public UserCreatedEvent() {
	}

	public UserCreatedEvent(UserActivityResource userActivityResource, Long creatorId, String creatorLogin) {
		this.userActivityResource = userActivityResource;
		this.creatorId = creatorId;
		this.creatorLogin = creatorLogin;
	}

	public UserActivityResource getUserActivityResource() {
		return userActivityResource;
	}

	public void setUserActivityResource(UserActivityResource userActivityResource) {
		this.userActivityResource = userActivityResource;
	}

	public Long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Long creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreatorLogin() {
		return creatorLogin;
	}

	public void setCreatorLogin(String creatorLogin) {
		this.creatorLogin = creatorLogin;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(CREATE_USER)
				.addActivityEntityType(USER).addUserId(creatorId).addUserName(creatorLogin)
				.addObjectId(userActivityResource.getId())
				.addObjectName(userActivityResource.getFullName())
				.addProjectId(userActivityResource.getDefaultProjectId())
				.get();
	}
}
