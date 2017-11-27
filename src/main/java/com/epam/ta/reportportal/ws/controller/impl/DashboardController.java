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

import com.epam.ta.reportportal.core.dashboard.ICreateDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IDeleteDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IGetDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.IUpdateDashboardHandler;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.controller.IDashboardController;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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
		return createHandler.createDashboard(normalizeId(projectName), createRQ, principal.getName());
	}

	@Override
	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get all dashboard resources for specified project")
	public Iterable<DashboardResource> getAllDashboards(@PathVariable String projectName, Principal principal) {
		return getHandler.getAllDashboards(principal.getName(), normalizeId(projectName));
	}

	@Override
	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get specified dashboard by ID for specified project")
	public DashboardResource getDashboard(@PathVariable String projectName, @PathVariable String dashboardId, Principal principal) {
		return getHandler.getDashboard(dashboardId, principal.getName(), normalizeId(projectName));
	}

	@Override
	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Update specified dashboard for specified project")
	public OperationCompletionRS updateDashboard(@PathVariable String projectName, @PathVariable String dashboardId,
			@RequestBody @Validated UpdateDashboardRQ updateRQ, Principal principal, @ActiveRole UserRole userRole) {
		return updateHandler.updateDashboard(updateRQ, dashboardId, principal.getName(), normalizeId(projectName), userRole);
	}

	@Override
	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.DELETE)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Delete specified dashboard by ID for specified project")
	public OperationCompletionRS deleteDashboard(@PathVariable String projectName, @PathVariable String dashboardId,
			@ActiveRole UserRole userRole, Principal principal) {
		return deleteHandler.deleteDashboard(dashboardId, principal.getName(), normalizeId(projectName), userRole);
	}

	@Override
	@RequestMapping(value = "/shared", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get names of shared dashboards from specified project")
	public Iterable<SharedEntity> getSharedDashboardsNames(@PathVariable String projectName, Principal principal, Pageable pageable) {
		return getHandler.getSharedDashboardsNames(principal.getName(), normalizeId(projectName), pageable);
	}
}
