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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.DeleteUserFilterHandler;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.filter.UpdateUserFilterHandler;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.model.CollectionsRQ;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.OwnedEntityResource;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.model.filter.UserFilterResource;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pavel Bortnik
 */

@RestController
@PreAuthorize(ASSIGNED_TO_PROJECT)
@RequestMapping("/v1/{projectName}/filter")
@Tag(name = "User Filter", description = "User Filters API collection")
public class UserFilterController {

  private final ProjectExtractor projectExtractor;
  private final GetUserFilterHandler getFilterHandler;
  private final DeleteUserFilterHandler deleteFilterHandler;
  private final UpdateUserFilterHandler updateUserFilterHandler;

  @Autowired
  public UserFilterController(ProjectExtractor projectExtractor,
      GetUserFilterHandler getFilterHandler, DeleteUserFilterHandler deleteFilterHandler,
      UpdateUserFilterHandler updateUserFilterHandler) {
    this.projectExtractor = projectExtractor;
    this.getFilterHandler = getFilterHandler;
    this.deleteFilterHandler = deleteFilterHandler;
    this.updateUserFilterHandler = updateUserFilterHandler;
  }

  @Transactional
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create user filter")
  public EntryCreatedRS createFilter(@PathVariable String projectName,
      @RequestBody @Validated UpdateUserFilterRQ createFilterRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateUserFilterHandler.createFilter(createFilterRQ, projectName, user);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{filterId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get specified user filter by id")
  public UserFilterResource getFilter(@PathVariable String projectName, @PathVariable Long filterId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getFilterHandler.getUserFilter(
        filterId, projectExtractor.extractProjectDetails(user, projectName));
  }

  @Transactional(readOnly = true)
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get filters")
  public Page<UserFilterResource> getAllFilters(@PathVariable String projectName,
      @SortFor(UserFilter.class) Pageable pageable, @FilterFor(UserFilter.class) Filter filter,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getFilterHandler.getUserFilters(projectName, pageable, filter, user);
  }

  @Transactional
  @DeleteMapping(value = "/{filterId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Delete specified user filter by id")
  public OperationCompletionRS deleteFilter(@PathVariable String projectName,
      @PathVariable Long filterId, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteFilterHandler.deleteFilter(
        filterId, projectExtractor.extractProjectDetails(user, projectName), user);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/names")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get available filter names")
  public Page<OwnedEntityResource> getAllFiltersNames(@PathVariable String projectName,
      @SortFor(UserFilter.class) Pageable pageable, @FilterFor(UserFilter.class) Filter filter,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getFilterHandler.getFiltersNames(
        projectExtractor.extractProjectDetails(user, projectName), pageable, filter, user);
  }

  @Transactional
  @PutMapping(value = "/{filterId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update specified user filter")
  public OperationCompletionRS updateUserFilter(@PathVariable String projectName,
      @PathVariable Long filterId, @RequestBody @Validated UpdateUserFilterRQ updateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateUserFilterHandler.updateUserFilter(filterId, updateRQ,
        projectExtractor.extractProjectDetails(user, projectName), user
    );
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/filters")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get list of specified user filters")
  public List<UserFilterResource> getUserFilters(@PathVariable String projectName,
      @RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
    List<UserFilter> filters = getFilterHandler.getFiltersById(ids,
        projectExtractor.extractProjectDetails(user, projectName), user
    );
    return filters.stream().map(UserFilterConverter.TO_FILTER_RESOURCE)
        .collect(Collectors.toList());
  }

  @Transactional
  @RequestMapping(method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update list of user filters")
  public List<OperationCompletionRS> updateUserFilters(@PathVariable String projectName,
      @RequestBody @Validated CollectionsRQ<BulkUpdateFilterRQ> updateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateUserFilterHandler.updateUserFilter(
        updateRQ, projectExtractor.extractProjectDetails(user, projectName), user);
  }

}
