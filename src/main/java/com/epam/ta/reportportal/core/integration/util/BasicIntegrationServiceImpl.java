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

package com.epam.ta.reportportal.core.integration.util;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationBuilder;
import com.epam.ta.reportportal.ws.model.integration.IntegrationRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class BasicIntegrationServiceImpl implements IntegrationService {

	private static final String TEST_CONNECTION_COMMAND = "testConnection";
	private static final String RETRIEVE_VALID_PARAMS = "retrieveValid";

	protected IntegrationRepository integrationRepository;

	protected PluginBox pluginBox;

	@Autowired
	public BasicIntegrationServiceImpl(IntegrationRepository integrationRepository, PluginBox pluginBox) {
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
	}

	@Override
	public Integration createIntegration(IntegrationRQ integrationRq, IntegrationType integrationType) {
		return new IntegrationBuilder().withCreationDate(LocalDateTime.now())
				.withType(integrationType)
				.withEnabled(integrationRq.getEnabled())
				.withName(integrationRq.getName())
				.withParams(new IntegrationParams(retrieveValidParams(integrationType.getName(), integrationRq.getIntegrationParams())))
				.get();
	}

	@Override
	public Integration updateIntegration(Integration integration, IntegrationRQ integrationRQ) {
		IntegrationParams combinedParams = getCombinedParams(integration, integrationRQ.getIntegrationParams());
		Map<String, Object> validParams = retrieveValidParams(integration.getType().getName(), combinedParams.getParams());
		integration.setParams(new IntegrationParams(validParams));
		ofNullable(integrationRQ.getEnabled()).ifPresent(integration::setEnabled);
		ofNullable(integrationRQ.getName()).ifPresent(integration::setName);
		return integration;
	}

	@Override
	public Map<String, Object> retrieveValidParams(String integrationType, Map<String, Object> integrationParams) {
		final PluginCommand<?> pluginCommand = getCommandByName(integrationType, RETRIEVE_VALID_PARAMS);
		return (Map<String, Object>) pluginCommand.executeCommand(null, integrationParams);
	}

	@Override
	public boolean checkConnection(Integration integration) {
		final PluginCommand<?> pluginCommand = getCommandByName(integration.getType().getName(), TEST_CONNECTION_COMMAND);
		return (Boolean) pluginCommand.executeCommand(integration, integration.getParams().getParams());
	}

	private PluginCommand<?> getCommandByName(String integration, String commandName) {
		ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(integration, ReportPortalExtensionPoint.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Plugin for {} isn't installed", integration));
		return ofNullable(pluginInstance.getCommandToExecute(commandName)).orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
				"Command {} is not found in plugin {}.",
				commandName,
				integration
		));
	}

	private IntegrationParams getCombinedParams(Integration integration, Map<String, Object> retrievedParams) {
		if (integration.getParams() != null && integration.getParams().getParams() != null) {
			integration.getParams().getParams().putAll(retrievedParams);
			return integration.getParams();
		}
		return new IntegrationParams(retrievedParams);
	}
}
