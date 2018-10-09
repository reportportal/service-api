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
