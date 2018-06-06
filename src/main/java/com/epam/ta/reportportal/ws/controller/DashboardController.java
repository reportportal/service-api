/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.core.dashboard.ICreateDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IGetDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IUpdateDashboardHandler;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@Controller
@RequestMapping("/{projectName}/dashboard")
public class DashboardController {

	private ICreateDashboardHandler createDashboardHandler;

	private IUpdateDashboardHandler updateDashboardHandler;

	private IGetDashboardHandler getDashboardHandler;

	@Autowired
	public void setCreateDashboardHandler(ICreateDashboardHandler createDashboardHandler) {
		this.createDashboardHandler = createDashboardHandler;
	}

	@Autowired
	public void setUpdateDashboardHandler(IUpdateDashboardHandler updateDashboardHandler) {
		this.updateDashboardHandler = updateDashboardHandler;
	}

	@Autowired
	public void setGetHandler(IGetDashboardHandler getDashboardHandler) {
		this.getDashboardHandler = getDashboardHandler;
	}

	@Transactional
	@PostMapping
	@ResponseStatus(CREATED)
	@ResponseBody
	@ApiOperation("Create dashboard for specified project")
	public EntryCreatedRS createDashboard(@PathVariable String projectName, @RequestBody @Validated CreateDashboardRQ createRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createDashboardHandler.createDashboard(EntityUtils.takeProjectDetails(user, projectName), createRQ, user);
	}

	@Transactional
	@PutMapping("/{dashboardId}/add")
	@ResponseStatus(CREATED)
	@ResponseBody
	@ApiOperation("Add widget to specified dashboard")
	public OperationCompletionRS addWidget(@PathVariable String projectName, @PathVariable Long dashboardId,
			@RequestBody @Validated AddWidgetRq addWidgetRq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateDashboardHandler.addWidget(dashboardId, EntityUtils.takeProjectDetails(user, projectName), addWidgetRq, user);
	}

	@Transactional
	@DeleteMapping("/{dashboardId}/{widgetId}")
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Remove widget from specified dashboard")
	public OperationCompletionRS removeWidget(@PathVariable String projectName, @PathVariable Long dashboardId, @PathVariable Long widgetId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateDashboardHandler.removeWidget(widgetId, dashboardId, EntityUtils.takeProjectDetails(user, projectName));
	}

	@Transactional
	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Update specified dashboard for specified project")
	public OperationCompletionRS updateDashboard(@PathVariable String projectName, @PathVariable Long dashboardId,
			@RequestBody @Validated UpdateDashboardRQ updateRQ, Principal principal, @AuthenticationPrincipal ReportPortalUser user) {
		return new OperationCompletionRS("ok");
	}

	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get specified dashboard by ID for specified project")
	public DashboardResource getDashboard(@PathVariable String projectName, @PathVariable Long dashboardId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getDashboardHandler.getDashboard(dashboardId, EntityUtils.takeProjectDetails(user, projectName), user);
	}

}
