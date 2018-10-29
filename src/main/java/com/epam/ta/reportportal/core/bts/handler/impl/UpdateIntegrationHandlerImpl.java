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
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.UpdateIntegrationHandler;
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
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemFieldsConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * Initial realization for {@link UpdateIntegrationHandler} interface
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class UpdateIntegrationHandlerImpl implements UpdateIntegrationHandler {

	//	@Autowired
	//	private StrategyProvider strategyProvider;

	private final BasicTextEncryptor simpleEncryptor;

	private final IntegrationRepository integrationRepository;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final ProjectRepository projectRepository;

	private final BugTrackingSystemAuthFactory bugTrackingSystemAuthFactory;

	private final PluginBox pluginBox;

	@Autowired
	public UpdateIntegrationHandlerImpl(BasicTextEncryptor simpleEncryptor, IntegrationRepository integrationRepository,
			IntegrationTypeRepository integrationTypeRepository, ProjectRepository projectRepository,
			BugTrackingSystemAuthFactory bugTrackingSystemAuthFactory, PluginBox pluginBox) {
		this.simpleEncryptor = simpleEncryptor;
		this.integrationRepository = integrationRepository;
		this.integrationTypeRepository = integrationTypeRepository;
		this.projectRepository = projectRepository;
		this.bugTrackingSystemAuthFactory = bugTrackingSystemAuthFactory;
		this.pluginBox = pluginBox;
	}

	@Override
	public OperationCompletionRS updateIntegration(UpdateExternalSystemRQ updateRQ, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails) {

		Integration bugTrackingSystem = integrationRepository.findById(integrationId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		BugTrackingSystemBuilder builder = new BugTrackingSystemBuilder(bugTrackingSystem);

		Optional<IntegrationType> type = integrationTypeRepository.findByName(updateRQ.getExternalSystemType());
		expect(type, Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Integration type '{}' was not found.", updateRQ.getExternalSystemType())
		);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, "with id = " + projectDetails.getProjectId()));

		bugTrackingSystem = builder.addUrl(updateRQ.getUrl())
				.addIntegrationType(type.get())
				.addBugTrackingProject(updateRQ.getProject())
				.addProject(project)
				.addUsername(updateRQ.getUsername())
				.addPassword(simpleEncryptor.encrypt(updateRQ.getPassword()))
				.addAuthType(updateRQ.getExternalSystemAuth())
				.addFields(updateRQ.getFields().stream().map(ExternalSystemFieldsConverter.FIELD_TO_DB).collect(Collectors.toSet()))
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

		integrationRepository.save(bugTrackingSystem);

		//eventPublisher.publishEvent(new IntegrationUpdatedEvent(exist, principalName));
		return new OperationCompletionRS("ExternalSystem with ID = '" + integrationId + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS integrationConnect(UpdateExternalSystemRQ updateRQ, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails) {
		Integration bugTrackingSystem = integrationRepository.findById(integrationId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		Integration details = new BugTrackingSystemBuilder().addUrl(updateRQ.getUrl()).addBugTrackingProject(updateRQ.getProject()).get();

		Optional<BtsExtension> extension = pluginBox.getInstance(details.getType().getName(), BtsExtension.class);

		expect(extension, Optional::isPresent).verify(UNABLE_INTERACT_WITH_INTEGRATION, bugTrackingSystem.getProject().getId());
		expect(extension.get().connectionTest(details), equalTo(true)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				bugTrackingSystem.getProject().getId()
		);

		return new OperationCompletionRS("Connection to ExternalSystem with ID = '" + integrationId + "' is successfully performed.");
	}
}
