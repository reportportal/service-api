/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Andrei_Ramanchuk
 */
public interface GetProjectHandler {

	/**
	 * Get project users info
	 *
	 * @param projectDetails Project details
	 * @return list of {@link UserResource}
	 */
	Iterable<UserResource> getProjectUsers(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);

	/**
	 * Get project resource information
	 *
	 * @param projectName Project name
	 * @param user        User
	 * @return {@link ProjectResource}
	 */
	ProjectResource getProject(String projectName, ReportPortalUser user);

	/**
	 * Get list of specified usernames
	 *
	 * @param projectDetails Project name
	 * @param value          Login
	 * @return List of found user logins
	 */
	List<String> getUserNames(ReportPortalUser.ProjectDetails projectDetails, String value);

	/**
	 * Performs global search for user
	 *
	 * @param value login OR full name of user
	 * @return List of found user logins
	 */
	Iterable<UserResource> getUserNames(String value, Pageable pageable);

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

	/**
	 * Export Projects info according to the {@link ReportFormat} type
	 *
	 * @param reportFormat {@link ReportFormat}
	 * @param filter       {@link Filter}
	 * @param outputStream {@link HttpServletResponse#getOutputStream()}
	 * @param pageable     {@link Pageable}
	 */
	void exportProjects(ReportFormat reportFormat, Filter filter, OutputStream outputStream, Pageable pageable);

	Map<String, Boolean> getAnalyzerIndexingStatus();
}