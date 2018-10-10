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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.admin.ServerAdminHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;

/**
 * @author Andrei_Ramanchuk
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@RestController
@RequestMapping("/settings")
@PreAuthorize(ADMIN_ONLY)
public class SettingsController {

	private final ServerAdminHandler serverHandler;

	@Autowired
	public SettingsController(ServerAdminHandler serverHandler) {
		this.serverHandler = serverHandler;
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Get server email settings")
	public ServerSettingsResource getServerSettings(@AuthenticationPrincipal ReportPortalUser user) {
		return serverHandler.getServerSettings();
	}

	@RequestMapping(value = "/email", method = { RequestMethod.POST, RequestMethod.PUT })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Set server email settings")
	public OperationCompletionRS saveEmailSettings(@RequestBody @Validated ServerEmailResource request,
			@AuthenticationPrincipal ReportPortalUser user) {
		return serverHandler.saveEmailSettings(request);
	}

	@DeleteMapping(value = "/email")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Delete email settings for specified profile")
	public OperationCompletionRS deleteEmailSettings(@AuthenticationPrincipal ReportPortalUser user) {
		return serverHandler.deleteEmailSettings();
	}

	@RequestMapping(value = "/analytics", method = { RequestMethod.PUT, RequestMethod.POST })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Update analytics settings")
	public OperationCompletionRS saveAnalyticsSettings(@RequestBody @Validated AnalyticsResource request,
			@AuthenticationPrincipal ReportPortalUser user) {
		return serverHandler.saveAnalyticsSettings(request);
	}
}