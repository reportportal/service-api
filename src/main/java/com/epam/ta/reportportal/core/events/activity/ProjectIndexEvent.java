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

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PROJECT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.DELETE_INDEX;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.GENERATE_INDEX;

/**
 * @author Pavel Bortnik
 */
public class ProjectIndexEvent implements ActivityEvent {

	private Long projectId;
	private String projectName;
	private Long userId;
	private boolean indexing;

	public ProjectIndexEvent() {
	}

	public ProjectIndexEvent(Long projectId, String projectName, Long userId, boolean indexing) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.userId = userId;
		this.indexing = indexing;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isIndexing() {
		return indexing;
	}

	public void setIndexing(boolean indexing) {
		this.indexing = indexing;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(indexing ? GENERATE_INDEX : DELETE_INDEX)
				.addActivityEntityType(PROJECT)
				.addUserId(userId)
				.addObjectId(projectId)
				.addObjectName(projectName)
				.addProjectId(projectId)
				.get();
	}
}
