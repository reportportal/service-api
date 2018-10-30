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
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.IGetTicketHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link IGetTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetTicketHandler implements IGetTicketHandler {

	private final IntegrationRepository integrationRepository;
	private final PluginBox pluginBox;

	@Autowired
	public GetTicketHandler(IntegrationRepository integrationRepository, PluginBox pluginBox) {
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
	}

	@Override
	public Ticket getTicket(String ticketId, String projectName, Long systemId, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectExtractor.extractProjectDetails(user, projectName);
		List<Integration> integrations = integrationRepository.findAllByProjectId(projectDetails.getProjectId());
		expect(integrations, not(CollectionUtils::isEmpty)).verify(PROJECT_NOT_CONFIGURED, projectName);
		Integration integration = integrations.stream()
				.filter(it -> Objects.equals(it.getId(), systemId))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, systemId));

		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				integration.getType().getName()
		);
		return btsExtension.get().getTicket(ticketId, integration).get();
	}

	@Override
	public List<PostFormField> getSubmitTicketFields(String ticketType, String projectName, Long systemId, ReportPortalUser user) {
		ProjectExtractor.extractProjectDetails(user, projectName);
		Integration integration = validateExternalSystem(systemId);

		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				integration.getType().getName()
		);
		return btsExtension.get().getTicketFields(ticketType, integration);
	}

	@Override
	public List<String> getAllowableIssueTypes(String projectName, Long systemId, ReportPortalUser user) {
		ProjectExtractor.extractProjectDetails(user, projectName);
		Integration integration = validateExternalSystem(systemId);
		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				integration.getType().getName()
		);
		return btsExtension.get().getIssueTypes(integration);
	}

	private Integration validateExternalSystem(Long systemId) {
		return integrationRepository.findById(systemId).orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, systemId));
	}
}
