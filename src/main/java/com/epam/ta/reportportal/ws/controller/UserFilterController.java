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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.ICreateUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IDeleteUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IGetUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IUpdateUserFilterHandler;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.util.ProjectUtils.extractProjectDetails;

/**
 * @author Pavel Bortnik
 */

@RestController
@PreAuthorize(ASSIGNED_TO_PROJECT)
@RequestMapping("/{projectName}/filter")
public class UserFilterController {

	private final ICreateUserFilterHandler createFilterHandler;
	private final IGetUserFilterHandler getFilterHandler;
	private final IDeleteUserFilterHandler deleteFilterHandler;
	private final IUpdateUserFilterHandler updateUserFilterHandler;

	@Autowired
	public UserFilterController(ICreateUserFilterHandler createFilterHandler, IGetUserFilterHandler getFilterHandler,
			IDeleteUserFilterHandler deleteFilterHandler, IUpdateUserFilterHandler updateUserFilterHandler) {
		this.createFilterHandler = createFilterHandler;
		this.getFilterHandler = getFilterHandler;
		this.deleteFilterHandler = deleteFilterHandler;
		this.updateUserFilterHandler = updateUserFilterHandler;
	}

	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create user filter")
	public EntryCreatedRS createFilter(@PathVariable String projectName, @RequestBody @Validated CreateUserFilterRQ createFilterRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createFilterHandler.createFilter(createFilterRQ, extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get specified user filter by id")
	public UserFilterResource getFilter(@PathVariable String projectName, @PathVariable Long filterId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getFilter(filterId, extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all filters")
	public Iterable<UserFilterResource> getAllFilters(@PathVariable String projectName, @SortFor(UserFilter.class) Pageable pageable,
			@FilterFor(UserFilter.class) Filter filter, @AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getFilters(filter, pageable, extractProjectDetails(user, projectName), user);
	}

	// filter/own
	@Transactional(readOnly = true)
	@GetMapping(value = "/own")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all filters for specified user who own them")
	public List<UserFilterResource> getOwnFilters(@PathVariable String projectName, @FilterFor(UserFilter.class) Filter filter,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getOwnFilters(filter, extractProjectDetails(user, projectName), user);
	}

	// filter/shared
	@Transactional(readOnly = true)
	@GetMapping(value = "/shared")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all available shared filters (except own shared filters)")
	public List<UserFilterResource> getSharedFilters(@PathVariable String projectName, @FilterFor(UserFilter.class) Filter filter,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getSharedFilters(filter, extractProjectDetails(user, projectName), user);
	}

	@Transactional
	@DeleteMapping(value = "/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete specified user filter by id")
	public OperationCompletionRS deleteFilter(@PathVariable String projectName, @PathVariable Long filterId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteFilterHandler.deleteFilter(filterId, extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/names")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get available filter names")
	public Iterable<SharedEntity> getAllFiltersNames(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestParam(value = "share", defaultValue = "false", required = false) boolean isShared) {
		return getFilterHandler.getFiltersNames(extractProjectDetails(user, projectName), user, isShared);
	}

	@Transactional
	@PutMapping(value = "/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update specified user filter")
	public OperationCompletionRS updateUserFilter(@PathVariable String projectName, @PathVariable Long filterId,
			@RequestBody @Validated UpdateUserFilterRQ updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateUserFilterHandler.updateUserFilter(filterId, updateRQ, extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/filters")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of specified user filters")
	public List<UserFilterResource> getUserFilters(@PathVariable String projectName, @RequestParam(value = "ids") Long[] ids,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getFilters(ids, extractProjectDetails(user, projectName), user);
	}

	@Transactional
	@RequestMapping(method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update list of user filters")
	public List<OperationCompletionRS> updateUserFilters(@PathVariable String projectName,
			@RequestBody @Validated CollectionsRQ<BulkUpdateFilterRQ> updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateUserFilterHandler.updateUserFilter(updateRQ, extractProjectDetails(user, projectName), user);
	}

}
