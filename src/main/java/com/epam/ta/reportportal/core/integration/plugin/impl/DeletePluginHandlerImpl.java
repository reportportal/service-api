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
import com.epam.ta.reportportal.core.integration.plugin.DeletePluginHandler;
import com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DeletePluginHandlerImpl implements DeletePluginHandler {

	private final String pluginsDir;
	private final IntegrationTypeRepository integrationTypeRepository;
	private final Pf4jPluginBox pluginBox;
	private final DataStore dataStore;

	@Autowired
	public DeletePluginHandlerImpl(@Value("${rp.plugins.path}") String pluginsDir, IntegrationTypeRepository integrationTypeRepository,
			Pf4jPluginBox pluginBox, DataStore dataStore) {
		this.pluginsDir = pluginsDir;
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginBox = pluginBox;
		this.dataStore = dataStore;
	}

	@Override
	public OperationCompletionRS deleteById(Long id) {

		IntegrationType integrationType = integrationTypeRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Plugin with id = '{}' not found", id).get()
				));

		pluginBox.getPluginById(integrationType.getName()).ifPresent(pluginWrapper -> {
			if (!pluginBox.deletePlugin(pluginWrapper.getPluginId())) {
				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR, "Unable to remove from plugin manager.");
			}
		});
		ofNullable(integrationType.getDetails()).flatMap(details -> ofNullable(details.getDetails())).ifPresent(this::deletePluginFiles);
		integrationTypeRepository.deleteById(integrationType.getId());

		return new OperationCompletionRS(Suppliers.formattedSupplier("Plugin = '{}' has been successfully removed",
				integrationType.getName()
		).get());

	}

	private void deletePluginFiles(Map<String, Object> details) {
		IntegrationDetailsProperties.FILE_NAME.getValue(details).map(String::valueOf).ifPresent(fileName -> {
			try {
				Files.deleteIfExists(Paths.get(pluginsDir, fileName));
			} catch (IOException e) {
				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR, "Unable to delete plugin file.");
			}
		});
		IntegrationDetailsProperties.FILE_ID.getValue(details).map(String::valueOf).ifPresent(dataStore::delete);
	}
}
