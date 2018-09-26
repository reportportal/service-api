/*
 * Copyright 2017 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
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
	 * @param filterId       Filter id
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return {@link UserFilterResource}
	 */
	UserFilterResource getFilter(Long filterId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get all {@link UserFilterResource} objects
	 *
	 * @param pageable       Page request
	 * @param filter         Filter representation
	 * @param projectDetails Project Details
	 * @param user           Report Portal User
	 * @return {@link Iterable}
	 */
	Iterable<UserFilterResource> getFilters(Filter filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Get owned user filters for specified project
	 *
	 * @param filter         Filter representation
	 * @param projectDetails Project Details
	 * @param user           Report Portal
	 * @return Filters
	 */
	List<UserFilterResource> getOwnFilters(Filter filter, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get shared filters for specified project
	 *
	 * @param filter         Filter representation
	 * @param projectDetails Project details
	 * @param user           Report Portal user
	 * @return Shared Filters
	 */
	List<UserFilterResource> getSharedFilters(Filter filter, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get all {@link com.epam.ta.reportportal.entity.filter.UserFilter}'s names
	 *
	 * @param projectDetails Project details
	 * @param user           Report Portal user
	 * @param isShared       Is shared
	 * @return List of {@link SharedEntity}
	 */
	Iterable<SharedEntity> getFiltersNames(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, boolean isShared);

	/**
	 * Get all
	 * {@link com.epam.ta.reportportal.ws.model.filter.UserFilterResource}
	 * objects
	 *
	 * @param ids            Filter IDs
	 * @param projectDetails Project details
	 * @param user           Report Portal user
	 * @return Found filters
	 */
	List<UserFilterResource> getFilters(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

}
