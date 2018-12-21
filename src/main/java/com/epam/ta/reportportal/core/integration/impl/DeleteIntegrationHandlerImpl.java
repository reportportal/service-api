/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.IntegrationDeletedEvent;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class DeleteIntegrationHandlerImpl implements DeleteIntegrationHandler {

	private final IntegrationRepository integrationRepository;

	private final MessageBus messageBus;

	@Autowired
	public DeleteIntegrationHandlerImpl(IntegrationRepository integrationRepository, MessageBus messageBus) {
		this.integrationRepository = integrationRepository;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS deleteProjectIntegration(Long integrationId, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {

		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		integrationRepository.delete(integration);

		messageBus.publishActivity(new IntegrationDeletedEvent(TO_ACTIVITY_RESOURCE.apply(integration), user.getUserId()));
		return new OperationCompletionRS("ExternalSystem with ID = '" + integrationId + "' is successfully deleted.");
	}

	@Override
	public OperationCompletionRS deleteProjectIntegrations(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<Integration> integrations = integrationRepository.findAllByProjectId(projectDetails.getProjectId());
		if (!CollectionUtils.isEmpty(integrations)) {
			integrationRepository.deleteAll(integrations);
		}
		integrations.stream()
				.map(TO_ACTIVITY_RESOURCE)
				.forEach(it -> messageBus.publishActivity(new IntegrationDeletedEvent(it, user.getUserId())));
		return new OperationCompletionRS(
				"All ExternalSystems for project with id ='" + projectDetails.getProjectId() + "' successfully removed");
	}
}
