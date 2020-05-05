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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.integration.IntegrationRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class BasicIntegrationServiceImpl implements IntegrationService {

	private static final String TEST_CONNECTION_COMMAND = "testConnection";

	protected IntegrationRepository integrationRepository;

	protected PluginBox pluginBox;

	@Autowired
	public BasicIntegrationServiceImpl(IntegrationRepository integrationRepository, PluginBox pluginBox) {
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
	}

	@Override
	public Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams) {
		return integrationParams;
	}

	@Override
	public void decryptParams(Integration integration) {

	}

	@Override
	public void encryptParams(Integration integration) {

	}

	private static IntegrationParams getIntegrationParams(Integration integration, Map<String, Object> retrievedParams) {
		if (integration.getParams() != null && integration.getParams().getParams() != null) {
			integration.getParams().getParams().putAll(retrievedParams);
			return integration.getParams();
		}
		return new IntegrationParams(retrievedParams);
	}

	@Override
	public Integration createIntegration(IntegrationRQ integrationRq, IntegrationType integrationType) {
		return new IntegrationBuilder().withCreationDate(LocalDateTime.now())
				.withType(integrationType)
				.withEnabled(integrationRq.getEnabled())
				.withName(integrationRq.getName())
				.withParams(new IntegrationParams(retrieveIntegrationParams(integrationRq.getIntegrationParams())))
				.get();
	}

	@Override
	public Integration updateIntegration(Integration integration, IntegrationRQ integrationRQ) {
		Map<String, Object> integrationParams = retrieveIntegrationParams(integrationRQ.getIntegrationParams());
		IntegrationParams params = getIntegrationParams(integration, integrationParams);
		integration.setParams(params);
		ofNullable(integrationRQ.getEnabled()).ifPresent(integration::setEnabled);
		ofNullable(integrationRQ.getName()).ifPresent(integration::setName);
		return integration;
	}

	@Override
	public boolean validateIntegration(Integration integration) {
		List<Integration> global = integrationRepository.findAllGlobalByType(integration.getType());
		BusinessRule.expect(global, List::isEmpty).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration with type " + integration.getType().getName() + " is already exists"
		);
		return true;
	}

	@Override
	public boolean validateIntegration(Integration integration, Project project) {
		List<Integration> integrations = integrationRepository.findAllByProjectIdAndType(project.getId(), integration.getType());
		BusinessRule.expect(integrations, List::isEmpty).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration with type " + integration.getType().getName() + " is already exists for project " + project.getName()
		);
		return true;
	}

	@Override
	public boolean checkConnection(Integration integration) {
		ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(integration.getType().getName(), ReportPortalExtensionPoint.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						"Plugin for {} isn't installed",
						integration.getType().getName()
				));

		PluginCommand commandToExecute = ofNullable(pluginInstance.getCommandToExecute(TEST_CONNECTION_COMMAND)).orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
				"Command {} is not found in plugin {}.",
				TEST_CONNECTION_COMMAND,
				integration.getType().getName()
		));

		return (Boolean) commandToExecute.executeCommand(integration, integration.getParams().getParams());
	}
}
