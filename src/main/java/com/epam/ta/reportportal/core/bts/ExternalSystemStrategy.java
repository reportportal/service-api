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

package com.epam.ta.reportportal.core.bts;

import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

import java.util.List;
import java.util.Optional;

/**
 * Define operations for working with external system.
 *
 * @author Andrei Varabyeu
 */
public interface ExternalSystemStrategy {

	/**
	 * Test connection to external system with provided details
	 *
	 * @param system - external system details
	 * @return TRUE if connection is successful. Otherwise FALSE or throws an exception if no such external system is present
	 */
	boolean connectionTest(BugTrackingSystem system);

	/**
	 * Get ticket by ID
	 *
	 * @param id     ID of ticket
	 * @param system ExternalSystem
	 * @return Found Ticket
	 */
	Optional<Ticket> getTicket(String id, BugTrackingSystem system);

	/**
	 * Submit ticket into external system
	 *
	 * @param ticketRQ Create ticket DTO
	 * @param system   External system
	 * @return Created Ticket
	 */
	Ticket submitTicket(PostTicketRQ ticketRQ, BugTrackingSystem system);

	/**
	 * Get map of fields for ticket POST into external system
	 *
	 * @param issueType Type of issue
	 * @param system    External system
	 * @return List of form fields
	 */
	List<PostFormField> getTicketFields(String issueType, BugTrackingSystem system);

	/**
	 * Get list of allowable issue types for external system
	 *
	 * @param system External system
	 * @return List of issue types
	 */
	List<String> getIssueTypes(BugTrackingSystem system);

}