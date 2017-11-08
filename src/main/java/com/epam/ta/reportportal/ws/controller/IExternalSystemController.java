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

import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.*;

import java.security.Principal;
import java.util.List;

/**
 * Report Portal WS Interface. Bug tracking controller
 *
 * @author Aliaksei_Makayed
 */

public interface IExternalSystemController {

	/**
	 * Create resource for external system instance
	 *
	 * @param createRQ    Request Data
	 * @param projectName Project Name
	 * @param principal   Login
	 * @return Operation Result
	 */
	EntryCreatedRS createExternalSystemInstance(CreateExternalSystemRQ createRQ, String projectName, Principal principal);

	/**
	 * Get resource of specified external system
	 *
	 * @param projectName Project Name
	 * @param id          ID of External System
	 * @param principal   Login
	 * @return Found External System
	 */
	ExternalSystemResource getExternalSystem(String projectName, String id, Principal principal);

	/**
	 * Delete registered external system
	 *
	 * @param projectName Project Name
	 * @param id          ID of system
	 * @param principal   Login
	 * @return Operation result
	 */
	OperationCompletionRS deleteExternalSystem(String projectName, String id, Principal principal);

	/**
	 * Delete all external systems assigned to specified Report Portal project
	 *
	 * @param projectName Project Name
	 * @param principal   Login
	 * @return Operation result
	 */
	OperationCompletionRS deleteAllExternalSystems(String projectName, Principal principal);

	/**
	 * Update external system method
	 *
	 * @param request     Request Data
	 * @param projectName Project Name
	 * @param id          System ID
	 * @param principal   Login
	 * @return Operation Result
	 */
	OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ request, String projectName, String id, Principal principal);

	/**
	 * ExternalSystem Basic\NTLM connection validation
	 *
	 * @param projectName Project Name
	 * @param id          System ID
	 * @param updateRQ    Request Data
	 * @param principal   Login
	 * @return Operation Result
	 */
	OperationCompletionRS jiraConnection(String projectName, String id, UpdateExternalSystemRQ updateRQ, Principal principal);

	/**
	 * Get list of external system fields for UI representation
	 *
	 * @param issuetype   Type of Issue
	 * @param projectName Name of Project
	 * @param id          System ID
	 * @return Found fields
	 */
	List<PostFormField> getSetOfExternalSystemFields(String issuetype, String projectName, String id);

	/**
	 * Get list of external system fields for UI representation
	 *
	 * @param projectName Name of Project
	 * @param systemId    System ID
	 * @return Found fields
	 */
	List<String> getAllowableIssueTypes(String projectName, String systemId);

	/**
	 * Post issue to bug tracking system.
	 *
	 * @param ticketRQ    Ticket details
	 * @param projectName Name of project
	 * @param systemId    System ID
	 * @param principal   Login
	 * @return Found Ticket
	 */
	Ticket createIssue(PostTicketRQ ticketRQ, String projectName, String systemId, Principal principal);

	/**
	 * Get ticket from external system
	 *
	 * @param ticketId    Ticket ID
	 * @param projectName Project Name
	 * @param systemId    System ID
	 * @param principal   Login
	 * @return Found Ticket
	 */
	Ticket getTicket(String ticketId, String projectName, String systemId, Principal principal);

}