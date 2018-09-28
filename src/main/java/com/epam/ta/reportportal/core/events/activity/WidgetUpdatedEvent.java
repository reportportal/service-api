/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.activity.details.ActivityDetails;
import com.epam.ta.reportportal.core.events.activity.details.HistoryField;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processDescription;
import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.processName;

/**
 * @author Andrei Varabyeu
 */
public class WidgetUpdatedEvent extends BeforeEvent<Widget> implements ActivityEvent {

	private final Widget updated;
	private final Long updatedBy;

	public WidgetUpdatedEvent(Widget before, Widget updated, Long updatedBy) {
		super(before);
		this.updated = updated;
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_WIDGET.toString());
		activity.setEntity(Activity.Entity.WIDGET);
		activity.setUserId(updatedBy);
		activity.setProjectId(updated.getId());

		ActivityDetails details = new ActivityDetails();
		processName(details, getBefore().getName(), updated.getName());
		processDescription(details, getBefore().getDescription(), updated.getDescription());

		activity.setDetails(details);
		return activity;
	}
}
