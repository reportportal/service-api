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

package com.epam.ta.reportportal.core.bts.handler;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

import java.util.List;

/**
 * Get ticket handler(from external system)
 *
 * @author Aliaksei_Makayed
 */
public interface GetTicketHandler {

	/**
	 * Get ticket from specified external system by id.<br>
	 * <b>Note: resulting object returned from cache.</b>
	 *
	 * @param ticketId       Ticket ID
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param btsUrl         URL of the bug tracking system to get ticket from
	 * @param btsProject     Project in the bug tracking system to get ticket from
	 * @return Ticket
	 */
	Ticket getTicket(String ticketId, String btsUrl, String btsProject, ReportPortalUser.ProjectDetails projectDetails);

	/**
	 * Get set of fields of external system to submit a ticket
	 *
	 * @param ticketType     Ticket Type
	 * @param integrationId  Integration id
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return Found fields
	 */
	List<PostFormField> getSubmitTicketFields(String ticketType, Long integrationId, ReportPortalUser.ProjectDetails projectDetails);

	List<PostFormField> getSubmitTicketFields(String issueType, Long integrationId);

	/**
	 * Get allowable issue types
	 *
	 * @param integrationId  Integration id
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return Fields
	 */
	List<String> getAllowableIssueTypes(Long integrationId, ReportPortalUser.ProjectDetails projectDetails);

	List<String> getAllowableIssueTypes(Long integrationId);
}