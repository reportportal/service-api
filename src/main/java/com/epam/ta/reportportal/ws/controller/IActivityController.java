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

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

/**
 * Report Portal WS Interface. Activity controller
 *
 * @author Dzmitry_Kavalets
 */
public interface IActivityController {

	/**
	 * Load activity
	 *
	 * @param projectName
	 * @param activityId
	 * @param principal
	 * @return
	 */
	ActivityResource getActivity(String projectName, String activityId, Principal principal);

	/**
	 * Load test item activities
	 *
	 * @param projectName
	 * @param itemId
	 * @param filter
	 * @param pageable
	 * @param principal
	 * @return
	 */
	List<ActivityResource> getTestItemActivities(String projectName, String itemId, Filter filter, Pageable pageable, Principal principal);

	/**
	 * Get activities for specified project with filter
	 * and paging
	 *
	 * @param projectName
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<ActivityResource> getActivities(String projectName, Filter filter, Pageable pageable);

}