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
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;

import static com.epam.ta.reportportal.core.events.activity.ActivityAction.FINISH_IMPORT;
import static com.epam.ta.reportportal.entity.Activity.ActivityEntityType.IMPORT;

/**
 * @author Pavel Bortnik
 */
public class ImportFinishedEvent implements ActivityEvent {

	private Long projectId;
	private Long userId;
	private String fileName;

	public ImportFinishedEvent() {
	}

	public ImportFinishedEvent(Long projectId, Long userId, String fileName) {
		this.projectId = projectId;
		this.userId = userId;
		this.fileName = fileName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(FINISH_IMPORT)
				.addActivityEntityType(IMPORT)
				.addUserId(userId)
				.addProjectId(projectId)
				.addObjectName(fileName)
				.get();
	}
}
