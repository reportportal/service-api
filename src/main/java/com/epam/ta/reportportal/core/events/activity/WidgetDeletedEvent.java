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
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.WIDGET;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.DELETE_WIDGET;

/**
 * @author pavel_bortnik
 */
public class WidgetDeletedEvent extends BeforeEvent<WidgetActivityResource> implements ActivityEvent {

	private Long deletedBy;

	public WidgetDeletedEvent() {
	}

	public WidgetDeletedEvent(WidgetActivityResource before, Long deletedBy) {
		super(before);
		this.deletedBy = deletedBy;
	}

	public Long getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(Long deletedBy) {
		this.deletedBy = deletedBy;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(DELETE_WIDGET)
				.addActivityEntityType(WIDGET)
				.addUserId(deletedBy)
				.addObjectId(getBefore().getId())
				.addObjectName(getBefore().getName())
				.addProjectId(getBefore().getProjectId())
				.get();
	}
}
