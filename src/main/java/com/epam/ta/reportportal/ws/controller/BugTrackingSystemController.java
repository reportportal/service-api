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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.CreateTicketHandler;
import com.epam.ta.reportportal.core.bts.handler.GetTicketHandler;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
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
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

/**
 * Controller implementation for working with external systems.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@RestController
@RequestMapping("/v1/bts")
public class BugTrackingSystemController {

	private final ProjectExtractor projectExtractor;
	private final CreateTicketHandler createTicketHandler;
	private final GetTicketHandler getTicketHandler;

	@Autowired
	public BugTrackingSystemController(ProjectExtractor projectExtractor, CreateTicketHandler createTicketHandler, GetTicketHandler getTicketHandler) {
		this.projectExtractor = projectExtractor;
		this.createTicketHandler = createTicketHandler;
		this.getTicketHandler = getTicketHandler;
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectKey}/{integrationId}/fields-set")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of fields required for posting ticket in concrete integration")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	public List<PostFormField> getSetOfIntegrationSystemFields(@RequestParam(value = "issueType") String issuetype,
			@PathVariable String projectKey, @PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getSubmitTicketFields(issuetype,
				integrationId,
				projectExtractor.extractProjectDetails(user, EntityUtils.normalizeId(projectKey))
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectKey}/{integrationId}/issue_types")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of allowable issue types for bug tracking system")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	public List<String> getAllowableIssueTypes(@PathVariable String projectKey, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getAllowableIssueTypes(integrationId, projectExtractor.extractProjectDetails(user, EntityUtils.normalizeId(projectKey)));
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{integrationId}/fields-set")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of fields required for posting ticket")
	@PreAuthorize(ADMIN_ONLY)
	public List<PostFormField> getSetOfIntegrationSystemFields(@RequestParam(value = "issueType") String issueType,
			@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getSubmitTicketFields(issueType, integrationId);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{integrationId}/issue_types")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get list of existed issue types in bts")
	@PreAuthorize(ADMIN_ONLY)
	public List<String> getAllowableIssueTypes(@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getAllowableIssueTypes(integrationId);
	}

	@Transactional
	@PostMapping(value = "/{projectKey}/{integrationId}/ticket")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Post ticket to the bts integration")
	public Ticket createIssue(@Validated @RequestBody PostTicketRQ ticketRQ, @PathVariable String projectKey,
			@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return createTicketHandler.createIssue(ticketRQ,
				integrationId,
				projectExtractor.extractProjectDetails(user, EntityUtils.normalizeId(projectKey)),
				user
		);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectKey}/ticket/{ticketId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get ticket from the bts integration")
	public Ticket getTicket(@PathVariable String ticketId, @PathVariable String projectKey, @RequestParam(value = "btsUrl") String btsUrl,
			@RequestParam(value = "btsProject") String btsProject, @AuthenticationPrincipal ReportPortalUser user) {
		return getTicketHandler.getTicket(ticketId, btsUrl, btsProject, projectExtractor.extractProjectDetails(user, EntityUtils.normalizeId(projectKey)));
	}

}
