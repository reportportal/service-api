/*
 * Copyright (C) 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.project.DeleteProjectHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class DeleteProjectHandlerImpl implements DeleteProjectHandler {

	private final ProjectRepository projectRepository;

	@Autowired
	public DeleteProjectHandlerImpl(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public OperationCompletionRS deleteProject(String projectName) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		projectRepository.delete(project);
		return new OperationCompletionRS("Project with name = '" + projectName + "' is successfully deleted.");
	}

	@Override
	public OperationCompletionRS deleteProjectIndex(String projectName, String username) {
		return null;
	}
}
