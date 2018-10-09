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
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.item.issue.IssueType;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class DefectTypeCreatedEvent implements ActivityEvent {

	private final IssueType issueType;
	private final Long projectId;
	private final Long updatedBy;

	public DefectTypeCreatedEvent(IssueType issueType, Long projectId, Long updatedBy) {
		this.issueType = issueType;
		this.projectId = projectId;
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		// why UPDATE_DEFECT?
		activity.setAction(ActivityAction.UPDATE_DEFECT.getValue());
		activity.setEntity(Activity.Entity.DEFECT_TYPE);
		activity.setUserId(updatedBy);
		activity.setProjectId(projectId);
		activity.setObjectId(issueType.getId());
		activity.setDetails(new ActivityDetails(issueType.getLongName()));
		return activity;
	}
}
