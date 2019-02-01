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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginInfo;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.filesystem.DataStore;
import org.apache.commons.io.FileUtils;
import org.pf4j.PluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LoadPluginsJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoadPluginsJob.class);

	private final IntegrationTypeRepository integrationTypeRepository;

	private final PluginLoaderService pluginLoaderService;

	private final String pluginsRootPath;

	private final PluginBox pluginBox;

	private final DataStore dataStore;

	@Autowired
	public LoadPluginsJob(@Value("${rp.plugins.path}") String pluginsRootPath, IntegrationTypeRepository integrationTypeRepository,
			PluginLoaderService pluginLoaderService, PluginBox pf4jPluginBox, DataStore dataStore) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginLoaderService = pluginLoaderService;
		this.pluginBox = pf4jPluginBox;
		this.dataStore = dataStore;
		this.pluginsRootPath = pluginsRootPath;
	}

	@Scheduled(fixedDelayString = "${com.ta.reportportal.job.load.plugins.cron}")
	public void execute() {

		List<IntegrationType> integrationTypes = integrationTypeRepository.findAll();

		List<PluginInfo> notLoadedPlugins = pluginLoaderService.getNotLoadedPluginsInfo(integrationTypes);

		notLoadedPlugins.forEach(pluginInfo -> ofNullable(dataStore.load(pluginInfo.getId())).ifPresent(inputStream -> {
			try {
				FileUtils.copyToFile(inputStream, new File(pluginsRootPath, pluginInfo.getFileName()));

				LOGGER.info("Plugin loading has started...");

				String pluginId = pluginBox.loadPlugin(Paths.get(pluginsRootPath, pluginInfo.getFileName()));

				ofNullable(pluginId).ifPresent(id -> {
					LOGGER.info(Suppliers.formattedSupplier("Plugin - '{}' has been successfully loaded.", id).get());

					PluginState pluginState = pluginBox.startUpPlugin(pluginId);

					if (pluginState == PluginState.STARTED) {
						LOGGER.info(Suppliers.formattedSupplier("Plugin - '{}' has been successfully started.", id).get());
					} else {
						LOGGER.debug(Suppliers.formattedSupplier("Plugin - '{}' has not been started.", id).get());
					}
				});

			} catch (IOException ex) {
				LOGGER.debug("Error has occurred during plugin copying from the Data store", ex);
				//do nothing
			}
		}));

	}

}
