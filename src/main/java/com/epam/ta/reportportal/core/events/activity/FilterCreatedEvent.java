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
import com.epam.ta.reportportal.ws.model.activity.UserFilterActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.FILTER;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.CREATE_FILTER;

/**
 * @author pavel_bortnik
 */
public class FilterCreatedEvent extends AbstractEvent implements ActivityEvent {

	private UserFilterActivityResource userFilterActivityResource;

	public FilterCreatedEvent() {
	}

	public FilterCreatedEvent(UserFilterActivityResource userFilterActivityResource, Long userId, String userLogin) {
		super(userId, userLogin);
		this.userFilterActivityResource = userFilterActivityResource;
	}

	public UserFilterActivityResource getUserFilterActivityResource() {
		return userFilterActivityResource;
	}

	public void setUserFilterActivityResource(UserFilterActivityResource userFilterActivityResource) {
		this.userFilterActivityResource = userFilterActivityResource;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(CREATE_FILTER)
				.addActivityEntityType(FILTER)
				.addUserId(getUserId())
				.addUserName(getUserLogin())
				.addObjectId(userFilterActivityResource.getId())
				.addObjectName(userFilterActivityResource.getName())
				.addProjectId(userFilterActivityResource.getProjectId())
				.get();
	}
}
