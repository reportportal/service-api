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

package com.epam.ta.reportportal.core.bts.handler;

import com.epam.ta.reportportal.auth.ReportPortalUser;
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
	 * @param user        Report Portal user
	 * @return Ticket
	 */
	Ticket getTicket(String ticketId, String projectName, Long systemId, ReportPortalUser user);

	/**
	 * Get set of fields of external system to submit a ticket
	 *
	 * @param ticketType  Ticket Type
	 * @param projectName ProjectName
	 * @param systemId    System id
	 * @param user        Report Portal user
	 *
	 * @return Found fields
	 */
	List<PostFormField> getSubmitTicketFields(String ticketType, String projectName, Long systemId, ReportPortalUser user);

	/**
	 * Get allowable issue types
	 *
	 * @param projectName Project Name
	 * @param systemId    External System ID
	 * @param user        Report Portal user
	 *
	 * @return Fields
	 */
	List<String> getAllowableIssueTypes(String projectName, Long systemId, ReportPortalUser user);
}