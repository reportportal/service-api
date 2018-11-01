/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Get filter handler
 *
 * @author Aliaksei_Makayed
 */
public interface GetUserFilterHandler {

	/**
	 * Get {@link UserFilterResource} by id
	 *
	 * @param filterId       Filter id
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return {@link UserFilterResource}
	 */
	UserFilter getFilter(Long filterId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get own {@link UserFilterResource} objects
	 *
	 * @param projectName    Project Name
	 * @param pageable       Page request
	 * @param filter         Filter representation
	 * @param user           Report Portal User
	 * @return {@link Iterable}
	 */
	Iterable<UserFilterResource> getOwnFilters(String projectName, Pageable pageable,
		Filter filter, ReportPortalUser user);

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
	List<UserFilter> getFilters(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

}
