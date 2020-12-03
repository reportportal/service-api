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
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.MergeLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.logging.HttpLogging;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.composeBaseUrl;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Controller implementation for async reporting client API for
 * {@link com.epam.ta.reportportal.entity.launch.Launch} entity
 *
 * @author Konstantin Antipin
 */
@RestController
@RequestMapping("/v2/{projectName}/launch")
public class LaunchAsyncController {

	private final StartLaunchHandler startLaunchHandler;
	private final FinishLaunchHandler finishLaunchHandler;
	private final MergeLaunchHandler mergeLaunchesHandler;

	@Autowired
	public LaunchAsyncController(@Qualifier("startLaunchHandlerAsync") StartLaunchHandler startLaunchHandler,
			@Qualifier("finishLaunchHandlerAsync") FinishLaunchHandler finishLaunchHandler,
			MergeLaunchHandler mergeLaunchesHandler) {
		this.startLaunchHandler = startLaunchHandler;
		this.finishLaunchHandler = finishLaunchHandler;
		this.mergeLaunchesHandler = mergeLaunchesHandler;
	}

	@HttpLogging
	@PostMapping
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(CREATED)
	@ApiOperation("Starts launch for specified project")
	public StartLaunchRS startLaunch(@PathVariable String projectName,
			@ApiParam(value = "Start launch request body", required = true) @RequestBody @Validated StartLaunchRQ startLaunchRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return startLaunchHandler.startLaunch(user, extractProjectDetails(user, normalizeId(projectName)), startLaunchRQ);
	}

	@HttpLogging
	@PutMapping(value = "/{launchId}/finish")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Finish launch for specified project")
	public FinishLaunchRS finishLaunch(@PathVariable String projectName, @PathVariable String launchId,
			@RequestBody @Validated FinishExecutionRQ finishLaunchRQ, @AuthenticationPrincipal ReportPortalUser user,
			HttpServletRequest request) {
		return finishLaunchHandler.finishLaunch(
				launchId,
				finishLaunchRQ,
				extractProjectDetails(user, normalizeId(projectName)),
				user,
				composeBaseUrl(request)
		);
	}

	@HttpLogging
	@Transactional
	@PostMapping("/merge")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Merge set of specified launches in common one")
	public LaunchResource mergeLaunches(@PathVariable String projectName,
			@ApiParam(value = "Merge launches request body", required = true) @RequestBody @Validated MergeLaunchesRQ mergeLaunchesRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return mergeLaunchesHandler.mergeLaunches(extractProjectDetails(user, normalizeId(projectName)), user, mergeLaunchesRQ);
	}

}
