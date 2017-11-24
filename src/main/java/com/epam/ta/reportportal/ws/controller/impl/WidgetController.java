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

import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.IGetWidgetHandler;
import com.epam.ta.reportportal.core.widget.IUpdateWidgetHandler;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.controller.IWidgetController;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import com.epam.ta.reportportal.ws.validation.WidgetRQCustomValidator;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Controller implementation for
 * {@link com.epam.ta.reportportal.database.entity.widget.Widget} entity
 *
 * @author Aliaksei_Makayed
 */
@Controller
@RequestMapping("/{projectName}/widget")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class WidgetController implements IWidgetController {

	@Autowired
	private IGetWidgetHandler getHandler;

	@Autowired
	private ICreateWidgetHandler createHandler;

	@Autowired
	private IUpdateWidgetHandler updateHandler;

	@Autowired
	private WidgetRQCustomValidator validator;

	@InitBinder
	@PreAuthorize("permitAll")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}

	@Override
	@RequestMapping(method = POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiOperation("Create new widget")
	public EntryCreatedRS createWidget(@PathVariable String projectName,
			@RequestBody @Validated(WidgetRQCustomValidator.class) WidgetRQ createWidgetRQ, Principal principal) {
		return createHandler.createWidget(createWidgetRQ, normalizeId(projectName), principal.getName());
	}

	@Override
	@RequestMapping(value = "/{widgetId}", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get widget by ID")
	public WidgetResource getWidget(@PathVariable String projectName, @PathVariable String widgetId, Principal principal) {
		return getHandler.getWidget(widgetId, principal.getName(), normalizeId(projectName));
	}

	@Override
	@RequestMapping(value = "/preview", method = POST)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get widget preview")
	public Map<String, ?> getWidgetPreview(@PathVariable String projectName, @RequestBody @Validated WidgetPreviewRQ previewRQ,
			Principal principal) {
		return getHandler.getWidgetPreview(normalizeId(projectName), principal.getName(), previewRQ);
	}

	@Override
	@RequestMapping(value = "/{widgetId}", method = PUT)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Update specified widget")
	public OperationCompletionRS updateWidget(@PathVariable String projectName, @PathVariable String widgetId,
			@RequestBody @Validated WidgetRQ updateRQ, @ActiveRole UserRole userRole, Principal principal) {
		return updateHandler.updateWidget(widgetId, updateRQ, principal.getName(), normalizeId(projectName), userRole);
	}

	@Override
	@RequestMapping(value = "/names/shared", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@Deprecated
	@ApiIgnore
	public Iterable<SharedEntity> getSharedWidgets(Principal principal, @PathVariable String projectName, Pageable pageable) {
		return getHandler.getSharedWidgetNames(principal.getName(), normalizeId(projectName), pageable);
	}

	@Override
	@RequestMapping(value = "/names/all", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Load all widget names which belong to a user")
	public List<String> getWidgetNames(@PathVariable String projectName, Principal principal) {
		return getHandler.getWidgetNames(normalizeId(projectName), principal.getName());
	}

	@Override
	@RequestMapping(value = "/shared", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Load shared widgets")
	public Iterable<WidgetResource> getSharedWidgetsList(Principal principal, @PathVariable String projectName, Pageable pageable) {
		return getHandler.getSharedWidgetsList(principal.getName(), normalizeId(projectName), pageable);
	}

	@Override
	@RequestMapping(value = "/shared/search", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Search shared widgets by name")
	public Iterable<WidgetResource> searchSharedWidgets(@RequestParam("term") String term, @PathVariable String projectName, Pageable pageable) {
		return getHandler.searchSharedWidgets(term, normalizeId(projectName), pageable);
	}
}
