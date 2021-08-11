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

package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Delete {@link com.epam.ta.reportportal.entity.project.Project} request handler
 *
 * @author Hanna_Sukhadolava
 */
public interface DeleteProjectHandler {

	/**
	 * Delete specified project
	 *
	 * @param projectId Project id
	 * @return Result of operation
	 * @throws ReportPortalException
	 */
	OperationCompletionRS deleteProject(Long projectId);

	/**
	 * Delete project index
	 *
	 * @param projectName Project name
	 * @param username    User name
	 */
	OperationCompletionRS deleteProjectIndex(String projectName, String username);

}