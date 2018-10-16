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
import com.epam.ta.reportportal.ws.model.project.config.UpdateOneIssueSubTypeRQ;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class DefectTypeUpdatedEvent implements ActivityEvent {

	private Long projectId;
	private Long updatedBy;
	private UpdateOneIssueSubTypeRQ request;

	public DefectTypeUpdatedEvent() {
	}

	public DefectTypeUpdatedEvent(Long projectId, Long updatedBy, UpdateOneIssueSubTypeRQ request) {
		this.projectId = projectId;
		this.updatedBy = updatedBy;
		this.request = request;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	public UpdateOneIssueSubTypeRQ getRequest() {
		return request;
	}

	public void setRequest(UpdateOneIssueSubTypeRQ request) {
		this.request = request;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_DEFECT.toString());
		activity.setEntity(Activity.Entity.DEFECT_TYPE);
		activity.setProjectId(projectId);
		activity.setUserId(updatedBy);
		activity.setObjectId(Long.valueOf(request.getId()));
		activity.setDetails(new ActivityDetails(request.getLongName()));
		return activity;
	}
}
