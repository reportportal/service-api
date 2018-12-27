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
import com.epam.ta.reportportal.ws.model.activity.IssueTypeActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.DEFECT_TYPE;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_DEFECT;

/**
 * @author Andrei Varabyeu
 */
public class DefectTypeUpdatedEvent implements ActivityEvent {

	private IssueTypeActivityResource issueTypeActivityResource;
	private Long updatedBy;
	private Long projectId;

	public DefectTypeUpdatedEvent() {
	}

	public DefectTypeUpdatedEvent(IssueTypeActivityResource issueTypeActivityResource, Long updatedBy, Long projectId) {
		this.issueTypeActivityResource = issueTypeActivityResource;
		this.updatedBy = updatedBy;
		this.projectId = projectId;
	}

	public IssueTypeActivityResource getIssueTypeActivityResource() {
		return issueTypeActivityResource;
	}

	public void setIssueTypeActivityResource(IssueTypeActivityResource issueTypeActivityResource) {
		this.issueTypeActivityResource = issueTypeActivityResource;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(UPDATE_DEFECT)
				.addActivityEntityType(DEFECT_TYPE)
				.addUserId(updatedBy)
				.addObjectId(issueTypeActivityResource.getId())
				.addObjectName(issueTypeActivityResource.getLongName())
				.addProjectId(projectId)
				.get();
	}
}
