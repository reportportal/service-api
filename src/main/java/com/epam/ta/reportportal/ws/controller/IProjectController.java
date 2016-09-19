/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.ws.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.epam.ta.reportportal.ws.model.preference.UpdatePreferenceRQ;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.model.project.email.UpdateProjectEmailRQ;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.data.domain.Pageable;

/**
 * @author Hanna_Sukhadolava
 * 
 */
public interface IProjectController {

	/**
	 * Create new {@link Project} instance
	 * 
	 * @param createProjectRQ
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	EntryCreatedRS createProject(CreateProjectRQ createProjectRQ, Principal principal);

	/**
	 * Update {@link Project} instance
	 * 
	 * @param projectName
	 * @param updateProjectRQ
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateProject(String projectName, UpdateProjectRQ updateProjectRQ, Principal principal);

	/**
	 * Update {@link Project} instance with specified email configuration
	 * 
	 * @see {@link ProjectEmailConfig}}
	 * 
	 * @param projectName
	 * @param updateProjectRQ
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateProjectEmailConfig(String projectName, UpdateProjectEmailRQ updateProjectRQ, Principal principal);

	/**
	 * Delete {@link Project} instance
	 * 
	 * @param projectName
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	OperationCompletionRS deleteProject(String projectName, Principal principal);

	/**
	 * Get list of {@link com.epam.ta.reportportal.ws.model.user.UserResource}
	 * for UI page
	 * 
	 * @param projectName
	 * @return
	 */
	Iterable<UserResource> getProjectUsers(String projectName, Filter filter, Pageable pageable, Principal principal);

	/**
	 * Get {@link ProjectResource} instance
	 * 
	 * @param project
	 * @return
	 */
	ProjectResource getProject(String project, Principal principal);

	/**
	 * Un-assign users from project
	 * 
	 * @param projectName
	 * @param unassignUsersRQ
	 * @param principal
	 * @return
	 */
	OperationCompletionRS unassignProjectUsers(String projectName, UnassignUsersRQ unassignUsersRQ, Principal principal);

	/**
	 * Assign users to project
	 * 
	 * @param projectName
	 * @param assignUsersRQ
	 * @param principal
	 * @return
	 */
	OperationCompletionRS assignProjectUsers(String projectName, AssignUsersRQ assignUsersRQ, Principal principal);

	/**
	 * Get users available for assign to specified project
	 * 
	 * @param filter
	 * @param projectName
	 * @param principal
	 * @return
	 */
	Iterable<UserResource> getUsersForAssign(Filter filter, Pageable pageable, String projectName, Principal principal);

	/**
	 * Get specified project usernames
	 * 
	 * @param projectName
	 * @param value
	 * @param principal
	 * @return
	 */
	List<String> getProjectUsers(String projectName, String value, Principal principal);

	/**
	 * Get user preference
	 * 
	 * @param projectName
	 * @param userName
	 * @param principal
	 * @return
	 */
	PreferenceResource getUserPreference(String projectName, String userName, Principal principal);

	/**
	 * Update user preference
	 * 
	 * @param projectName
	 * @param rq
	 * @param login
	 * @param principal
	 * @return
	 */
	OperationCompletionRS updateUserPreference(String projectName, UpdatePreferenceRQ rq, String login, Principal principal);

	/**
	 * Get information about all projects
	 * 
	 * @param principal
	 * @return
	 */
	Iterable<ProjectInfoResource> getAllProjectsInfo(Principal principal);

	/**
	 * Get project information
	 * 
	 * @param projectId
	 * @return
	 */
	ProjectInfoResource getProjectInfo(String projectId, String interval, Principal principal);

	Map<String, List<ChartObject>> getProjectWidget(String projectId, String interval, String widgetId, Principal principal);

	/**
	 * Get names of all projects
	 * 
	 * @param principal
	 * @return
	 */
	Iterable<String> getAllProjectNames(Principal principal);
}