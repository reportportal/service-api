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

import com.epam.ta.reportportal.core.launch.IGetLaunchHandler;
import com.epam.ta.reportportal.core.project.IGetProjectHandler;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.util.SystemInformatorService;
import com.epam.ta.reportportal.ws.controller.IPluginsController;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SystemInfoRS;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

/**
 * Separate controller for external plug-ins of ReportPortal requests handling.
 * Basic implementation of {@link IPluginsController}
 *
 * @author Andrei_Ramanchuk
 */
@Controller
public class PluginController implements IPluginsController {

	@Autowired
	private IGetLaunchHandler getLaunchMessageHandler;

	@Autowired
	private IGetProjectHandler getProjectHandler;

	@Autowired
	private SystemInformatorService infoService;

	@Override
	@RequestMapping(value = "/{projectName}/launch/jenkins", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	public LaunchResource getLaunchByName(@PathVariable String projectName, @SortFor(Launch.class) Pageable pageable,
			@FilterFor(Launch.class) Filter filter, Principal principal) {
		return getLaunchMessageHandler.getLaunchByName(normalizeId(projectName), pageable, filter, principal.getName());
	}

	@Override
	@RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Heartbeat method for Tomcat and MongoDB (authentication required)")
	public OperationCompletionRS heartBeat(HttpServletRequest request, Principal principal) {
		return getProjectHandler.isProjectsAvailable();
	}

	@Override
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public SystemInfoRS getSystemStatus() {
		return infoService.getSystemInformation();
	}
}
