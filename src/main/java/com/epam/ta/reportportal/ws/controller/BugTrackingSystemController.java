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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.CreateTicketHandler;
import com.epam.ta.reportportal.core.bts.handler.GetTicketHandler;
import com.epam.ta.reportportal.core.bts.handler.UpdateBugTrackingSystemHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller implementation for working with external systems.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@RestController
@RequestMapping("/bts")
public class BugTrackingSystemController {

	private final CreateTicketHandler createTicketHandler;
	private final GetTicketHandler getTicketHandler;
	private final UpdateBugTrackingSystemHandler updateBugTrackingSystemHandler;

	@Autowired
	public BugTrackingSystemController(CreateTicketHandler createTicketHandler, GetTicketHandler getTicketHandler,
			UpdateBugTrackingSystemHandler updateBugTrackingSystemHandler) {
		this.createTicketHandler = createTicketHandler;
		this.getTicketHandler = getTicketHandler;
		this.updateBugTrackingSystemHandler = updateBugTrackingSystemHandler;
	}

	@Transactional
	@PutMapping(value = "/{integrationId}", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update global bug tracking system integration form fields")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS updateGlobalBtsIntegration(@Validated @RequestBody UpdateBugTrackingSystemRQ updateRequest,
			@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return updateBugTrackingSystemHandler.updateGlobalBugTrackingSystem(updateRequest, integrationId);
	}

	@Transactional
	@PutMapping(value = "/{projectName}/{integrationId}", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update project bug tracking system integration form fields")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS updateProjectBtsIntegration(@Validated @RequestBody UpdateBugTrackingSystemRQ updateRequest,
			@PathVariable String projectName, @PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return updateBugTrackingSystemHandler.updateProjectBugTrackingSystem(updateRequest,
				integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional(readOnly = true)
	@PutMapping(value = "/{projectName}/{integrationId}/connect", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Check connection to the integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS checkConnection(@PathVariable String projectName, @PathVariable Long integrationId,
			@RequestBody @Validated BtsConnectionTestRQ connectionTestRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateBugTrackingSystemHandler.integrationConnect(connectionTestRQ,
				integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName))
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectName}/{integrationId}/fields-set")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of fields required for posting ticket")
	@PreAuthorize(PROJECT_MANAGER)
	public List<PostFormField> getSetOfIntegrationSystemFields(@RequestParam(value = "issueType") String issuetype,
			@PathVariable String projectName, @PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getSubmitTicketFields(issuetype,
				integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName))
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectName}/{integrationId}/issue_types")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of fields required for posting ticket")
	@PreAuthorize(PROJECT_MANAGER)
	public List<String> getAllowableIssueTypes(@PathVariable String projectName, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getAllowableIssueTypes(integrationId, extractProjectDetails(user, EntityUtils.normalizeId(projectName)));
	}

	@Transactional
	@PostMapping(value = "/{projectName}/{integrationId}/ticket")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Post ticket to the bts integration")
	public Ticket createIssue(@Validated @RequestBody PostTicketRQ ticketRQ, @PathVariable String projectName,
			@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return createTicketHandler.createIssue(ticketRQ,
				integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectName}/ticket/{ticketId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get ticket from the bts integration")
	public Ticket getTicket(@PathVariable String ticketId, @PathVariable String projectName, @RequestParam(value = "url") String btsUrl,
			@RequestParam(value = "btsProject") String btsProject, @AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getTicket(ticketId, btsUrl, btsProject, extractProjectDetails(user, EntityUtils.normalizeId(projectName)));
	}

}
