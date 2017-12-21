/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;

/**
 * Update {@link Project} request handler
 *
 * @author Hanna_Sukhadolava
 */
public interface IUpdateProjectHandler {

	/**
	 * Update specified project(projectName, customer and addInfo)
	 *
	 * @param projectName     Project Name
	 * @param updateProjectRQ Project data
	 * @param principalName   Login
	 * @return Operation result
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateProject(String projectName, UpdateProjectRQ updateProjectRQ, String principalName);

	/**
	 * Update specified project email configuration
	 *
	 * @param projectName     Project Name
	 * @param updateProjectRQ Request Data
	 * @param user            User performing that update
	 * @return Operation Result
	 */
	OperationCompletionRS updateProjectEmailConfig(String projectName, String user, ProjectEmailConfigDTO updateProjectRQ);

	/**
	 * Un-assign specified user from project
	 *
	 * @param projectName     Project Name
	 * @param modifier        Modifier
	 * @param unassignUsersRQ Request Data
	 * @return Operation Result
	 * @throws ReportPortalException
	 */
	OperationCompletionRS unassignUsers(String projectName, String modifier, UnassignUsersRQ unassignUsersRQ);

	/**
	 * Assign specified user from project
	 *
	 * @param projectName   Project Name
	 * @param modifier      Modifier
	 * @param assignUsersRQ Request Data
	 * @return Operation Result
	 * @throws ReportPortalException
	 */
	OperationCompletionRS assignUsers(String projectName, String modifier, AssignUsersRQ assignUsersRQ);
}