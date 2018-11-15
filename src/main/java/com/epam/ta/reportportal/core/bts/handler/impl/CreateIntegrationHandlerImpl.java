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

import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.IntegrationCreatedEvent;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.BugTrackingSystemBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateIntegrationRQ;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class CreateIntegrationHandlerImpl implements CreateIntegrationHandler {

	private final BasicTextEncryptor simpleEncryptor;

	private final IntegrationRepository integrationRepository;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final ProjectRepository projectRepository;

	private final PluginBox pluginBox;

	private final MessageBus messageBus;

	@Autowired
	public CreateIntegrationHandlerImpl(BasicTextEncryptor simpleEncryptor, IntegrationRepository integrationRepository,
			IntegrationTypeRepository integrationTypeRepository, ProjectRepository projectRepository, PluginBox pluginBox,
			MessageBus messageBus) {
		this.simpleEncryptor = simpleEncryptor;
		this.integrationRepository = integrationRepository;
		this.integrationTypeRepository = integrationTypeRepository;
		this.projectRepository = projectRepository;
		this.pluginBox = pluginBox;
		this.messageBus = messageBus;
	}

	public EntryCreatedRS createIntegration(CreateIntegrationRQ createRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {

		Optional<IntegrationType> type = integrationTypeRepository.findByName(createRQ.getExternalSystemType());
		expect(type, Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Integration type '{}' was not found.", createRQ.getExternalSystemType())
		);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, "with id = " + projectDetails.getProjectId()));

		Integration bugTrackingSystem = new BugTrackingSystemBuilder().addUrl(createRQ.getUrl())
				.addIntegrationType(type.get())
				.addBugTrackingProject(createRQ.getProject())
				.addProject(project)
				.addUsername(createRQ.getUsername())
				.addPassword(simpleEncryptor.encrypt(createRQ.getPassword()))
				.addAuthType(createRQ.getExternalSystemAuth())
				.addAuthKey(createRQ.getAccessKey())
				.get();

		checkUnique(bugTrackingSystem, projectDetails.getProjectId());

		Optional<BtsExtension> extenstion = pluginBox.getInstance(createRQ.getExternalSystemType(), BtsExtension.class);
		expect(extenstion, Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Could not find plugin with name '{}'.", createRQ.getExternalSystemType())
		);

		expect(extenstion.get().connectionTest(bugTrackingSystem), BooleanUtils::isTrue).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Connection refused."
		);

		integrationRepository.save(bugTrackingSystem);
		messageBus.publishActivity(new IntegrationCreatedEvent(TO_ACTIVITY_RESOURCE.apply(bugTrackingSystem), user.getUserId()));
		return new EntryCreatedRS(bugTrackingSystem.getId());
	}

	private void checkUnique(Integration integration, Long projectId) {
		String url = (String) BtsConstants.URL.getParam(integration.getParams())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
		String btsProject = (String) BtsConstants.PROJECT.getParam(integration.getParams())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "BTS project is not specified."));
		expect(integrationRepository.findByUrlAndBtsProjectAndProjectId(url, btsProject, projectId),
				not(isPresent())
		).verify(ErrorType.INTEGRATION_ALREADY_EXISTS, url + " & " + btsProject);
	}
}
