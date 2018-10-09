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

import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;

/**
 * @author Andrei Varabyeu
 */
public class DefectTypeUpdatedEvent {

	private final String project;
	private final String updatedBy;
	private final UpdateIssueSubTypeRQ request;

	public DefectTypeUpdatedEvent(String project, String updatedBy, UpdateIssueSubTypeRQ request) {
		this.project = project;
		this.updatedBy = updatedBy;
		this.request = request;
	}

	public String getProject() {
		return project;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public UpdateIssueSubTypeRQ getRequest() {
		return request;
	}
}
