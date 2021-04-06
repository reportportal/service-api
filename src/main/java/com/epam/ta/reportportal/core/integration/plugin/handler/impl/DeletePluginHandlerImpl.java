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

package com.epam.ta.reportportal.core.integration.plugin.handler.impl;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.handler.DeletePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.file.PluginFileManager;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.ReservedIntegrationTypeEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.IntegrationTypeConverter.TO_PATH_INFO;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DeletePluginHandlerImpl implements DeletePluginHandler {

	private final IntegrationTypeRepository integrationTypeRepository;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final PluginFileManager pluginFileManager;
	private final Pf4jPluginBox pluginBox;

	@Autowired
	public DeletePluginHandlerImpl(IntegrationTypeRepository integrationTypeRepository, ApplicationEventPublisher applicationEventPublisher,
			PluginFileManager pluginFileManager, Pf4jPluginBox pluginBox) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.applicationEventPublisher = applicationEventPublisher;
		this.pluginFileManager = pluginFileManager;
		this.pluginBox = pluginBox;
	}

	@Override
	public OperationCompletionRS deleteById(Long id) {

		IntegrationType integrationType = integrationTypeRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Plugin with id = '{}' not found", id).get()
				));

		expect(ReservedIntegrationTypeEnum.fromName(integrationType.getName()), Optional::isEmpty).verify(ErrorType.PLUGIN_REMOVE_ERROR,
				Suppliers.formattedSupplier("Unable to remove reserved plugin - '{}'", integrationType.getName())
		);

		integrationTypeRepository.deleteById(integrationType.getId());
		deletePlugin(integrationType);

		return new OperationCompletionRS(Suppliers.formattedSupplier("Plugin = '{}' has been successfully removed",
				integrationType.getName()
		).get());

	}

	private void deletePlugin(IntegrationType integrationType) {
		pluginBox.getPluginById(integrationType.getName()).ifPresent(pluginWrapper -> {
			if (!pluginBox.deletePlugin(pluginWrapper)) {
				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR, "Unable to remove from plugin manager.");
			}
			ofNullable(integrationType.getDetails()).map(details -> TO_PATH_INFO.apply(details, pluginWrapper))
					.ifPresent(pluginFileManager::delete);
		});
	}
}
