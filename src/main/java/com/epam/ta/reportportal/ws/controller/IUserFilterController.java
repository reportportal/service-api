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

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

/**
 * Report Portal WS Interface. Filter controller
 *
 * @author Aliaksei_Makayed
 */
public interface IUserFilterController {

	/**
	 * Create new filter
	 *
	 * @param createFilterRQ Create filter DTO
	 * @param projectName    Project Name
	 * @return EntryCreatedRS
	 */
	List<EntryCreatedRS> createFilter(String projectName, CollectionsRQ<CreateUserFilterRQ> createFilterRQ, Principal principal);

	/**
	 * Get filter by id
	 *
	 * @param filterId    ID of filter
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @return FilterResource
	 */
	UserFilterResource getFilter(String projectName, String filterId, Principal principal);

	/**
	 * Get all filters
	 *
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @param filter      Filter
	 * @param pageable    Paging details
	 * @return Iterable of filters
	 */
	Iterable<UserFilterResource> getAllFilters(String projectName, Pageable pageable, Filter filter, Principal principal);

	/**
	 * Get all owned filters by user for project
	 *
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @param filter      Filter
	 * @return Iterable of filters
	 */
	Iterable<UserFilterResource> getOwnFilters(String projectName, Filter filter, Principal principal);

	/**
	 * Get all shared filters for specified project
	 *
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @param filter      Filter
	 * @return Iterable of filters
	 */
	Iterable<UserFilterResource> getSharedFilters(String projectName, Filter filter, Principal principal);

	/**
	 * Delete filter with specified id
	 *
	 * @param filterId    ID of filter
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @param userRole
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS deleteFilter(String projectName, String filterId, UserRole userRole, Principal principal);

	/**
	 * Get all user filter's names
	 *
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @param isShared    whether filter is shared
	 * @return Map String-String key - filter id, value - filter name
	 */
	Iterable<SharedEntity> getAllFiltersNames(String projectName, Principal principal, boolean isShared);

	/**
	 * Update user filter with specified id
	 *
	 * @param projectName  Project Name
	 * @param principal    User performing this request
	 * @param userFilterId , updateRQ
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS updateUserFilter(String projectName, String userFilterId, UpdateUserFilterRQ updateRQ, Principal principal,
			UserRole userRole);

	/**
	 * Get user filters
	 *
	 * @param ids         Filter IDs
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @return List of found filters
	 */
	List<UserFilterResource> getUserFilters(String projectName, String[] ids, Principal principal);

	/**
	 * Update user filters
	 *
	 * @param projectName Project Name
	 * @param principal   User performing this request
	 * @param updateRQ    Update DTO
	 * @return Operation result
	 */
	List<OperationCompletionRS> updateUserFilters(String projectName, CollectionsRQ<BulkUpdateFilterRQ> updateRQ, Principal principal,
			UserRole userRole);

}
