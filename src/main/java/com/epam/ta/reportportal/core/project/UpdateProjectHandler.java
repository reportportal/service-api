/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;

/**
 * Update {@link com.epam.ta.reportportal.entity.project.Project} request handler
 *
 * @author Hanna_Sukhadolava
 */
public interface UpdateProjectHandler {

	/**
	 * Update specified project(projectName, customer and addInfo)
	 *
	 * @param projectName     {@link com.epam.ta.reportportal.entity.project.Project#name}
	 * @param updateProjectRQ Project data
	 * @param user            ReportPortal user
	 * @return Operation result
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateProject(String projectName, UpdateProjectRQ updateProjectRQ, ReportPortalUser user);

	/**
	 * Update specified project email configuration
	 *
	 * @param projectName                       Project Name
	 * @param updateProjectNotificationConfigRQ Request Data
	 * @param user                              User performing that update
	 * @return Operation Result
	 */
	OperationCompletionRS updateProjectNotificationConfig(String projectName, ReportPortalUser user,
			ProjectNotificationConfigDTO updateProjectNotificationConfigRQ);

	/**
	 * Un-assign specified user from project
	 *
	 * @param projectName     {@link com.epam.ta.reportportal.entity.project.Project#name}
	 * @param modifier        Modifier User
	 * @param unassignUsersRQ Request Data
	 * @return Operation Result
	 * @throws ReportPortalException
	 */
	OperationCompletionRS unassignUsers(String projectName, UnassignUsersRQ unassignUsersRQ, ReportPortalUser modifier);

	/**
	 * Assign specified user from project
	 *
	 * @param projectName   {@link com.epam.ta.reportportal.entity.project.Project#name}
	 * @param modifier      Modifier User
	 * @param assignUsersRQ Request Data
	 * @return Operation Result
	 */
	OperationCompletionRS assignUsers(String projectName, AssignUsersRQ assignUsersRQ, ReportPortalUser modifier);

	/**
	 * Index logs for specified project
	 *
	 * @param projectDetails Project details
	 * @param user           User
	 * @return Operation Result
	 */
	OperationCompletionRS indexProjectData(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);
}