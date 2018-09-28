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
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.item.TestItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class TicketAttachedEvent extends AroundEvent<List<TestItem>> implements ActivityEvent {

	private final Long attachedBy;
	private final Long projectId;

	public TicketAttachedEvent(List<TestItem> before, List<TestItem> after, Long attachedBy, Long projectId) {
		super(before, after);
		this.attachedBy = attachedBy;
		this.projectId = projectId;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		//		No activity action
		//		activity.setAction(ActivityAction.);
		activity.setEntity(Activity.Entity.TICKET);
		activity.setUserId(attachedBy);
		activity.setProjectId(projectId);
		return activity;
	}
}
