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

package com.epam.ta.reportportal.core.integration.impl;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ExecuteIntegrationHandlerImpl implements ExecuteIntegrationHandler {

	private final IntegrationRepository integrationRepository;

	private final PluginBox pluginBox;

	@Autowired
	public ExecuteIntegrationHandlerImpl(IntegrationRepository integrationRepository, PluginBox pluginBox) {
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
	}

	@Override
	public Object executeCommand(ReportPortalUser.ProjectDetails projectDetails, Long integrationId, String command,
			Map<String, ?> executionParams) {

		Integration integration = integrationRepository.findById(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

		ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(integration.getType().getName(), ReportPortalExtensionPoint.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						"Plugin for {} isn't installed",
						integration.getType().getName()
				));

		PluginCommand commandToExecute = Optional.ofNullable(pluginInstance.getCommandToExecute(command))
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						"Command {} is not found in plugin {}.",
						command,
						integration.getType().getName()
				));

		return commandToExecute.executeCommand(integration, executionParams);
	}
}
