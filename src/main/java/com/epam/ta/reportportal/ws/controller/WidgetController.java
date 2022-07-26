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
import com.epam.ta.reportportal.core.widget.CreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.GetWidgetHandler;
import com.epam.ta.reportportal.core.widget.UpdateWidgetHandler;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@RestController
@PreAuthorize(ASSIGNED_TO_PROJECT)
@RequestMapping("/v1/{projectName}/widget")
public class WidgetController {

	private final ProjectExtractor projectExtractor;
	private final CreateWidgetHandler createWidgetHandler;
	private final UpdateWidgetHandler updateWidgetHandler;
	private final GetWidgetHandler getWidgetHandler;

	@Autowired
	public WidgetController(ProjectExtractor projectExtractor, CreateWidgetHandler createWidgetHandler, UpdateWidgetHandler updateWidgetHandler,
			GetWidgetHandler getWidgetHandler) {
		this.projectExtractor = projectExtractor;
		this.createWidgetHandler = createWidgetHandler;
		this.updateWidgetHandler = updateWidgetHandler;
		this.getWidgetHandler = getWidgetHandler;
	}

	@Transactional
	@PostMapping
	@ResponseStatus(CREATED)
	@ApiOperation("Create a new widget")
	public EntryCreatedRS createWidget(@RequestBody @Validated WidgetRQ createWidget, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String projectName) {
		return createWidgetHandler.createWidget(createWidget, projectExtractor.extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{widgetId}")
	@ResponseStatus(OK)
	@ApiOperation("Get widget by ID")
	public WidgetResource getWidget(@PathVariable String projectName, @PathVariable Long widgetId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getWidgetHandler.getWidget(widgetId, projectExtractor.extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "multilevel/{widgetId}")
	@ResponseStatus(OK)
	@ApiOperation("Get multilevel widget by ID")
	public WidgetResource getWidget(@PathVariable String projectName, @PathVariable Long widgetId,
			@RequestParam(required = false, name = "attributes") String[] attributes, @RequestParam MultiValueMap<String, String> params, @AuthenticationPrincipal ReportPortalUser user) {
		return getWidgetHandler.getWidget(widgetId, ArrayUtils.nullToEmpty(attributes), params, projectExtractor.extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@PostMapping(value = "/preview")
	@ResponseStatus(OK)
	@ApiOperation("Get widget preview")
	public Map<String, ?> getWidgetPreview(@PathVariable String projectName, @RequestBody @Validated WidgetPreviewRQ previewRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getWidgetHandler.getWidgetPreview(previewRQ, projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user);
	}

	@Transactional
	@PutMapping(value = "/{widgetId}")
	@ResponseStatus(OK)
	@ApiOperation("Update specified widget")
	public OperationCompletionRS updateWidget(@PathVariable String projectName, @PathVariable Long widgetId,
			@RequestBody @Validated WidgetRQ updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateWidgetHandler.updateWidget(widgetId, updateRQ, projectExtractor.extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/names/all")
	@ResponseStatus(OK)
	@ApiOperation("Load all widget names which belong to a user")
	public Iterable<Object> getWidgetNames(@PathVariable String projectName, @SortFor(Widget.class) Pageable pageable,
			@FilterFor(Widget.class) Filter filter, @AuthenticationPrincipal ReportPortalUser user) {
		return getWidgetHandler.getOwnNames(projectExtractor.extractProjectDetails(user, projectName), pageable, filter, user);
	}
}