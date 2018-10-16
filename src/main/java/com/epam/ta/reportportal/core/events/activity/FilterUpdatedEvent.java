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

import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processDescription;
import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processName;

/**
 * @author Pavel Bortnik
 */
public class FilterUpdatedEvent extends AroundEvent<UserFilter> implements ActivityEvent {

	private Long updatedBy;

	public FilterUpdatedEvent() {
	}

	public FilterUpdatedEvent(UserFilter before, UserFilter after, Long updatedBy) {
		super(before, after);
		this.updatedBy = updatedBy;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_FILTER.getValue());
		activity.setEntity(Activity.Entity.FILTER);
		// before or after?
		activity.setProjectId(getBefore().getId());
		activity.setUserId(updatedBy);
		activity.setObjectId(getAfter().getId());

		ActivityDetails details = new ActivityDetails(getAfter().getName());
		processName(details, getBefore().getName(), getAfter().getName());
		processDescription(details, getBefore().getDescription(), getAfter().getDescription());

		activity.setDetails(details);
		return activity;
	}
}
