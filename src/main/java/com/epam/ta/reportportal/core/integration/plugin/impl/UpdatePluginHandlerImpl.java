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

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.UpdatePluginHandler;
import com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdatePluginStateRQ;
import org.apache.commons.io.FileUtils;
import org.pf4j.PluginState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class UpdatePluginHandlerImpl implements UpdatePluginHandler {

	private final Pf4jPluginBox pluginBox;
	private final IntegrationTypeRepository integrationTypeRepository;
	private final DataStore dataStore;
	private final String pluginsRootPath;

	@Autowired
	public UpdatePluginHandlerImpl(Pf4jPluginBox pluginBox, IntegrationTypeRepository integrationTypeRepository, DataStore dataStore,
			@Value("${rp.plugins.path}") String pluginsRootPath) {
		this.pluginBox = pluginBox;
		this.integrationTypeRepository = integrationTypeRepository;
		this.dataStore = dataStore;
		this.pluginsRootPath = pluginsRootPath;
	}

	@Override
	public OperationCompletionRS updatePluginState(Long id, UpdatePluginStateRQ updatePluginStateRQ) {

		IntegrationType integrationType = integrationTypeRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Integration type with id - '{}' not found.", id).get()
				));

		ReportPortalIntegrationEnum reportPortalIntegration = ReportPortalIntegrationEnum.findByName(integrationType.getName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Unknown integration type - {}.", integrationType.getName()).get()
				));

		boolean isEnabled = updatePluginStateRQ.getEnabled();
		integrationType.setEnabled(isEnabled);

		if (reportPortalIntegration.isPlugin()) {
			return handlePluginState(integrationType, isEnabled);
		}

		return new OperationCompletionRS(Suppliers.formattedSupplier("Enabled state of the integration type with name = '{}' has been switched to - '{}'",
				integrationType.getName(),
				isEnabled
		)
				.get());
	}

	private OperationCompletionRS handlePluginState(IntegrationType integrationType, boolean isEnabled) {

		if (isEnabled) {
			loadPlugin(integrationType);
		} else {
			unloadPlugin(integrationType.getName());
		}

		return new OperationCompletionRS(Suppliers.formattedSupplier("Enabled state of the plugin with id = '{}' has been switched to - '{}'",
				integrationType.getName(),
				isEnabled
		).get());
	}

	private void loadPlugin(IntegrationType integrationType) {

		if (!pluginBox.getPluginById(integrationType.getName()).isPresent()) {

			Map<String, Object> details = ofNullable(integrationType.getDetails()).map(IntegrationTypeDetails::getDetails)
					.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
							"Integration type details have not been found"
					));

			String pluginFileName = IntegrationDetailsProperties.FILE_NAME.getValue(details)
					.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
							Suppliers.formattedSupplier("Plugin file name property for Integration type with name - '{}' not found.",
									integrationType.getName()
							).get()
					));

			if (!Files.exists(Paths.get(pluginsRootPath, pluginFileName))) {

				String pluginFileId = IntegrationDetailsProperties.FILE_NAME.getValue(details)
						.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
								Suppliers.formattedSupplier("Plugin file name property for Integration type with name - '{}' not found.",
										integrationType.getName()
								).get()
						));

				try (InputStream inputStream = ofNullable(dataStore.load(pluginFileId)).orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Error during downloading the file of the plugin with ID = '{}' from the data storage",
								integrationType.getName()
						)
								.get()
				))) {
					FileUtils.copyToFile(inputStream, new File(pluginsRootPath, pluginFileName));
				} catch (IOException e) {

					throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
							Suppliers.formattedSupplier("Error during copying the file of the plugin with ID = '{}'",
									integrationType.getName()
							).get()
					);
				}
			}

			Optional<String> pluginId = ofNullable(pluginBox.loadPlugin(Paths.get(pluginsRootPath, pluginFileName)));

			if (pluginId.isPresent()) {
				startUpPlugin(pluginId.get());
			} else {
				throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Error during loading the plugin with id = '{}'", integrationType.getName()).get()
				);
			}
		}
	}

	private void startUpPlugin(String pluginId) {
		if (!(pluginBox.startUpPlugin(pluginId) == PluginState.STARTED)) {
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					Suppliers.formattedSupplier("Error during starting up the plugin with id = '{}'", pluginId).get()
			);
		}
	}

	private void unloadPlugin(String pluginId) {

		pluginBox.getPluginById(pluginId).ifPresent(plugin -> {

			if (!pluginBox.unloadPlugin(plugin.getPluginId())) {
				throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Error during unloading the plugin with id = '{}'", pluginId).get()
				);
			}
		});
	}
}
