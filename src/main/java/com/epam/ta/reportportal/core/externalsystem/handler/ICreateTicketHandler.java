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

import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

/**
 * External system ticket creation handler
 *
 * @author Aliaksei_Makayed
 */

public interface ICreateTicketHandler {

	/**
	 * Post ticket to external system.
	 *
	 * @param postTicketRQ Request Data
	 * @param project      Project Name
	 * @param systemId     ID of external system
	 * @param username     Name of logged in user
	 * @return Found Ticket
	 */
	Ticket createIssue(PostTicketRQ postTicketRQ, String project, String systemId, String username);
}