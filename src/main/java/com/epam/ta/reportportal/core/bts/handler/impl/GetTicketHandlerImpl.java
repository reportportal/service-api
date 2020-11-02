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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.GetTicketHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
	private final GetIntegrationHandler getIntegrationHandler;

	@Autowired
	public GetTicketHandlerImpl(PluginBox pluginBox, GetIntegrationHandler getIntegrationHandler) {
		this.pluginBox = pluginBox;
		this.getIntegrationHandler = getIntegrationHandler;
	}

	@Override
	public Ticket getTicket(String ticketId, String url, String btsProject, ReportPortalUser.ProjectDetails projectDetails) {
		Integration integration = getIntegrationHandler.getEnabledBtsIntegration(projectDetails, url, btsProject);
		return getBtsExtension(integration).getTicket(ticketId, integration)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TICKET_NOT_FOUND, ticketId));
	}

	@Override
	public List<PostFormField> getSubmitTicketFields(String ticketType, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails) {
		Integration integration = getIntegrationHandler.getEnabledBtsIntegration(projectDetails, integrationId);
		return getBtsExtension(integration).getTicketFields(ticketType, integration);
	}

	@Override
	public List<PostFormField> getSubmitTicketFields(String ticketType, Long integrationId) {
		Integration integration = getIntegrationHandler.getEnabledBtsIntegration(integrationId);
		return getBtsExtension(integration).getTicketFields(ticketType, integration);
	}

	@Override
	public List<String> getAllowableIssueTypes(Long integrationId, ReportPortalUser.ProjectDetails projectDetails) {
		Integration integration = getIntegrationHandler.getEnabledBtsIntegration(projectDetails, integrationId);
		return getBtsExtension(integration).getIssueTypes(integration);
	}

	@Override
	public List<String> getAllowableIssueTypes(Long integrationId) {
		Integration integration = getIntegrationHandler.getEnabledBtsIntegration(integrationId);
		return getBtsExtension(integration).getIssueTypes(integration);
	}

	private BtsExtension getBtsExtension(Integration integration) {
		return pluginBox.getInstance(integration.getType().getName(), BtsExtension.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("BugTracking plugin for {} isn't installed", integration.getType().getName()).get()
				));
	}

}
