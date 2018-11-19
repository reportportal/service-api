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
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.UserFilterActivityResource;

import static com.epam.ta.reportportal.core.events.activity.ActivityAction.CREATE_FILTER;
import static com.epam.ta.reportportal.entity.Activity.ActivityEntityType.FILTER;

/**
 * @author pavel_bortnik
 */
public class FilterCreatedEvent implements ActivityEvent {

	private UserFilterActivityResource userFilterActivityResource;
	private Long createdBy;

	public FilterCreatedEvent() {
	}

	public FilterCreatedEvent(UserFilterActivityResource userFilterActivityResource, Long createdBy) {
		this.userFilterActivityResource = userFilterActivityResource;
		this.createdBy = createdBy;
	}

	public UserFilterActivityResource getUserFilterActivityResource() {
		return userFilterActivityResource;
	}

	public void setUserFilterActivityResource(UserFilterActivityResource userFilterActivityResource) {
		this.userFilterActivityResource = userFilterActivityResource;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(CREATE_FILTER)
				.addActivityEntityType(FILTER)
				.addUserId(createdBy)
				.addObjectId(userFilterActivityResource.getId())
				.addObjectName(userFilterActivityResource.getName())
				.addProjectId(userFilterActivityResource.getProjectId())
				.get();
	}
}
