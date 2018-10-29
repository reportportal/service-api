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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.IGetWidgetHandler;
import com.epam.ta.reportportal.core.widget.IShareWidgetHandler;
import com.epam.ta.reportportal.core.widget.IUpdateWidgetHandler;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.util.ProjectUtils.extractProjectDetails;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@RestController
@PreAuthorize(ASSIGNED_TO_PROJECT)
@RequestMapping("/{projectName}/widget")
public class WidgetController {

	private final ICreateWidgetHandler createWidgetHandler;
	private final IUpdateWidgetHandler updateWidgetHandler;
	private final IGetWidgetHandler getWidgetHandler;
	@Autowired
	private IShareWidgetHandler shareWidgetHandler;

	@Autowired
	public WidgetController(ICreateWidgetHandler createWidgetHandler, IUpdateWidgetHandler updateWidgetHandler,
			IGetWidgetHandler getWidgetHandler) {
		this.createWidgetHandler = createWidgetHandler;
		this.updateWidgetHandler = updateWidgetHandler;
		this.getWidgetHandler = getWidgetHandler;
	}

	@Transactional
	@PostMapping
	@ResponseStatus(CREATED)
	public EntryCreatedRS createWidget(@RequestBody WidgetRQ createWidget, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String projectName) {
		return createWidgetHandler.createWidget(createWidget, extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{widgetId}")
	@ResponseStatus(OK)
	@ApiOperation("Get widget by ID")
	public WidgetResource getWidget(@PathVariable String projectName, @PathVariable Long widgetId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getWidgetHandler.getWidget(widgetId, extractProjectDetails(user, projectName), user);
	}

	@Transactional
	@PutMapping(value = "/{widgetId}")
	@ResponseStatus(OK)
	@ApiOperation("Update specified widget")
	public OperationCompletionRS updateWidget(@PathVariable String projectName, @PathVariable Long widgetId,
			@RequestBody @Validated WidgetRQ updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateWidgetHandler.updateWidget(widgetId, updateRQ, extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@PostMapping(value = "/preview")
	@ResponseStatus(OK)
	@ApiOperation("Get widget preview")
	public Map<String, ?> getWidgetPreview(@PathVariable String projectName, @RequestBody @Validated WidgetPreviewRQ previewRQ,
			Principal principal) {
		return getWidgetHandler.getWidgetPreview(normalizeId(projectName), principal.getName(), previewRQ);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/names/all")
	@ResponseStatus(OK)
	@ApiOperation("Load all widget names which belong to a user")
	public List<String> getWidgetNames(@PathVariable String projectName, Principal principal) {
		return getWidgetHandler.getWidgetNames(normalizeId(projectName), principal.getName());
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/shared")
	@ResponseStatus(OK)
	@ApiOperation("Load shared widgets")
	public Iterable<WidgetResource> getSharedWidgetsList(Principal principal, @PathVariable String projectName, Pageable pageable) {
		return getWidgetHandler.getSharedWidgetsList(principal.getName(), normalizeId(projectName), pageable);
	}

	@Transactional(readOnly = true)
	@PostMapping(value = "/shared/{widgetId}")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Share widget to project")
	public void shareWidjet(@PathVariable String projectName, @PathVariable Long widgetId) {
		shareWidgetHandler.shareWidget(projectName, widgetId);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/shared/search")
	@ResponseStatus(OK)
	@ApiOperation("Search shared widgets by name")
	public Iterable<WidgetResource> searchSharedWidgets(@RequestParam("term") String term, @PathVariable String projectName,
			Pageable pageable) {
		return getWidgetHandler.searchSharedWidgets(term, normalizeId(projectName), pageable);
	}

}
