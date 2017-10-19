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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.externalsystem.handler.*;
import com.epam.ta.reportportal.ws.controller.IExternalSystemController;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller implementation for working with external systems.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Controller
@RequestMapping("/{projectName}/external-system")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ExternalSystemController implements IExternalSystemController {

	@Autowired
	private ICreateTicketHandler createTicketHandler;

	@Autowired
	private IGetTicketHandler getTicketHandler;

	@Autowired
	private ICreateExternalSystemHandler createExternalSystemHandler;

	@Autowired
	private IDeleteExternalSystemHandler deleteExternalSystemHandler;

	@Autowired
	private IUpdateExternalSystemHandler updateExternalSystemHandler;

	@Autowired
	private IGetExternalSystemHandler getExternalSystemHandler;

	// ==========================
	// EXTERNAL SYSTEMS BLOCK
	// ==========================
	@Override
	@RequestMapping(method = RequestMethod.POST, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@PreAuthorize(PROJECT_MANAGER)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Register external system instance")
	public EntryCreatedRS createExternalSystemInstance(@Validated @RequestBody CreateExternalSystemRQ createRQ,
			@PathVariable String projectName, Principal principal) {
		return createExternalSystemHandler.createExternalSystem(createRQ, EntityUtils.normalizeId(projectName), principal.getName());
	}

	@Override
	@RequestMapping(value = "/{systemId}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get registered external system instance")
	public ExternalSystemResource getExternalSystem(@PathVariable String projectName, @PathVariable String systemId, Principal principal) {
		return getExternalSystemHandler.getExternalSystem(EntityUtils.normalizeId(projectName), systemId);
	}

	@Override
	@RequestMapping(value = "/{systemId}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize(PROJECT_MANAGER)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete registered external system instance")
	public OperationCompletionRS deleteExternalSystem(@PathVariable String projectName, @PathVariable String systemId,
			Principal principal) {
		return deleteExternalSystemHandler.deleteExternalSystem(EntityUtils.normalizeId(projectName), systemId, principal.getName());
	}

	@Override
	@RequestMapping(value = "/clear", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize(PROJECT_MANAGER)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all external system assigned to specified project")
	public OperationCompletionRS deleteAllExternalSystems(@PathVariable String projectName, Principal principal) {
		return deleteExternalSystemHandler.deleteAllExternalSystems(EntityUtils.normalizeId(projectName), principal.getName());
	}

	@Override
	@RequestMapping(value = "/{systemId}", method = RequestMethod.PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@PreAuthorize(PROJECT_MANAGER)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update registered external system instance")
	public OperationCompletionRS updateExternalSystem(@Validated @RequestBody UpdateExternalSystemRQ request,
			@PathVariable String projectName, @PathVariable String systemId, Principal principal) {
		return updateExternalSystemHandler.updateExternalSystem(request, EntityUtils.normalizeId(projectName), systemId,
				principal.getName()
		);
	}

	@Override
	@RequestMapping(value = "/{systemId}/connect", method = RequestMethod.PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Check connection to external system instance")
	public OperationCompletionRS jiraConnection(@PathVariable String projectName, @PathVariable String systemId,
			@RequestBody @Validated UpdateExternalSystemRQ updateRQ, Principal principal) {
		return updateExternalSystemHandler.externalSystemConnect(systemId, updateRQ, principal.getName());
	}

	@Override
	@RequestMapping(value = "/{systemId}/fields-set", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Get list of fields required for posting ticket")
	public List<PostFormField> getSetOfExternalSystemFields(@RequestParam(value = "issuetype") String issuetype,
			@PathVariable String projectName, @PathVariable String systemId) {
		return getTicketHandler.getSubmitTicketFields(issuetype, EntityUtils.normalizeId(projectName), systemId);
	}

	@Override
	@RequestMapping(value = "/{systemId}/issue_types", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Get list of fields required for posting ticket")
	public List<String> getAllowableIssueTypes(@PathVariable String projectName, @PathVariable String systemId) {
		return getTicketHandler.getAllowableIssueTypes(EntityUtils.normalizeId(projectName), systemId);
	}

	// ===================
	// TICKETS BLOCK
	// ===================
	@Override
	@RequestMapping(method = RequestMethod.POST, value = "{systemId}/ticket")
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Post ticket to external system")
	public Ticket createIssue(@Validated @RequestBody PostTicketRQ ticketRQ, @PathVariable String projectName,
			@PathVariable String systemId, Principal principal) {
		return createTicketHandler.createIssue(ticketRQ, EntityUtils.normalizeId(projectName), systemId, principal.getName());
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/{systemId}/ticket/{ticketId}")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get ticket from external system")
	public Ticket getTicket(@PathVariable String ticketId, @PathVariable String projectName, @PathVariable String systemId,
			Principal principal) {
		return getTicketHandler.getTicket(ticketId, EntityUtils.normalizeId(projectName), systemId);
	}

}
