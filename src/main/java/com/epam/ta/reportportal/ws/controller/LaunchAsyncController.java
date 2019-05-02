/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.launch.util.LaunchLinkGenerator;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.FinishLaunchRS;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
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
@RequestMapping("/{projectName}/async/launch")
public class LaunchAsyncController {

	private final StartLaunchHandler createLaunchMessageHandler;
	private final FinishLaunchHandler finishLaunchMessageHandler;

	@Autowired
	public LaunchAsyncController(@Qualifier("startLaunchHandlerAsync") StartLaunchHandler createLaunchMessageHandler,
                                 @Qualifier("finishLaunchHandlerAsync") FinishLaunchHandler finishLaunchMessageHandler) {
		this.createLaunchMessageHandler = createLaunchMessageHandler;
		this.finishLaunchMessageHandler = finishLaunchMessageHandler;
	}

	@PostMapping
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(CREATED)
	@ApiOperation("Starts launch for specified project")
	public StartLaunchRS startLaunch(@PathVariable String projectName,
									 @ApiParam(value = "Start launch request body", required = true) @RequestBody @Validated StartLaunchRQ startLaunchRQ,
									 @AuthenticationPrincipal ReportPortalUser user) {
		return createLaunchMessageHandler.startLaunch(user, extractProjectDetails(user, normalizeId(projectName)), startLaunchRQ);
	}

	@PutMapping(value = "/{launchId}/finish")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Finish launch for specified project")
	public FinishLaunchRS finishLaunch(@PathVariable String projectName, @PathVariable String launchId,
									   @RequestBody @Validated FinishExecutionRQ finishLaunchRQ, @AuthenticationPrincipal ReportPortalUser user,
									   HttpServletRequest request) {
		return finishLaunchMessageHandler.finishLaunch(launchId,
				finishLaunchRQ,
				extractProjectDetails(user, normalizeId(projectName)),
				user,
				LaunchLinkGenerator.LinkParams.of(request.getScheme(), request.getHeader("host"), projectName)
		);
	}

	@Transactional
	@PutMapping("/{launchId}/stop")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch for specified project")
	public OperationCompletionRS forceFinishLaunch(@PathVariable String projectName, @PathVariable String launchId,
												   @RequestBody @Validated FinishExecutionRQ finishExecutionRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return finishLaunchMessageHandler.stopLaunch(launchId,
				finishExecutionRQ,
				extractProjectDetails(user, normalizeId(projectName)),
				user
		);
	}

	@Transactional
	@PutMapping("/stop")
	@PreAuthorize(ALLOWED_TO_REPORT)
	@ResponseStatus(OK)
	@ApiOperation("Force finish launch")
	public List<OperationCompletionRS> bulkForceFinish(@PathVariable String projectName,
													   @RequestBody @Validated BulkRQ<String, FinishExecutionRQ> rq, @AuthenticationPrincipal ReportPortalUser user) {
		return finishLaunchMessageHandler.stopLaunch(rq, extractProjectDetails(user, normalizeId(projectName)), user);
	}

}
