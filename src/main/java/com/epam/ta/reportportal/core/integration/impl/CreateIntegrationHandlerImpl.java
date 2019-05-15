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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
	public EntryCreatedRS createGlobalIntegration(IntegrationRQ createRequest, String pluginName) {
		IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));
		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integrationType.getName(),
				this.basicIntegrationService
		);
		Integration integration = integrationService.createIntegration(createRequest, integrationType);
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

		Integration integration = integrationService.createIntegration(createRequest, integrationType);
		integration.setProject(project);
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

		Integration integration = integrationRepository.findGlobalById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));
		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integration.getType().getName(),
				this.basicIntegrationService
		);

		integration = integrationService.updateIntegration(integration, updateRequest);
		integrationService.checkConnection(integration);
		integrationRepository.save(integration);

		return new OperationCompletionRS("Integration with id = " + integration.getId() + " has been successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectIntegration(Long id, String projectName, IntegrationRQ updateRequest, ReportPortalUser user) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		Integration integration = integrationRepository.findByIdAndProjectId(id, project.getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));
		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integration.getType().getName(),
				this.basicIntegrationService
		);
		integration = integrationService.updateIntegration(integration, updateRequest);
		integration.setProject(project);
		integrationService.checkConnection(integration);
		integrationRepository.save(integration);

		messageBus.publishActivity(new IntegrationUpdatedEvent(TO_ACTIVITY_RESOURCE.apply(integration),
				user.getUserId(),
				user.getUsername()
		));

		return new OperationCompletionRS("Integration with id = " + integration.getId() + " has been successfully updated.");
	}

}
