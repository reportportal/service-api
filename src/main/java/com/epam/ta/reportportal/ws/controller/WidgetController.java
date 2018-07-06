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
import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.IGetWidgetHandler;
import com.epam.ta.reportportal.core.widget.IUpdateWidgetHandler;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * @author Pavel Bortnik
 */
@Controller
@RequestMapping("/{projectName}/widget")
public class WidgetController {

	private ICreateWidgetHandler createWidgetHandler;

	private IUpdateWidgetHandler updateWidgetHandler;

	private IGetWidgetHandler getWidgetHandler;

	@Autowired
	public void setCreateWidgetHandler(ICreateWidgetHandler createWidgetHandler) {
		this.createWidgetHandler = createWidgetHandler;
	}

	@Autowired
	public void setUpdateWidgetHandler(IUpdateWidgetHandler updateWidgetHandler) {
		this.updateWidgetHandler = updateWidgetHandler;
	}

	@Autowired
	public void setGetWidgetHandler(IGetWidgetHandler getWidgetHandler) {
		this.getWidgetHandler = getWidgetHandler;
	}

	@Transactional
	@PostMapping
	@ResponseBody
	@ResponseStatus(CREATED)
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS createWidget(@RequestBody WidgetRQ createWidget, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String projectName) {
		return createWidgetHandler.createWidget(createWidget, ProjectUtils.extractProjectDetails(user, projectName), user);
	}

	@Transactional
	@RequestMapping(value = "/{widgetId}", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get widget by ID")
	public WidgetResource getWidget(@PathVariable String projectName, @PathVariable Long widgetId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getWidgetHandler.getWidget(widgetId, ProjectUtils.extractProjectDetails(user, projectName), user);
	}

	@Transactional
	@RequestMapping(value = "/{widgetId}", method = PUT)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Update specified widget")
	public OperationCompletionRS updateWidget(@PathVariable String projectName, @PathVariable Long widgetId,
			@RequestBody @Validated WidgetRQ updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateWidgetHandler.updateWidget(widgetId, updateRQ, ProjectUtils.extractProjectDetails(user, projectName), user);
	}

}
