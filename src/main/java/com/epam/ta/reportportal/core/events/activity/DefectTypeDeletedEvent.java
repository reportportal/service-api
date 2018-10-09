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
public class DefectTypeDeletedEvent implements ActivityEvent {

	private final IssueType issueType;
	private final Long projectId;
	private final Long deletedBy;

	public DefectTypeDeletedEvent(IssueType issueType, Long projectId, Long deletedBy) {
		this.issueType = issueType;
		this.projectId = projectId;
		this.deletedBy = deletedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.DELETE_DEFECT.getValue());
		activity.setEntity(Activity.Entity.DEFECT_TYPE);
		activity.setUserId(deletedBy);
		activity.setProjectId(projectId);
		activity.setObjectId(issueType.getId());
		activity.setDetails(new ActivityDetails(issueType.getLongName()));
		return activity;
	}
}
