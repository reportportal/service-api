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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

//import com.epam.ta.reportportal.entity.widget.content.ComparisonStatisticsContent;

/**
 * Get Launch operation handler
 *
 * @author Andrei_Kliashchonak
 * @author Andrei Varabyeu
 */
public interface GetLaunchHandler {

	/**
	 * Get Launch resource by specified UUID
	 *
	 * @param launchId       Launch uuid
	 * @param projectDetails Project Details
	 * @return
	 */
	LaunchResource getLaunch(String launchId, ReportPortalUser.ProjectDetails projectDetails);

	/**
	 * Get Launch resource by specified Name (for Jenkins Plugin)
	 *
	 * @param project  Project Name
	 * @param pageable Page details
	 * @param username User name
	 * @return Response Data
	 */
	LaunchResource getLaunchByProjectName(String project, Pageable pageable, Filter filter, String username);

	/**
	 * Get list of Launch resources for specified project
	 *
	 * @param projectDetails Project Details
	 * @param filter         Filter data
	 * @param pageable       Page details
	 * @param userName       Name of User
	 * @return Response Data
	 */
	Iterable<LaunchResource> getProjectLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable,
			String userName);

	/**
	 * Get debug launches
	 *
	 * @param projectDetails Project Details
	 * @param filter         Filter data
	 * @param pageable       Page details
	 * @return Response Data
	 */
	Iterable<LaunchResource> getDebugLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);

	/**
	 * Get specified launch attribute keys (auto-complete functionality)
	 *
	 * @param projectDetails Project Details
	 * @param value          Tag prefix to be searched
	 * @return List of found tags
	 */
	List<String> getAttributeKeys(ReportPortalUser.ProjectDetails projectDetails, String value);

	/**
	 * Get specified launch attribute values (auto-complete functionality)
	 *
	 * @param projectDetails Project Details
	 * @param value          Tag prefix to be searched
	 * @return List of found tags
	 */
	List<String> getAttributeValues(ReportPortalUser.ProjectDetails projectDetails, String key, String value);

	/**
	 * Get launch names of specified project (auto-complete functionality)
	 *
	 * @param projectDetails Project Details
	 * @param value          Launch name prefix
	 * @return List of found launches
	 */
	List<String> getLaunchNames(ReportPortalUser.ProjectDetails projectDetails, String value);

	/**
	 * Get unique owners of launches in specified mode
	 *
	 * @param projectDetails Project Details
	 * @param value          Owner name prefix
	 * @param mode           Mode
	 * @return Response Data
	 */
	List<String> getOwners(ReportPortalUser.ProjectDetails projectDetails, String value, String mode);

	/**
	 * Get launches comparison info
	 *
	 * @param projectDetails Project Details
	 * @param ids            IDs to be looked up
	 * @return Response Data
	 * //
	 */
	Map<String, List<ChartStatisticsContent>> getLaunchesComparisonInfo(ReportPortalUser.ProjectDetails projectDetails, Long[] ids);

	/**
	 * Get statuses of specified launches
	 *
	 * @param projectDetails Project Details
	 * @param ids            Launch IDs
	 * @return Response Data
	 */
	Map<String, String> getStatuses(ReportPortalUser.ProjectDetails projectDetails, Long[] ids);

	/**
	 * Export Launch info according to the {@link ReportFormat} type
	 *
	 * @param launchId     {@link com.epam.ta.reportportal.entity.launch.Launch#id}
	 * @param reportFormat {@link ReportFormat}
	 * @param outputStream {@link HttpServletResponse#getOutputStream()}
	 * @param user         Current {@link ReportPortalUser}
	 */
	void exportLaunch(Long launchId, ReportFormat reportFormat, OutputStream outputStream, ReportPortalUser user);

	/**
	 * Get latest launches
	 *
	 * @param projectDetails Project Details
	 * @param filter         Filter data
	 * @param pageable       Page details
	 * @return Response Data
	 */
	Iterable<LaunchResource> getLatestLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);
}