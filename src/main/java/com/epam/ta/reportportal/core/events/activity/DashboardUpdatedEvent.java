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
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.DASHBOARD;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_DASHBOARD;

/**
 * @author Andrei Varabyeu
 */
public class DashboardUpdatedEvent extends AroundEvent<DashboardActivityResource> implements ActivityEvent {

	private Long updatedBy;

	public DashboardUpdatedEvent() {
	}

	public DashboardUpdatedEvent(DashboardActivityResource before, DashboardActivityResource after, Long updatedBy) {
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
		return new ActivityBuilder().addCreatedNow()
				.addAction(UPDATE_DASHBOARD)
				.addActivityEntityType(DASHBOARD)
				.addUserId(updatedBy)
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getName())
				.addProjectId(getAfter().getProjectId())
				.addHistoryField(processShared(getBefore().isShared(), getAfter().isShared()))
				.addHistoryField(processName(getBefore().getName(), getAfter().getName()))
				.addHistoryField(processDescription(getBefore().getDescription(), getAfter().getDescription()))
				.get();
	}
}
