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
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;

import static com.epam.ta.reportportal.core.events.activity.ProjectAnalyzerConfigEvent.processParameter;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PROJECT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_PROJECT;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;

/**
 * Being triggered on after project update
 *
 * @author Andrei Varabyeu
 */
public class ProjectUpdatedEvent extends AroundEvent<ProjectAttributesActivityResource> implements ActivityEvent {

	private Long userId;
	private String userLogin;

	public ProjectUpdatedEvent() {
	}

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param before Project before update
	 * @param after  Project after update
	 */
	public ProjectUpdatedEvent(ProjectAttributesActivityResource before, ProjectAttributesActivityResource after, Long userId,
			String userLogin) {
		super(before, after);
		this.userId = userId;
		this.userLogin = userLogin;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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
				.addAction(UPDATE_PROJECT)
				.addActivityEntityType(PROJECT).addUserId(userId).addUserName(userLogin)
				.addObjectId(getAfter().getProjectId())
				.addObjectName(getAfter().getProjectName())
				.addProjectId(getAfter().getProjectId())
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), INTERRUPT_JOB_TIME.getAttribute()))
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), KEEP_SCREENSHOTS.getAttribute()))
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), KEEP_LOGS.getAttribute()))
				.get();
	}
}
