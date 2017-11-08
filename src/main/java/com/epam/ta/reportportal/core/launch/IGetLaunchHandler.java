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

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Get Launch operation handler
 *
 * @author Andrei_Kliashchonak
 * @author Andrei Varabyeu
 */
public interface IGetLaunchHandler {

	/**
	 * Get Launch resource by specified ID
	 *
	 * @param launchId    ID of launch
	 * @param userName    Login
	 * @param projectName Project Name
	 * @return
	 */
	LaunchResource getLaunch(String launchId, String userName, String projectName);

	/**
	 * Get Launch resource by specified Name (for Jenkins Plugin)
	 *
	 * @param project  Project Name
	 * @param pageable Page details
	 * @param username User name
	 * @return Response Data
	 */
	LaunchResource getLaunchByName(String project, Pageable pageable, Filter filter, String username);

	/**
	 * Get list of Launch resources for specified project
	 *
	 * @param projectName Project Name
	 * @param filter      Filter data
	 * @param pageable    Page details
	 * @param userName    Name of User
	 * @return Response Data
	 */
	Iterable<LaunchResource> getProjectLaunches(String projectName, Filter filter, Pageable pageable, String userName);

	/**
	 * Get debug launches
	 *
	 * @param projectName Project Name
	 * @param userName    Name of User
	 * @param filter      Filter data
	 * @param pageable    Page details
	 * @return Response Data
	 */
	Iterable<LaunchResource> getDebugLaunches(String projectName, String userName, Filter filter, Pageable pageable);

	/**
	 * Get specified launch tags (auto-complete functionality)
	 *
	 * @param project Project Name
	 * @param value   Tag prefix to be searched
	 * @return List of found tags
	 */
	List<String> getTags(String project, String value);

	/**
	 * Get launch names of specified project (auto-complete functionality)
	 *
	 * @param project Project Name
	 * @param value   Launch name prefix
	 * @return List of found launches
	 */
	List<String> getLaunchNames(String project, String value);

	/**
	 * Get unique owners of launches in specified mode
	 *
	 * @param project Project Name
	 * @param value   Owner name prefix
	 * @param field   Field
	 * @param mode    Mode
	 * @return Response Data
	 */
	List<String> getOwners(String project, String value, String field, String mode);

	/**
	 * Get launches comparison info
	 *
	 * @param projectName Name of project
	 * @param ids         IDs to be looked up
	 * @return Response Data
	 */
	Map<String, List<ChartObject>> getLaunchesComparisonInfo(String projectName, String[] ids);

	/**
	 * Get statuses of specified launches
	 *
	 * @param projectName Project Name
	 * @param ids         Launch IDs
	 * @return Response Data
	 */
	Map<String, String> getStatuses(String projectName, String[] ids);

	Page<LaunchResource> getLatestLaunches(String projectName, Filter filter, Pageable pageable);
}