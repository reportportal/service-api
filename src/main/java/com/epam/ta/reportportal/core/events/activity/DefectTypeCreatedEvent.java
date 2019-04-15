/*
 * Copyright 2019 EPAM Systems
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
import static com.epam.ta.reportportal.entity.activity.ActivityAction.CREATE_DEFECT;

/**
 * @author Andrei Varabyeu
 */
public class DefectTypeCreatedEvent implements ActivityEvent {

	private IssueTypeActivityResource issueTypeActivityResource;
	private Long userId;
	private String userLogin;
	private Long projectId;

	public DefectTypeCreatedEvent() {
	}

	public DefectTypeCreatedEvent(IssueTypeActivityResource issueTypeActivityResource, Long userId, String userLogin, Long projectId) {
		this.issueTypeActivityResource = issueTypeActivityResource;
		this.userId = userId;
		this.userLogin = userLogin;
		this.projectId = projectId;
	}

	public IssueTypeActivityResource getIssueTypeActivityResource() {
		return issueTypeActivityResource;
	}

	public void setIssueTypeActivityResource(IssueTypeActivityResource issueTypeActivityResource) {
		this.issueTypeActivityResource = issueTypeActivityResource;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(CREATE_DEFECT)
				.addActivityEntityType(DEFECT_TYPE).addUserId(userId).addUserName(userLogin)
				.addObjectId(issueTypeActivityResource.getId())
				.addObjectName(issueTypeActivityResource.getLongName())
				.addProjectId(projectId)
				.get();
	}
}
