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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import java.util.Map;

import com.epam.ta.reportportal.commons.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.epam.ta.reportportal.core.dashboard.ICreateDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IDeleteDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IGetDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IUpdateDashboardHandler;
import com.epam.ta.reportportal.ws.controller.IDashboardController;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;

import io.swagger.annotations.ApiOperation;

/**
 * Controller implementation for
 * {@link com.epam.ta.reportportal.database.entity.Dashboard} entity
 * 
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Controller
@RequestMapping("/{projectName}/dashboard")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class DashboardController implements IDashboardController {

	@Autowired
	private ICreateDashboardHandler createHandler;

	@Autowired
	private IDeleteDashboardHandler deleteHandler;

	@Autowired
	private IGetDashboardHandler getHandler;

	@Autowired
	private IUpdateDashboardHandler updateHandler;

	@Override
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(CREATED)
	@ResponseBody
	@ApiOperation("Create dashboard for specified project")
	public EntryCreatedRS createDashboard(@PathVariable String projectName, @RequestBody @Validated CreateDashboardRQ createRQ,
			Principal principal) {
		return createHandler.createDashboard(EntityUtils.normalizeProjectName(projectName), createRQ, principal.getName());
	}

	@Override
	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get all dashboard resources for specified project")
	public Iterable<DashboardResource> getAllDashboards(@PathVariable String projectName, Principal principal) {
		return getHandler.getAllDashboards(principal.getName(), EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get specified dashboard by ID for specified project")
	public DashboardResource getDashboard(@PathVariable String projectName, @PathVariable String dashboardId, Principal principal) {
		return getHandler.getDashboard(dashboardId, principal.getName(), EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Update specified dashboard for specified project")
	public OperationCompletionRS updateDashboard(@PathVariable String projectName, @PathVariable String dashboardId,
			@RequestBody @Validated UpdateDashboardRQ updateRQ, Principal principal) {
		return updateHandler.updateDashboard(updateRQ, dashboardId, principal.getName(), EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.DELETE)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Delete specified dashboard by ID for specified project")
	public OperationCompletionRS deleteDashboard(@PathVariable String projectName, @PathVariable String dashboardId, Principal principal) {
		return deleteHandler.deleteDashboard(dashboardId, principal.getName(), EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(value = "/shared", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get names of shared dashboards from specified project")
	public Map<String, SharedEntity> getSharedDashboardsNames(@PathVariable String projectName, Principal principal) {
		return getHandler.getSharedDashboardsNames(principal.getName(), EntityUtils.normalizeProjectName(projectName));
	}
}