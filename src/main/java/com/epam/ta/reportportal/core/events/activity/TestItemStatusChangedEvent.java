/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.STATUS;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class TestItemStatusChangedEvent extends AroundEvent<TestItemActivityResource> implements ActivityEvent {

	private Long changedBy;

	public TestItemStatusChangedEvent() {
	}

	public TestItemStatusChangedEvent(TestItemActivityResource before, TestItemActivityResource after, Long changedBy) {
		super(before, after);
		this.changedBy = changedBy;
	}

	public Long getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(Long changedBy) {
		this.changedBy = changedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM);
		activity.setObjectId(getAfter().getId());
		activity.setAction(ActivityAction.UPDATE_ITEM.getValue());
		activity.setProjectId(getAfter().getProjectId());
		activity.setUserId(changedBy);
		activity.setCreatedAt(LocalDateTime.now());

		ActivityDetails details = new ActivityDetails(getAfter().getName());
		details.addHistoryField(HistoryField.of(STATUS, getBefore().getStatus(), getAfter().getStatus()));

		activity.setDetails(details);
		return activity;
	}
}
