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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.epam.ta.reportportal.ws.model.preference.UpdatePreferenceRQ;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * @author Hanna_Sukhadolava
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
	 * @param projectName
	 * @param updateProjectRQ
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 * @see {@link ProjectEmailConfigDTO}}
	 */
	OperationCompletionRS updateProjectEmailConfig(String projectName, ProjectEmailConfigDTO updateProjectRQ, Principal principal);

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
	 * Searches for user across all db
	 *
	 * @param projectName Project Name
	 * @param term        Search Term
	 * @param pageable    Page request
	 * @return Page of users
	 */
	Page<UserResource> searchForUser(String projectName, String term, Pageable pageable);

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
	Iterable<ProjectInfoResource> getAllProjectsInfo(Filter filter, Pageable pageable, Principal principal);

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