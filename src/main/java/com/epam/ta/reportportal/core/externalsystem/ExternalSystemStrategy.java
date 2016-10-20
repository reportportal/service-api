/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.externalsystem;

import java.util.List;
import java.util.Optional;

import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

/**
 * Define operations for working with external system.
 * 
 * @author Andrei Varabyeu
 *
 */
public interface ExternalSystemStrategy {

	/**
	 * Test connection to external system with provided details
	 * 
	 * @param system
	 *            - external system details
	 * @param principal
	 *            - request principal name (for OAuth only)
	 * @return
	 */
	boolean connectionTest(ExternalSystem system, String principal);

	/**
	 * Get ticket by ID
	 * 
	 * @param id
	 * @param system
	 * @return
	 */
	Optional<Ticket> getTicket(String id, ExternalSystem system);

	/**
	 * Submit ticket into external system
	 * 
	 * @param ticketRQ
	 * @param system
	 * @return
	 */
	Ticket submitTicket(PostTicketRQ ticketRQ, ExternalSystem system);

	/**
	 * Get map of fields for ticket POST into external system
	 * 
	 * @param issueType
	 * @param system
	 * @return
	 */
	List<PostFormField> getTicketFields(String issueType, ExternalSystem system);

}