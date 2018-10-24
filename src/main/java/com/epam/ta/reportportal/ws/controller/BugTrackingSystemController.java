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
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.bts.handler.ICreateTicketHandler;
import com.epam.ta.reportportal.core.bts.handler.IGetTicketHandler;
import com.epam.ta.reportportal.core.bts.handler.IUpdateExternalSystemHandler;
import com.epam.ta.reportportal.core.bts.handler.impl.IntegrationsHandler;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller implementation for working with external systems.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@RestController
@RequestMapping("/{projectName}/external-system")
//@PreAuthorize(ASSIGNED_TO_PROJECT)
public class BugTrackingSystemController {

	private final ICreateTicketHandler createTicketHandler;
	private final IGetTicketHandler getTicketHandler;
	private final IUpdateExternalSystemHandler updateExternalSystemHandler;
	private final IntegrationsHandler integrationsHandler;

	@Autowired
	public BugTrackingSystemController(ICreateTicketHandler createTicketHandler, IGetTicketHandler getTicketHandler,
			IntegrationsHandler integrationsHandler, IUpdateExternalSystemHandler updateExternalSystemHandler) {
		this.createTicketHandler = createTicketHandler;
		this.getTicketHandler = getTicketHandler;
		this.integrationsHandler = integrationsHandler;
		this.updateExternalSystemHandler = updateExternalSystemHandler;

	}

	@Transactional
	@PostMapping(consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Register external system instance")
	//@PreAuthorize(PROJECT_MANAGER)
	public EntryCreatedRS createExternalSystemInstance(@Validated @RequestBody CreateExternalSystemRQ createRQ,
			@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return integrationsHandler.createExternalSystem(createRQ, EntityUtils.normalizeId(projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{systemId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get registered external system instance")
	public IntegrationResource getExternalSystem(@PathVariable String projectName, @PathVariable Long systemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return integrationsHandler.getIntegrationByID(projectName, systemId);
	}

	@Transactional
	@DeleteMapping(value = "/{systemId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete registered external system instance")
	//@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteExternalSystem(@PathVariable String projectName, @PathVariable Long systemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return integrationsHandler.deleteIntegration(projectName, systemId, user);
	}

	@Transactional
	@DeleteMapping(value = "/clear")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all external system assigned to specified project")
	//@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteAllExternalSystems(@PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return integrationsHandler.deleteProjectIntegrations(EntityUtils.normalizeId(projectName), user);
	}

	@Transactional
	@PutMapping(value = "/{systemId}", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update registered external system instance")
	//@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS updateExternalSystem(@Validated @RequestBody UpdateExternalSystemRQ request,
			@PathVariable String projectName, @PathVariable Long systemId, @AuthenticationPrincipal ReportPortalUser user) {
		return updateExternalSystemHandler.updateExternalSystem(request, EntityUtils.normalizeId(projectName), systemId, user);
	}

	@Transactional
	@PutMapping(value = "/{systemId}/connect", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Check connection to external system instance")
	//@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS checkConnection(@PathVariable String projectName, @PathVariable Long systemId,
			@RequestBody @Validated UpdateExternalSystemRQ updateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateExternalSystemHandler.externalSystemConnect(updateRQ, EntityUtils.normalizeId(projectName), systemId, user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{systemId}/fields-set")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of fields required for posting ticket")
	//	@PreAuthorize(PROJECT_MANAGER)
	public List<PostFormField> getSetOfExternalSystemFields(@RequestParam(value = "issuetype") String issuetype,
			@PathVariable String projectName, @PathVariable Long systemId, @AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getSubmitTicketFields(issuetype, EntityUtils.normalizeId(projectName), systemId, user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{systemId}/issue_types")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of fields required for posting ticket")
	//	@PreAuthorize(PROJECT_MANAGER)
	public List<String> getAllowableIssueTypes(@PathVariable String projectName, @PathVariable Long systemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getAllowableIssueTypes(EntityUtils.normalizeId(projectName), systemId, user);
	}

	//
	//	// ===================
	//	// TICKETS BLOCK
	//	// ===================
	//
	@Transactional
	@PostMapping(value = "{systemId}/ticket")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Post ticket to external system")
	public Ticket createIssue(@Validated @RequestBody PostTicketRQ ticketRQ, @PathVariable String projectName, @PathVariable Long systemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createTicketHandler.createIssue(ticketRQ, EntityUtils.normalizeId(projectName), systemId, user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{systemId}/ticket/{ticketId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get ticket from external system")
	public Ticket getTicket(@PathVariable String ticketId, @PathVariable String projectName, @PathVariable Long systemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getTicket(ticketId, EntityUtils.normalizeId(projectName), systemId, user);
	}

}
