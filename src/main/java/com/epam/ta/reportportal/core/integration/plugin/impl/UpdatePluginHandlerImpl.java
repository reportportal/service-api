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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.UpdatePluginHandler;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.ReservedIntegrationTypeEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdatePluginStateRQ;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class UpdatePluginHandlerImpl implements UpdatePluginHandler {

	private final Pf4jPluginBox pluginBox;
	private final IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	public UpdatePluginHandlerImpl(Pf4jPluginBox pluginBox, IntegrationTypeRepository integrationTypeRepository) {
		this.pluginBox = pluginBox;
		this.integrationTypeRepository = integrationTypeRepository;
	}

	@Override
	public OperationCompletionRS updatePluginState(Long id, UpdatePluginStateRQ updatePluginStateRQ) {

		IntegrationType integrationType = integrationTypeRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Integration type with id - '{}' not found.", id).get()
				));

		boolean isEnabled = updatePluginStateRQ.getEnabled();
		integrationType.setEnabled(isEnabled);
		return handlePluginState(integrationType, isEnabled);
	}

	private OperationCompletionRS handlePluginState(IntegrationType integrationType, boolean isEnabled) {

		/*
		 *	hack: while email and ldap isn't a plugin - it shouldn't be proceeded as a plugin
		 *  it is configured as a integration type on the database startup
		 *  should be replaced as a separate tables for both 'email' or 'ldap' or remove them
		 *  and rewrite as a plugin
		 */
		if (ReservedIntegrationTypeEnum.fromName(integrationType.getName()).isPresent()) {
			return new OperationCompletionRS(Suppliers.formattedSupplier("Enabled state of the plugin with id = '{}' has been switched to - '{}'",
					integrationType.getName(),
					isEnabled
			).get());
		}


		if (isEnabled) {
			loadPlugin(integrationType);
		} else {
			unloadPlugin(integrationType);
		}

		return new OperationCompletionRS(Suppliers.formattedSupplier("Enabled state of the plugin with id = '{}' has been switched to - '{}'",
				integrationType.getName(),
				isEnabled
		).get());
	}

	private void loadPlugin(IntegrationType integrationType) {
		if (pluginBox.getPluginById(integrationType.getName()).isEmpty()) {
			boolean isLoaded = pluginBox.loadPlugin(integrationType.getName(), integrationType.getDetails());
			BusinessRule.expect(isLoaded, BooleanUtils::isTrue).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					Suppliers.formattedSupplier("Error during loading the plugin with id = '{}'", integrationType.getName()).get()
			);
		}
	}

	private void unloadPlugin(IntegrationType integrationType) {

		pluginBox.getPluginById(integrationType.getName()).ifPresent(plugin -> {

			if (!pluginBox.unloadPlugin(integrationType)) {
				throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Error during unloading the plugin with id = '{}'", integrationType.getName()).get()
				);
			}
		});
	}
}
