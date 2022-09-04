/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.DeleteUserFilterHandler;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.filter.UpdateUserFilterHandler;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.OwnedEntityResource;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
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
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

/**
 * @author Pavel Bortnik
 */

@RestController
@PreAuthorize(ASSIGNED_TO_PROJECT)
@RequestMapping("/v1/{projectKey}/filter")
public class UserFilterController {

	private final ProjectExtractor projectExtractor;
	private final GetUserFilterHandler getFilterHandler;
	private final DeleteUserFilterHandler deleteFilterHandler;
	private final UpdateUserFilterHandler updateUserFilterHandler;

	@Autowired
	public UserFilterController(ProjectExtractor projectExtractor, GetUserFilterHandler getFilterHandler,
			DeleteUserFilterHandler deleteFilterHandler, UpdateUserFilterHandler updateUserFilterHandler) {
		this.projectExtractor = projectExtractor;
		this.getFilterHandler = getFilterHandler;
		this.deleteFilterHandler = deleteFilterHandler;
		this.updateUserFilterHandler = updateUserFilterHandler;
	}

	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create user filter")
	public EntryCreatedRS createFilter(@PathVariable String projectKey, @RequestBody @Validated UpdateUserFilterRQ createFilterRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateUserFilterHandler.createFilter(createFilterRQ, projectKey, user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get specified user filter by id")
	public UserFilterResource getFilter(@PathVariable String projectKey, @PathVariable Long filterId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getUserFilter(filterId, projectExtractor.extractProjectDetails(user, projectKey));
	}

	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get filters")
	public Iterable<UserFilterResource> getAllFilters(@PathVariable String projectKey, @SortFor(UserFilter.class) Pageable pageable,
			@FilterFor(UserFilter.class) Filter filter, @AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getUserFilters(projectKey, pageable, filter, user);
	}

	@Transactional
	@DeleteMapping(value = "/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete specified user filter by id")
	public OperationCompletionRS deleteFilter(@PathVariable String projectKey, @PathVariable Long filterId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteFilterHandler.deleteFilter(filterId, projectExtractor.extractProjectDetails(user, projectKey), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/names")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get available filter names")
	public Iterable<OwnedEntityResource> getAllFiltersNames(@PathVariable String projectKey, @SortFor(UserFilter.class) Pageable pageable,
			@FilterFor(UserFilter.class) Filter filter, @AuthenticationPrincipal ReportPortalUser user) {
		return getFilterHandler.getFiltersNames(projectExtractor.extractProjectDetails(user, projectKey), pageable, filter, user);
	}

	@Transactional
	@PutMapping(value = "/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update specified user filter")
	public OperationCompletionRS updateUserFilter(@PathVariable String projectKey, @PathVariable Long filterId,
			@RequestBody @Validated UpdateUserFilterRQ updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateUserFilterHandler.updateUserFilter(
				filterId,
				updateRQ,
				projectExtractor.extractProjectDetails(user, projectKey),
				user
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/filters")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of specified user filters")
	public List<UserFilterResource> getUserFilters(@PathVariable String projectKey, @RequestParam(value = "ids") Long[] ids,
			@AuthenticationPrincipal ReportPortalUser user) {
		List<UserFilter> filters = getFilterHandler.getFiltersById(ids, projectExtractor.extractProjectDetails(user, projectKey), user);
		return filters.stream().map(UserFilterConverter.TO_FILTER_RESOURCE).collect(Collectors.toList());
	}

	@Transactional
	@RequestMapping(method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update list of user filters")
	public List<OperationCompletionRS> updateUserFilters(@PathVariable String projectKey,
			@RequestBody @Validated CollectionsRQ<BulkUpdateFilterRQ> updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateUserFilterHandler.updateUserFilter(updateRQ, projectExtractor.extractProjectDetails(user, projectKey), user);
	}

}
