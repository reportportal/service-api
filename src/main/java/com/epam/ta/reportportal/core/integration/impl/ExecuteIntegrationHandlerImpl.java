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

import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ExecuteIntegrationHandlerImpl implements ExecuteIntegrationHandler {

	private final Map<String, IntegrationService> integrationServiceMapping;

	private final IntegrationRepository integrationRepository;

	private final PluginBox pluginBox;

	private final IntegrationService basicIntegrationService;

	public ExecuteIntegrationHandlerImpl(IntegrationRepository integrationRepository, PluginBox pluginBox,
			@Qualifier("integrationServiceMapping") Map<String, IntegrationService> integrationServiceMapping,
			@Qualifier("basicIntegrationServiceImpl") IntegrationService basicIntegrationService) {
		this.integrationServiceMapping = integrationServiceMapping;
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
		this.basicIntegrationService = basicIntegrationService;
	}

	@Override
	public Object executeCommand(ReportPortalUser.ProjectDetails projectDetails, Long integrationId, String command,
			Map<String, ?> executionParams) {
		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, projectDetails.getProjectId())
				.orElseGet(() -> integrationRepository.findGlobalById(integrationId)
						.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId)));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integration.getType().getName(),
				basicIntegrationService
		);
		integrationService.decryptParams(integration);

		ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(integration.getType().getName(), ReportPortalExtensionPoint.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Plugin for '{}' isn't installed", integration.getType().getName()).get()
				));

		return ofNullable(pluginInstance.getCommandToExecute(command)).map(it -> it.executeCommand(integration, executionParams))
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Command '{}' is not found in plugin {}.", command, integration.getType().getName()).get()
				));
	}
}
