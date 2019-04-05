/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.IntegrationCreatedEvent;
import com.epam.ta.reportportal.core.events.activity.IntegrationUpdatedEvent;
import com.epam.ta.reportportal.core.integration.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreateIntegrationHandlerImpl implements CreateIntegrationHandler {

	private final Map<String, IntegrationService> integrationServiceMapping;

	private final IntegrationRepository integrationRepository;

	private final ProjectRepository projectRepository;

	private final MessageBus messageBus;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final IntegrationService basicIntegrationService;

	@Autowired
	public CreateIntegrationHandlerImpl(@Qualifier("integrationServiceMapping") Map<String, IntegrationService> integrationServiceMapping,
			IntegrationRepository integrationRepository, ProjectRepository projectRepository, MessageBus messageBus,
			IntegrationTypeRepository integrationTypeRepository,
			@Qualifier("basicIntegrationServiceImpl") IntegrationService integrationService) {
		this.integrationServiceMapping = integrationServiceMapping;
		this.integrationRepository = integrationRepository;
		this.projectRepository = projectRepository;
		this.messageBus = messageBus;
		this.integrationTypeRepository = integrationTypeRepository;
		this.basicIntegrationService = integrationService;
	}

	@Override
	public OperationCompletionRS createGlobalIntegration(UpdateIntegrationRQ updateRequest) {

		IntegrationType integrationType = integrationTypeRepository.findByName(updateRequest.getIntegrationName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, updateRequest.getIntegrationName()));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integrationType.getName(),
				this.basicIntegrationService
		);

		Map<String, Object> validParams = integrationService.retrieveIntegrationParams(updateRequest.getIntegrationParams());
		Integration integration = createIntegration(integrationType, validParams);
		integrationService.validateIntegration(integration, null);
		integration.setEnabled(updateRequest.getEnabled());
		integrationRepository.save(integration);

		return new OperationCompletionRS("Integration with id = " + integration.getId() + " has been successfully created.");

	}

	@Override
	public OperationCompletionRS createProjectIntegration(ReportPortalUser.ProjectDetails projectDetails, UpdateIntegrationRQ updateRequest,
			ReportPortalUser user) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		IntegrationType integrationType = integrationTypeRepository.findByName(updateRequest.getIntegrationName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, updateRequest.getIntegrationName()));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integrationType.getName(),
				this.basicIntegrationService
		);

		Map<String, Object> validParams = integrationService.retrieveIntegrationParams(updateRequest.getIntegrationParams());

		Integration integration = createIntegration(integrationType, validParams);
		integrationService.validateIntegration(integration, projectDetails);
		integration.setEnabled(updateRequest.getEnabled());
		integration.setProject(project);

		integrationRepository.save(integration);

		messageBus.publishActivity(new IntegrationCreatedEvent(TO_ACTIVITY_RESOURCE.apply(integration), user.getUserId()));

		return new OperationCompletionRS("Integration with id = " + integration.getId() + " has been successfully created.");
	}

	@Override
	public OperationCompletionRS updateGlobalIntegration(Long id, UpdateIntegrationRQ updateRequest) {

		IntegrationType integrationType = integrationTypeRepository.findByName(updateRequest.getIntegrationName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, updateRequest.getIntegrationName()));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integrationType.getName(),
				this.basicIntegrationService
		);

		Integration integration = integrationRepository.findGlobalById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));

		integration.setParams(new IntegrationParams(integrationService.retrieveIntegrationParams(updateRequest.getIntegrationParams())));
		integration.setEnabled(updateRequest.getEnabled());

		integrationRepository.save(integration);

		return new OperationCompletionRS("Integration with id = " + integration.getId() + " has been successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectIntegration(Long id, ReportPortalUser.ProjectDetails projectDetails,
			UpdateIntegrationRQ updateRequest, ReportPortalUser user) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		IntegrationType integrationType = integrationTypeRepository.findByName(updateRequest.getIntegrationName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, updateRequest.getIntegrationName()));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integrationType.getName(),
				this.basicIntegrationService
		);

		Integration integration = integrationRepository.findByIdAndProjectId(id, project.getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));

		integration.setParams(new IntegrationParams(integrationService.retrieveIntegrationParams(updateRequest.getIntegrationParams())));
		integration.setEnabled(updateRequest.getEnabled());
		integration.setProject(project);

		integrationRepository.save(integration);

		messageBus.publishActivity(new IntegrationUpdatedEvent(TO_ACTIVITY_RESOURCE.apply(integration), user.getUserId()));

		return new OperationCompletionRS("Integration with id = " + integration.getId() + " has been successfully updated.");
	}

	private Integration createIntegration(IntegrationType integrationType, Map<String, Object> integrationParams) {
		Integration integration = new Integration();
		integration.setCreationDate(LocalDateTime.now());
		integration.setParams(new IntegrationParams(integrationParams));
		integration.setType(integrationType);
		return integration;
	}

}
