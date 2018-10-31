/*
 *
 *  Copyright (C) 2018 EPAM Systems
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.DeleteIntegrationHandler;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class DeleteIntegrationHandlerImpl implements DeleteIntegrationHandler {

	private final IntegrationRepository integrationRepository;

	@Autowired
	public DeleteIntegrationHandlerImpl(IntegrationRepository integrationRepository) {
		this.integrationRepository = integrationRepository;
	}

	@Override
	public OperationCompletionRS deleteIntegration(Long integrationId, ReportPortalUser.ProjectDetails projectDetails) {

		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		integrationRepository.delete(integration);

		//		eventPublisher.publishEvent(new ExternalSystemDeletedEvent(exist, username));
		return new OperationCompletionRS("ExternalSystem with ID = '" + integrationId + "' is successfully deleted.");
	}

	@Override
	public OperationCompletionRS deleteProjectIntegrations(ReportPortalUser.ProjectDetails projectDetails) {
		List<Integration> btsSystems = integrationRepository.findAllByProjectId(projectDetails.getProjectId());
		if (!CollectionUtils.isEmpty(btsSystems)) {
			integrationRepository.deleteAll(btsSystems);
		}
		//eventPublisher.publishEvent(new ProjectExternalSystemsDeletedEvent(exist, projectName, username));
		return new OperationCompletionRS(
				"All ExternalSystems for project with id ='" + projectDetails.getProjectId() + "' successfully removed");
	}
}
