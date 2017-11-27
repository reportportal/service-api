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

package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Get filter handler
 *
 * @author Aliaksei_Makayed
 */
public interface IGetUserFilterHandler {

	/**
	 * Get {@link UserFilterResource} by id
	 *
	 * @param userName    User name
	 * @param filterId    Filter ID
	 * @param projectName Project Name
	 * @return {@link UserFilterResource}
	 */
	UserFilterResource getFilter(String userName, String filterId, String projectName);

	/**
	 * Get all {@link UserFilterResource} objects
	 *
	 * @param userName    User name
	 * @param pageable    Page request
	 * @param projectName Project Name
	 * @param filter      Filter representation
	 * @return {@link Iterable}
	 */
	Iterable<UserFilterResource> getFilters(String userName, Pageable pageable, Filter filter, String projectName);

	/**
	 * Get owned user filters for specified project
	 *
	 * @param userName    User name
	 * @param filter      Filter representation
	 * @param projectName ProjectName
	 * @return Filters
	 */
	List<UserFilterResource> getOwnFilters(String userName, Filter filter, String projectName);

	/**
	 * Get shared filters for specified project
	 *
	 * @param userName    User name
	 * @param filter      Filter representation
	 * @param projectName Project name
	 * @return Shared Filters
	 */
	List<UserFilterResource> getSharedFilters(String userName, Filter filter, String projectName);

	/**
	 * Get all {@link UserFilter}'s names
	 *
	 * @param userName    User name
	 * @param projectName Project Name
	 * @return List of {@link SharedEntity}
	 */
	Iterable<SharedEntity> getFiltersNames(String userName, String projectName, boolean isShared);

	/**
	 * Get all
	 * {@link com.epam.ta.reportportal.ws.model.filter.UserFilterResource}
	 * objects
	 *
	 * @param projectName Project Name
	 * @param ids         Filter IDs
	 * @param userName    User Name
	 * @return Found filters
	 */
	List<UserFilterResource> getFilters(String projectName, String[] ids, String userName);

}
