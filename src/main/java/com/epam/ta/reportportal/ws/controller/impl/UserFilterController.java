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

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.core.filter.ICreateUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IDeleteUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IGetUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IUpdateUserFilterHandler;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.controller.IUserFilterController;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

/**
 * Controller implementation for
 * {@link com.epam.ta.reportportal.database.entity.filter.UserFilter} entity
 *
 * @author Aliaksei_Makayed
 */
@Controller
@RequestMapping("/{projectName}/filter")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class UserFilterController implements IUserFilterController {

	@Autowired
	private ICreateUserFilterHandler createFilterHandler;

	@Autowired
	private IGetUserFilterHandler getFilterHandler;

	@Autowired
	private IDeleteUserFilterHandler deleteFilterHandler;

	@Autowired
	private IUpdateUserFilterHandler updateUserFilterHandler;

	@Override
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create user filter")
	public List<EntryCreatedRS> createFilter(@PathVariable String projectName,
			@RequestBody @Validated CollectionsRQ<CreateUserFilterRQ> createFilterRQ, Principal principal) {
		return createFilterHandler.createFilter(principal.getName(), normalizeId(projectName), createFilterRQ);
	}

	@Override
	@RequestMapping(value = "/{filterId}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get specified user filter by id")
	public UserFilterResource getFilter(@PathVariable String projectName, @PathVariable String filterId, Principal principal) {
		return getFilterHandler.getFilter(principal.getName(), filterId, normalizeId(projectName));
	}

	@Override
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all filters")
	public Iterable<UserFilterResource> getAllFilters(@PathVariable String projectName, @SortFor(UserFilter.class) Pageable pageable,
			@FilterFor(UserFilter.class) Filter filter, Principal principal) {
		return getFilterHandler.getFilters(principal.getName(), pageable, filter, normalizeId(projectName));
	}

	// filter/own
	@Override
	@RequestMapping(value = "/own", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all filters for specified user who own them")
	public List<UserFilterResource> getOwnFilters(@PathVariable String projectName, @FilterFor(UserFilter.class) Filter filter,
			Principal principal) {
		return getFilterHandler.getOwnFilters(principal.getName(), filter, normalizeId(projectName));
	}

	// filter/shared
	@Override
	@RequestMapping(value = "/shared", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all available shared filters (except own shared filters)")
	public List<UserFilterResource> getSharedFilters(@PathVariable String projectName, @FilterFor(UserFilter.class) Filter filter,
			Principal principal) {
		return getFilterHandler.getSharedFilters(principal.getName(), filter, normalizeId(projectName));
	}

	@Override
	@RequestMapping(value = "/{filterId}", method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete specified user filter by id")
	public OperationCompletionRS deleteFilter(@PathVariable String projectName, @PathVariable String filterId,
			@ActiveRole UserRole userRole, Principal principal) {
		return deleteFilterHandler.deleteFilter(filterId, principal.getName(), normalizeId(projectName), userRole);
	}

	@Override
	@RequestMapping(value = "/names", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get available filter names")
	public Iterable<SharedEntity> getAllFiltersNames(@PathVariable String projectName, Principal principal,
			@RequestParam(value = "share", defaultValue = "false", required = false) boolean isShared) {
		return getFilterHandler.getFiltersNames(principal.getName(), normalizeId(projectName), isShared);
	}

	@Override
	@RequestMapping(value = "/{filterId}", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update specified user filter")
	public OperationCompletionRS updateUserFilter(@PathVariable String projectName, @PathVariable String filterId,
			@RequestBody @Validated UpdateUserFilterRQ updateRQ, Principal principal, @ActiveRole UserRole userRole) {
		return updateUserFilterHandler.updateUserFilter(filterId, updateRQ, principal.getName(), normalizeId(projectName), userRole);
	}

	@Override
	@RequestMapping(value = "/filters", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of specified user filters")
	public List<UserFilterResource> getUserFilters(@PathVariable String projectName,
			@RequestParam(value = "ids", required = true) String[] ids, Principal principal) {
		return getFilterHandler.getFilters(normalizeId(projectName), ids, principal.getName());
	}

	@Override
	@RequestMapping(method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update list of user filters")
	public List<OperationCompletionRS> updateUserFilters(@PathVariable String projectName,
			@RequestBody @Validated CollectionsRQ<BulkUpdateFilterRQ> updateRQ, Principal principal, @ActiveRole UserRole userRole) {
		return updateUserFilterHandler.updateUserFilter(updateRQ, principal.getName(), normalizeId(projectName), userRole);
	}
}
