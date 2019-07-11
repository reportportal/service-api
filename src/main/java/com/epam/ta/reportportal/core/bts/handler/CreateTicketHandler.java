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
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

/**
 * External system ticket creation handler
 *
 * @author Aliaksei_Makayed
 */

public interface CreateTicketHandler {

	/**
	 * Post ticket to external system.
	 *
	 * @param postTicketRQ   Request Data
	 * @param integrationId  Integration id
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param user           User
	 * @return Found Ticket
	 */
	Ticket createIssue(PostTicketRQ postTicketRQ, Long integrationId, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);
}