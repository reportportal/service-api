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

package com.epam.ta.reportportal.core.activity;

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Define operations for loading activities
 *
 * @author Dzmitry_Kavalets
 */
public interface IActivityHandler {

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.database.entity.item.TestItem}
	 *
	 * @param projectName Name of project
	 * @param filter      Filter
	 * @param pageable    Page Details
	 * @return Found activities
	 */
	List<ActivityResource> getActivitiesHistory(String projectName, Filter filter, Pageable pageable);

	/**
	 * Load {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 *
	 * @param projectName Name of project
	 * @param activityId  ID of activity
	 * @return Found Activity or NOT FOUND exception
	 */
	ActivityResource getActivity(String projectName, String activityId);

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.database.entity.item.TestItem}
	 *
	 * @param projectName Name of project
	 * @param itemId      ID of test item
	 * @param filter      Filter
	 * @param pageable    Page Details
	 * @return Found activities
	 */
	List<ActivityResource> getItemActivities(String projectName, String itemId, Filter filter, Pageable pageable);

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.database.entity.Project}
	 *
	 * @param projectName Name of project
	 * @param filter      Filter
	 * @param pageable    Page Details
	 * @return Found activities
	 */
	Page<ActivityResource> getItemActivities(String projectName, Filter filter, Pageable pageable);

}
