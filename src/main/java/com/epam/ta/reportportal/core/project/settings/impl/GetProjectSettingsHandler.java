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

package com.epam.ta.reportportal.core.project.settings.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.project.settings.IGetProjectSettingsHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class GetProjectSettingsHandler implements IGetProjectSettingsHandler {

	private ProjectRepository repository;

	public GetProjectSettingsHandler(ProjectRepository repository) {
		this.repository = repository;
	}

	@Override
	public ProjectSettingsResource getProjectSettings(ReportPortalUser.ProjectDetails projectDetails) {
		Project project = repository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		ProjectSettingsResource resource = new ProjectSettingsResource();
		resource.setProjectId(String.valueOf(project.getId()));
		resource.setSubTypes(ProjectConverter.TO_PROJECT_SUB_TYPES.apply(project.getIssueTypes()));
		return resource;
	}
}
