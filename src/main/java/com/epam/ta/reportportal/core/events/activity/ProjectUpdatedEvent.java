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

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Being triggered on after project update
 *
 * @author Andrei Varabyeu
 */
public class ProjectUpdatedEvent extends AroundEvent<Project> {

	private final String updatedBy;
	private final UpdateProjectRQ updateProjectRQ;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param before Project before update
	 * @param after  Project after update
	 */
	public ProjectUpdatedEvent(Project before, Project after, String updatedBy, UpdateProjectRQ updateProjectRQ) {
		super(before, after);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(updatedBy));
		this.updatedBy = updatedBy;
		this.updateProjectRQ = updateProjectRQ;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public UpdateProjectRQ getUpdateProjectRQ() {
		return updateProjectRQ;
	}
}
