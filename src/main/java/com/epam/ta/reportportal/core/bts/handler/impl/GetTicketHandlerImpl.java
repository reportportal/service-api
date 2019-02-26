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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.GetBugTrackingSystemHandler;
import com.epam.ta.reportportal.core.bts.handler.GetTicketHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

/**
 * Default implementation of {@link GetTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetTicketHandlerImpl implements GetTicketHandler {

	private final PluginBox pluginBox;
	private final GetBugTrackingSystemHandler getBugTrackingSystemHandler;

	@Autowired
	public GetTicketHandlerImpl(PluginBox pluginBox, GetBugTrackingSystemHandler getBugTrackingSystemHandler) {
		this.pluginBox = pluginBox;
		this.getBugTrackingSystemHandler = getBugTrackingSystemHandler;
	}

	@Override
	public Ticket getTicket(String ticketId, String url, String btsProject, ReportPortalUser.ProjectDetails projectDetails) {

		Integration integration = getBugTrackingSystemHandler.getEnabledProjectOrGlobalIntegrationByUrlAndBtsProject(projectDetails,
				url,
				btsProject
		);

		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				integration.getType().getName()
		);

		return btsExtension.get()
				.getTicket(ticketId, integration)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TICKET_NOT_FOUND, ticketId));
	}

	@Override
	public List<PostFormField> getSubmitTicketFields(String ticketType, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails) {

		Integration integration = getBugTrackingSystemHandler.getEnabledByProjectIdAndIdOrGlobalById(projectDetails, integrationId);

		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				integration.getType().getName()
		);

		return btsExtension.get().getTicketFields(ticketType, integration);
	}

	@Override
	public List<String> getAllowableIssueTypes(Long integrationId, ReportPortalUser.ProjectDetails projectDetails) {

		Integration integration = getBugTrackingSystemHandler.getEnabledByProjectIdAndIdOrGlobalById(projectDetails, integrationId);

		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				integration.getType().getName()
		);

		return btsExtension.get().getIssueTypes(integration);
	}

}
