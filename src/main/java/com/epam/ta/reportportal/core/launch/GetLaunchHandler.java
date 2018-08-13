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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
//import com.epam.ta.reportportal.entity.widget.content.ComparisonStatisticsContent;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Get Launch operation handler
 *
 * @author Andrei_Kliashchonak
 * @author Andrei Varabyeu
 */
public interface GetLaunchHandler {

	/**
	 * Get Launch resource by specified ID
	 *
	 * @param launchId       Launch id
	 * @param projectDetails Project Details
	 * @return
	 */
	LaunchResource getLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails);

	//	/**
	//	 * Get Launch resource by specified Name (for Jenkins Plugin)
	//	 *
	//	 * @param project  Project Name
	//	 * @param pageable Page details
	//	 * @param username User name
	//	 * @return Response Data
	//	 */
	//	LaunchResource getLaunchByName(String project, Pageable pageable, Filter filter, String username);

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
	 * Get specified launch tags (auto-complete functionality)
	 *
	 * @param projectDetails Project Details
	 * @param value          Tag prefix to be searched
	 * @return List of found tags
	 */
	List<String> getTags(ReportPortalUser.ProjectDetails projectDetails, String value);

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
//	 */
//	List<ComparisonStatisticsContent> getLaunchesComparisonInfo(ReportPortalUser.ProjectDetails projectDetails, Long[] ids);

	/**
	 * Get statuses of specified launches
	 *
	 * @param projectDetails Project Details
	 * @param ids            Launch IDs
	 * @return Response Data
	 */
	Map<String, String> getStatuses(ReportPortalUser.ProjectDetails projectDetails, Long[] ids);

	/**
	 * Get latest launches
	 *
	 * @param projectDetails Project Details
	 * @param filter         Filter data
	 * @param pageable       Page details
	 * @return Response Data
	 */
	Page<LaunchResource> getLatestLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);
}