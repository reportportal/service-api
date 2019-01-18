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
import com.epam.ta.reportportal.core.admin.ServerAdminHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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

	@Transactional
	@RequestMapping(value = "/analytics", method = { RequestMethod.PUT, RequestMethod.POST })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Update analytics settings")
	public OperationCompletionRS saveAnalyticsSettings(@RequestBody @Validated AnalyticsResource request,
			@AuthenticationPrincipal ReportPortalUser user) {
		return serverHandler.saveAnalyticsSettings(request);
	}

	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Get server settings")
	public ServerSettingsResource getServerSettings(@AuthenticationPrincipal ReportPortalUser user) {
		return serverHandler.getServerSettings();
	}
}