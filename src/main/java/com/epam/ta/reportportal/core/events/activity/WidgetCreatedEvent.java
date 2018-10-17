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
import com.epam.ta.reportportal.entity.widget.Widget;

import java.time.LocalDateTime;

/**
 * @author pavel_bortnik
 */
public class WidgetCreatedEvent implements ActivityEvent {

	private Widget widget;
	private Long createdBy;

	public WidgetCreatedEvent() {
	}

	public WidgetCreatedEvent(Widget widget, Long createdBy) {
		this.widget = widget;
		this.createdBy = createdBy;
	}

	public Widget getWidget() {
		return widget;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
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
		activity.setAction(ActivityAction.CREATE_WIDGET.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.WIDGET);
		activity.setUserId(createdBy);
		activity.setProjectId(widget.getProject().getId());
		activity.setObjectId(widget.getId());
		activity.setDetails(new ActivityDetails(widget.getName()));
		return activity;
	}

}
