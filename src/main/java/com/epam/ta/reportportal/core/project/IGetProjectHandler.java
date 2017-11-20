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

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author Andrei_Ramanchuk
 */
public interface IGetProjectHandler {

	/**
	 * Get project users info
	 *
	 * @param project Project name
	 * @return list of {@link UserResource}
	 */
	Iterable<UserResource> getProjectUsers(String project, Filter filter, Pageable pageable);

	/**
	 * Get project resource information
	 *
	 * @param project Project name
	 * @return {@link ProjectResource}
	 */
	ProjectResource getProject(String project);

	/**
	 * Get list of specified usernames
	 *
	 * @param project Project name
	 * @param value   Login
	 * @return List of found user logins
	 */
	List<String> getUserNames(String project, String value);

	/**
	 * Performs global search for user
	 *
	 * @param value login OR full name of user
	 * @return List of found user logins
	 */
	com.epam.ta.reportportal.ws.model.Page<UserResource> getUserNames(String value, Pageable pageable);

	/**
	 * Verify if any project exists in MongoDB 'project' collection
	 *
	 * @return TRUE if projects for current users are available
	 */
	OperationCompletionRS isProjectsAvailable();

	/**
	 * Get all project names
	 *
	 * @return All project names
	 */
	List<String> getAllProjectNames();
}