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

import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.UpdateBugTrackingSystemHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.IntegrationUpdatedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.IntegrationFieldsConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.BtsConnectionTestRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateBugTrackingSystemRQ;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static java.util.Optional.ofNullable;

/**
 * Initial realization for {@link UpdateBugTrackingSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class UpdateBugTrackingSystemHandlerImpl implements UpdateBugTrackingSystemHandler {

	private final IntegrationRepository integrationRepository;
	private final ProjectRepository projectRepository;
	private final MessageBus messageBus;
	private final PluginBox pluginBox;
	private final GetIntegrationHandler getIntegrationHandler;

	@Autowired
	public UpdateBugTrackingSystemHandlerImpl(IntegrationRepository integrationRepository, ProjectRepository projectRepository,
			MessageBus messageBus, PluginBox pluginBox, GetIntegrationHandler getIntegrationHandler) {
		this.integrationRepository = integrationRepository;
		this.projectRepository = projectRepository;
		this.messageBus = messageBus;
		this.pluginBox = pluginBox;
		this.getIntegrationHandler = getIntegrationHandler;
	}

	@Override
	public OperationCompletionRS updateGlobalBugTrackingSystem(UpdateBugTrackingSystemRQ updateRequest, Long integrationId) {

		Integration integration = integrationRepository.findGlobalById(integrationId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		BusinessRule.expect(integration, it -> IntegrationGroupEnum.BTS == it.getType().getIntegrationGroup())
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Defect form fields are applicable only to BTS integration type");

		IntegrationParams parameters = ofNullable(integration.getParams()).orElseGet(IntegrationParams::new);

		BtsConstants.DEFECT_FORM_FIELDS.setParam(parameters,
				updateRequest.getFields().stream().map(IntegrationFieldsConverter.FIELD_TO_DB).collect(Collectors.toSet())
		);

		integration.setParams(parameters);

		integrationRepository.save(integration);

		return new OperationCompletionRS("ExternalSystem with ID = '" + integrationId + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectBugTrackingSystem(UpdateBugTrackingSystemRQ updateRequest, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectName()));

		Integration integration = project.getIntegrations()
				.stream()
				.filter(i -> i.getId().equals(integrationId))
				.findAny()
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		BusinessRule.expect(integration, it -> IntegrationGroupEnum.BTS == it.getType().getIntegrationGroup())
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Defect form fields are applicable only to BTS integration type");

		IntegrationParams parameters = ofNullable(integration.getParams()).orElseGet(IntegrationParams::new);

		BtsConstants.DEFECT_FORM_FIELDS.setParam(parameters,
				updateRequest.getFields().stream().map(IntegrationFieldsConverter.FIELD_TO_DB).collect(Collectors.toSet())
		);

		integration.setParams(parameters);

		integrationRepository.save(integration);

		messageBus.publishActivity(new IntegrationUpdatedEvent(TO_ACTIVITY_RESOURCE.apply(integration),
				user.getUserId(),
				user.getUsername()
		));
		return new OperationCompletionRS("ExternalSystem with ID = '" + integrationId + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS testIntegrationConnection(BtsConnectionTestRQ connectionTestRQ, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectName()));

		Integration integration = getIntegrationHandler.getEnabledBtsIntegration(projectDetails, integrationId);

		IntegrationParams testConnectionParams = prepareTestConnectionParams(ofNullable(integration.getParams()).orElseGet(IntegrationParams::new),
				connectionTestRQ
		);
		Integration connectionIntegration = new Integration();
		connectionIntegration.setParams(testConnectionParams);
		connectionIntegration.setType(integration.getType());
		connectionIntegration.setProject(project);
		connectionIntegration.setEnabled(integration.isEnabled());

		Optional<BtsExtension> extension = pluginBox.getInstance(connectionIntegration.getType().getName(), BtsExtension.class);

		expect(extension, Optional::isPresent).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Plugin extension for integration with type = '{}' has not been found",
						connectionIntegration.getType().getName()
				)
		);
		expect(extension.get().testConnection(connectionIntegration), equalTo(true)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Connection to the integration with id = '{}' refused", integrationId)
		);

		return new OperationCompletionRS("Connection to  BTS Integration with ID = '" + integrationId + "' is successfully performed.");
	}

	/**
	 * @param existingParams   {@link IntegrationParams#params}
	 * @param connectionTestRQ {@link BtsConnectionTestRQ}
	 * @return Params to test connection to the BTS
	 */
	private IntegrationParams prepareTestConnectionParams(IntegrationParams existingParams, BtsConnectionTestRQ connectionTestRQ) {

		Map<String, Object> connectionParams = Maps.newHashMap();

		Set<String> excludedParameters = Sets.newHashSet(BtsProperties.PROJECT.getName(), BtsProperties.URL.getName());

		ofNullable(existingParams.getParams()).ifPresent(params -> params.entrySet()
				.stream()
				.filter(entry -> !excludedParameters.contains(entry.getKey()))
				.forEach(entry -> connectionParams.put(entry.getKey(), entry.getValue())));

		connectionParams.put(BtsProperties.URL.getName(), connectionTestRQ.getUrl());
		connectionParams.put(BtsProperties.PROJECT.getName(), connectionTestRQ.getBtsProject());

		IntegrationParams updatedParams = new IntegrationParams();
		updatedParams.setParams(connectionParams);

		return updatedParams;

	}
}
