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
import com.epam.ta.reportportal.entity.filter.UserFilter;

import java.time.LocalDateTime;

/**
 * @author pavel_bortnik
 */
public class FilterCreatedEvent implements ActivityEvent {

	private UserFilter filter;
	private Long createdBy;

	public FilterCreatedEvent() {
	}

	public FilterCreatedEvent(UserFilter filter, Long createdBy) {
		this.filter = filter;
		this.createdBy = createdBy;
	}

	public UserFilter getFilter() {
		return filter;
	}

	public void setFilter(UserFilter filter) {
		this.filter = filter;
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
		activity.setActivityEntityType(Activity.ActivityEntityType.FILTER);
		activity.setAction(ActivityAction.CREATE_FILTER.getValue());
		activity.setProjectId(filter.getProject().getId());
		activity.setUserId(createdBy);
		activity.setObjectId(filter.getId());
		activity.setDetails(new ActivityDetails(filter.getName()));
		return activity;
	}
}
