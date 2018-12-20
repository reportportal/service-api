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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.bts.handler.*;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
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
@RequestMapping("/{projectName}/integration")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class IntegrationController {

	private final CreateTicketHandler createTicketHandler;
	private final GetTicketHandler getTicketHandler;
	private final UpdateIntegrationHandler updateIntegrationHandler;
	private final CreateIntegrationHandler createIntegrationHandler;
	private final DeleteIntegrationHandler deleteIntegrationHandler;
	private final GetIntegrationHandler getIntegrationHandler;

	@Autowired
	public IntegrationController(CreateTicketHandler createTicketHandler, GetTicketHandler getTicketHandler,
			UpdateIntegrationHandler updateIntegrationHandler, CreateIntegrationHandler createIntegrationHandler,
			DeleteIntegrationHandler deleteIntegrationHandler, GetIntegrationHandler getIntegrationHandler) {
		this.createTicketHandler = createTicketHandler;
		this.getTicketHandler = getTicketHandler;
		this.updateIntegrationHandler = updateIntegrationHandler;
		this.createIntegrationHandler = createIntegrationHandler;
		this.deleteIntegrationHandler = deleteIntegrationHandler;
		this.getIntegrationHandler = getIntegrationHandler;
	}

	@Transactional
	@PostMapping(consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public EntryCreatedRS createIntegrationInstance(@Validated @RequestBody CreateIntegrationRQ createRQ, @PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createIntegrationHandler.createIntegration(createRQ,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get integration instance")
	public IntegrationResource getIntegration(@PathVariable String projectName, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getIntegrationHandler.getIntegrationById(integrationId, extractProjectDetails(user, EntityUtils.normalizeId(projectName)));
	}

	@Transactional
	@DeleteMapping(value = "/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteIntegration(@PathVariable String projectName, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteIntegration(integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional
	@DeleteMapping(value = "/clear")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all integrations assigned to specified project")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteAllIntegrations(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteProjectIntegrations(extractProjectDetails(user, EntityUtils.normalizeId(projectName)), user);
	}

	@Transactional
	@PutMapping(value = "/{integrationId}", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS updateIntegration(@Validated @RequestBody UpdateIntegrationRQ request, @PathVariable String projectName,
			@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return updateIntegrationHandler.updateIntegration(request,
				integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional
	@PutMapping(value = "/{integrationId}/connect", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Check connection to the integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS checkConnection(@PathVariable String projectName, @PathVariable Long integrationId,
			@RequestBody @Validated UpdateIntegrationRQ updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateIntegrationHandler.integrationConnect(updateRQ,
				integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName))
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{integrationId}/fields-set")
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
	@GetMapping(value = "/{integrationId}/issue_types")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of fields required for posting ticket")
	@PreAuthorize(PROJECT_MANAGER)
	public List<String> getAllowableIssueTypes(@PathVariable String projectName, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getAllowableIssueTypes(integrationId, extractProjectDetails(user, EntityUtils.normalizeId(projectName)));
	}

	//
	//	// ===================
	//	// TICKETS BLOCK
	//	// ===================
	//
	@Transactional
	@PostMapping(value = "{integrationId}/ticket")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Post ticket to the bts integration")
	public Ticket createIssue(@Validated @RequestBody PostTicketRQ ticketRQ, @PathVariable String projectName,
			@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return createTicketHandler.createIssue(
				ticketRQ,
				integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{integrationId}/ticket/{ticketId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get ticket from the bts integration")
	public Ticket getTicket(@PathVariable String ticketId, @PathVariable String projectName, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getTicket(ticketId, integrationId, extractProjectDetails(user, EntityUtils.normalizeId(projectName)));
	}

}
