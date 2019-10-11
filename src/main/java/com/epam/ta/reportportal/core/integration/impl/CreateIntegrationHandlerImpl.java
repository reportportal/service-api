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

package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.IntegrationCreatedEvent;
import com.epam.ta.reportportal.core.events.activity.IntegrationUpdatedEvent;
import com.epam.ta.reportportal.core.integration.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.IntegrationRQ;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

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
	public EntryCreatedRS createGlobalIntegration(IntegrationRQ createRequest, String pluginName, ReportPortalUser user) {
		IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));
		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integrationType.getName(),
				this.basicIntegrationService
		);

		String integrationName = ofNullable(createRequest.getName()).map(String::toLowerCase).map(name -> {
			validateGlobalIntegrationName(name, integrationType);
			return name;
		}).orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_INTEGRATION_NAME, "Integration name should be not null"));
		createRequest.setName(integrationName);

		Integration integration = integrationService.createIntegration(createRequest, integrationType);
		integration.setCreator(user.getUsername());
		integrationService.validateIntegration(integration);
		integrationService.checkConnection(integration);
		integrationRepository.save(integration);
		return new EntryCreatedRS(integration.getId());

	}

	@Override
	public EntryCreatedRS createProjectIntegration(String projectName, IntegrationRQ createRequest, String pluginName,
			ReportPortalUser user) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integrationType.getName(),
				this.basicIntegrationService
		);

		String integrationName = ofNullable(createRequest.getName()).map(String::toLowerCase).map(name -> {
			validateProjectIntegrationName(name, integrationType, project);
			return name;
		}).orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_INTEGRATION_NAME, "Integration name should be not null"));
		createRequest.setName(integrationName);

		Integration integration = integrationService.createIntegration(createRequest, integrationType);
		integration.setProject(project);
		integration.setCreator(user.getUsername());
		integrationService.validateIntegration(integration, project);
		integrationService.checkConnection(integration);

		integrationRepository.save(integration);

		messageBus.publishActivity(new IntegrationCreatedEvent(TO_ACTIVITY_RESOURCE.apply(integration),
				user.getUserId(),
				user.getUsername()
		));

		return new EntryCreatedRS(integration.getId());
	}

	@Override
	public OperationCompletionRS updateGlobalIntegration(Long id, IntegrationRQ updateRequest) {

		final Integration integration = integrationRepository.findGlobalById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));

		ofNullable(updateRequest.getName()).map(String::toLowerCase).ifPresent(name -> {
			if (!name.equals(integration.getName())) {
				validateGlobalIntegrationName(name, integration.getType());
				updateRequest.setName(name);
			}
		});

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integration.getType().getName(),
				this.basicIntegrationService
		);

		Integration updatedIntegration = integrationService.updateIntegration(integration, updateRequest);
		integrationService.checkConnection(integration);
		integrationRepository.save(updatedIntegration);

		return new OperationCompletionRS("Integration with id = " + updatedIntegration.getId() + " has been successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectIntegration(Long id, String projectName, IntegrationRQ updateRequest, ReportPortalUser user) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		final Integration integration = integrationRepository.findByIdAndProjectId(id, project.getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));

		ofNullable(updateRequest.getName()).map(String::toLowerCase).ifPresent(name -> {
			if (!name.equals(integration.getName())) {
				validateProjectIntegrationName(name, integration.getType(), project);
				updateRequest.setName(name);
			}
		});

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integration.getType().getName(),
				this.basicIntegrationService
		);
		Integration updatedIntegration = integrationService.updateIntegration(integration, updateRequest);
		updatedIntegration.setProject(project);
		integrationService.checkConnection(integration);

		integrationRepository.save(updatedIntegration);

		messageBus.publishActivity(new IntegrationUpdatedEvent(TO_ACTIVITY_RESOURCE.apply(updatedIntegration),
				user.getUserId(),
				user.getUsername()
		));

		return new OperationCompletionRS("Integration with id = " + updatedIntegration.getId() + " has been successfully updated.");
	}

	private void validateGlobalIntegrationName(String integrationName, IntegrationType integrationType) {
		BusinessRule.expect(integrationName, StringUtils::isNotBlank)
				.verify(ErrorType.INCORRECT_INTEGRATION_NAME, "Integration name should be not empty");
		BusinessRule.expect(integrationRepository.existsByNameAndTypeIdAndProjectIdIsNull(integrationName, integrationType.getId()),
				BooleanUtils::isFalse
		)
				.verify(ErrorType.INTEGRATION_ALREADY_EXISTS,
						Suppliers.formattedSupplier("Global integration of type = '{}' with name = '{}' already exists",
								integrationType.getName(),
								integrationName
						)
				);
	}

	private void validateProjectIntegrationName(String integrationName, IntegrationType integrationType, Project project) {
		BusinessRule.expect(integrationName, StringUtils::isNotBlank)
				.verify(ErrorType.INCORRECT_INTEGRATION_NAME, "Integration name should be not empty");
		BusinessRule.expect(integrationRepository.existsByNameAndTypeIdAndProjectId(integrationName,
				integrationType.getId(),
				project.getId()
		), BooleanUtils::isFalse)
				.verify(ErrorType.INTEGRATION_ALREADY_EXISTS,
						Suppliers.formattedSupplier("Project integration of type = '{}' with name = '{}' already exists on project = '{}'",
								integrationType.getName(),
								integrationName,
								project.getName()
						)
				);
	}

}
