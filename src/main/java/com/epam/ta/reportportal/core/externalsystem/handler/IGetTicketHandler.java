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

package com.epam.ta.reportportal.core.externalsystem.handler;

import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

import java.util.List;

/**
 * Get ticket handler(from external system)
 *
 * @author Aliaksei_Makayed
 */
public interface IGetTicketHandler {

	/**
	 * Get ticket from specified external system by id.<br>
	 * <b>Note: resulting object returned from cache.</b>
	 *
	 * @param ticketId    Ticket ID
	 * @param projectName Project Name
	 * @param systemId    ID of external system
	 * @return Ticket
	 */
	Ticket getTicket(String ticketId, String projectName, String systemId);

	/**
	 * Get set of fields of external system to submit a ticket
	 *
	 * @param ticketType  Ticket Type
	 * @param projectName ProjectName
	 * @param id          TicketID
	 * @return Found fields
	 */
	List<PostFormField> getSubmitTicketFields(String ticketType, String projectName, String id);

	/**
	 * Get allowable issue types
	 *
	 * @param projectName Project Name
	 * @param systemId    External System ID
	 * @return Fields
	 */
	List<String> getAllowableIssueTypes(String projectName, String systemId);
}