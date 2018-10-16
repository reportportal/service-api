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
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.user.User;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class UserCreatedEvent implements ActivityEvent {

	private User user;
	private Long createdBy;

	public UserCreatedEvent() {
	}

	public UserCreatedEvent(User user, Long createdBy) {
		this.user = user;
		this.createdBy = createdBy;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.CREATE_USER.getValue());
		activity.setEntity(Activity.Entity.USER);
		activity.setUserId(createdBy);
		activity.setProjectId(user.getDefaultProject().getId());
		activity.setObjectId(user.getId());
		activity.setDetails(new ActivityDetails(user.getFullName()));
		return activity;
	}
}
