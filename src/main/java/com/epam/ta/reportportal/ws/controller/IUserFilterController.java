/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import org.springframework.data.domain.Pageable;

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;


/**
 * Report Portal WS Interface. Filter controller
 * 
 * @author Aliaksei_Makayed
 * 
 */
public interface IUserFilterController {

	/**
	 * Create new filter
	 * 
	 * @param createFilterRQ
	 * @param projectName
	 * @return EntryCreatedRS
	 * @throws ReportPortalException
	 */
	List<EntryCreatedRS> createFilter(String projectName, CollectionsRQ<CreateUserFilterRQ> createFilterRQ, Principal principal);

	/**
	 * Get filter by id
	 * 
	 * @param filterId
	 * @param projectName
	 * @param principal
	 * @return FilterResource
	 * @throws ReportPortalException
	 */
	UserFilterResource getFilter(String projectName, String filterId, Principal principal);

	/**
	 * Get all filters
	 * 
	 * @param projectName
	 * @param principal
	 * @param filter
	 * @return Iterable<FilterResource>
	 * @throws ReportPortalException
	 */
	Iterable<UserFilterResource> getAllFilters(String projectName, Pageable pageable,Filter filter, Principal principal);
	
	/**
	 * Get all owned filters by user for project
	 * 
	 * @param projectName
	 * @param filter
	 * @param proncipal
	 * @return
	 */
	Iterable<UserFilterResource> getOwnFilters(String projectName, Filter filter, Principal proncipal);
	
	/**
	 * Get all shared filters for specified project
	 * 
	 * @param projectName
	 * @param filter
	 * @param principal
	 * @return
	 */
	Iterable<UserFilterResource> getSharedFilters(String projectName, Filter filter, Principal principal);

	/**
	 * Delete filter with specified id
	 * 
	 * @param filterId
	 * @param projectName
	 * @param principal
	 * @return OperationCompletionRS
	 * @throws ReportPortalException
	 */
	OperationCompletionRS deleteFilter(String projectName, String filterId, Principal principal);

	/**
	 * Get all user filter's names
	 * 
	 * @param projectName
	 * @param principal
	 * @param specify which filter names should be returned shared or owned
	 * @return Map<String, String> key - filter id, value - filter name
	 * @throws ReportPortalException
	 */
	Map<String, SharedEntity> getAllFiltersNames(String projectName, Principal principal, boolean isShared);
	
	/**
	 * Update user filter with specified id
	 * 
	 * @param projectName
	 * @param principal
	 * @param userFilterId
	 *            , updateRQ
	 * @return {@link OperationCompletionRS}
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateUserFilter(String projectName, String userFilterId, UpdateUserFilterRQ updateRQ, Principal principal);

	/**
	 * Get user filters
	 * 
	 * @param projectName
	 * @param ids
	 * @param principal
	 * @return
	 */
	List<UserFilterResource> getUserFilters(String projectName, String[] ids, Principal principal);

	/**
	 * Update user filters
	 *
	 * @param projectName
	 * @param updateRQ
	 * @param principal
	 * @return
	 */
	List<OperationCompletionRS> updateUserFilters(String projectName, CollectionsRQ<BulkUpdateFilterRQ> updateRQ, Principal principal);

}