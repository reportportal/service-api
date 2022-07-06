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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Supplier;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ExecuteIntegrationHandlerImpl implements ExecuteIntegrationHandler {

	private static final String ASYNC_MODE = "async";

	//Required field for user authorization in plugin
	private static final String PROJECT_ID = "projectId";
	private static final String PUBLIC_COMMAND_PREFIX = "public_";

	private final IntegrationRepository integrationRepository;

	private final PluginBox pluginBox;

	public ExecuteIntegrationHandlerImpl(IntegrationRepository integrationRepository, PluginBox pluginBox) {
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
	}

	@Override
	public Object executeCommand(ReportPortalUser.ProjectDetails projectDetails, String pluginName, String command,
			Map<String, Object> executionParams) {
		ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						formattedSupplier("Plugin for '{}' isn't installed", pluginName).get()
				));
		executionParams.put(PROJECT_ID, projectDetails.getProjectId());
		return ofNullable(pluginInstance.getCommonCommand(command)).map(it -> it.executeCommand(executionParams))
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						formattedSupplier("Command '{}' is not found in plugin {}.", command, pluginName).get()
				));
	}

	@Override
	public Object executePublicCommand(String pluginName, String command, Map<String, Object> executionParams) {
		BusinessRule.expect(command, c -> c.startsWith(PUBLIC_COMMAND_PREFIX))
				.verify(ACCESS_DENIED, formattedSupplier("Command '{}' is not public.", command).get());
		ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						formattedSupplier("Plugin for '{}' isn't installed", pluginName).get()
				));
		return ofNullable(pluginInstance.getCommonCommand(command)).map(it -> it.executeCommand(executionParams))
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						formattedSupplier("Public command '{}' is not found in plugin {}.", command, pluginName).get()
				));
	}

	@Override
	public Object executeCommand(ReportPortalUser.ProjectDetails projectDetails, Long integrationId, String command,
			Map<String, Object> executionParams) {
		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, projectDetails.getProjectId())
				.orElseGet(() -> integrationRepository.findGlobalById(integrationId)
						.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId)));

		ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(integration.getType().getName(), ReportPortalExtensionPoint.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						formattedSupplier("Plugin for '{}' isn't installed", integration.getType().getName()).get()
				));

		Boolean asyncMode = ofNullable((Boolean) executionParams.get(ASYNC_MODE)).orElse(false);

		executionParams.put(PROJECT_ID, projectDetails.getProjectId());

		return ofNullable(pluginInstance.getIntegrationCommand(command)).map(it -> {
			if (asyncMode) {
				supplyAsync(() -> it.executeCommand(integration, executionParams));
				return new OperationCompletionRS(formattedSupplier("Command '{}' accepted for processing in plugin",
						command,
						integration.getType().getName()
				).get());
			}
			return it.executeCommand(integration, executionParams);
		}).orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
				formattedSupplier("Command '{}' is not found in plugin {}.", command, integration.getType().getName()).get()
		));
	}

	@Async
	//need for security context sharing into plugin
	public <U> void supplyAsync(Supplier<U> supplier) {
		supplier.get();
	}
}
