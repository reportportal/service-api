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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.UpdateBugTrackingSystemHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.IntegrationUpdatedEvent;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystemAuthFactory;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.BugTrackingSystemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IntegrationFieldsConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateBugTrackingSystemRQ;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * Initial realization for {@link UpdateBugTrackingSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class UpdateBugTrackingSystemHandlerImpl implements UpdateBugTrackingSystemHandler {

	private final BasicTextEncryptor simpleEncryptor;

	private final IntegrationRepository integrationRepository;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final ProjectRepository projectRepository;

	private final BugTrackingSystemAuthFactory bugTrackingSystemAuthFactory;

	private final MessageBus messageBus;

	private final PluginBox pluginBox;

	@Autowired
	public UpdateBugTrackingSystemHandlerImpl(BasicTextEncryptor simpleEncryptor, IntegrationRepository integrationRepository,
			IntegrationTypeRepository integrationTypeRepository, ProjectRepository projectRepository,
			BugTrackingSystemAuthFactory bugTrackingSystemAuthFactory, MessageBus messageBus, PluginBox pluginBox) {
		this.simpleEncryptor = simpleEncryptor;
		this.integrationRepository = integrationRepository;
		this.integrationTypeRepository = integrationTypeRepository;
		this.projectRepository = projectRepository;
		this.bugTrackingSystemAuthFactory = bugTrackingSystemAuthFactory;
		this.messageBus = messageBus;
		this.pluginBox = pluginBox;
	}

	@Override
	public OperationCompletionRS updateBugTrackingSystem(UpdateBugTrackingSystemRQ updateRequest, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		Integration integration = integrationRepository.findById(integrationId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		BugTrackingSystemBuilder builder = new BugTrackingSystemBuilder(integration);

		Optional<IntegrationType> type = integrationTypeRepository.findByName(updateRequest.getSystemType());
		expect(type, Optional::isPresent).verify(
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Integration type '{}' was not found.", updateRequest.getSystemType())
		);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, "with id = " + projectDetails.getProjectId()));

		integration = builder.addUrl(updateRequest.getUrl())
				.addIntegrationType(type.get())
				.addBugTrackingProject(updateRequest.getProject())
				.addProject(project)
				.addUsername(updateRequest.getUsername())
				.addPassword(simpleEncryptor.encrypt(updateRequest.getPassword()))
				.addAuthType(updateRequest.getSystemAuth())
				.addAuthKey(updateRequest.getAccessKey())
				.addFields(updateRequest.getFields().stream().map(IntegrationFieldsConverter.FIELD_TO_DB).collect(Collectors.toSet()))
				.get();

		//TODO probably could be handled by database
		//				/* Check input params for avoid external system duplication */
		//		if (!sysUrl.equalsIgnoreCase(bugTrackingSystem.getUrl()) || !sysProject.equalsIgnoreCase(bugTrackingSystem.getBtsProject())
		//				|| !Objects.equals(rpProject, projectDetails.getProjectId())) {
		//			bugTrackingSystemRepository.findByUrlAndBtsProjectAndProjectId(
		//					request.getUrl(), request.getProject(), projectDetails.getProjectId()).ifPresent(it -> {
		//				throw new ReportPortalException(EXTERNAL_SYSTEM_ALREADY_EXISTS, request.getUrl() + " & " + request.getProject());
		//			});
		//		}

		//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(bugTrackingSystem.getBtsType());

		//			if (authType.requiresPassword()) {
		//				String decrypted = bugTrackingSystem.getAuth();
		//				exist.setPassword(simpleEncryptor.decrypt(exist.getPassword()));
		//				expect(externalSystemStrategy.connectionTest(exist), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
		//						projectName
		//				);
		//				exist.setPassword(decrypted);
		//			} else {
		//				expect(externalSystemStrategy.connectionTest(exist), equalTo(true)).verify(
		//						UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, projectName);
		//			}

		integrationRepository.save(integration);

		messageBus.publishActivity(new IntegrationUpdatedEvent(TO_ACTIVITY_RESOURCE.apply(integration), user.getUserId()));
		return new OperationCompletionRS("ExternalSystem with ID = '" + integrationId + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS integrationConnect(UpdateBugTrackingSystemRQ updateRequest, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails) {
		Integration integration = integrationRepository.findById(integrationId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		Integration details = new BugTrackingSystemBuilder().addUrl(updateRequest.getUrl())
				.addBugTrackingProject(updateRequest.getProject())
				.get();

		Optional<BtsExtension> extension = pluginBox.getInstance(details.getType().getName(), BtsExtension.class);

		expect(extension, Optional::isPresent).verify(UNABLE_INTERACT_WITH_INTEGRATION, integration.getProject().getId());
		expect(extension.get().connectionTest(details), equalTo(true)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				integration.getProject().getId()
		);

		return new OperationCompletionRS("Connection to ExternalSystem with ID = '" + integrationId + "' is successfully performed.");
	}
}
