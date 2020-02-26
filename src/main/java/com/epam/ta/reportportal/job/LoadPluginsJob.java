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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.configs.Conditions;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.job.service.PluginLoaderService;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Conditional(Conditions.NotTestCondition.class)
@Service
public class LoadPluginsJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoadPluginsJob.class);

	private final IntegrationTypeRepository integrationTypeRepository;

	private final PluginLoaderService pluginLoaderService;

	private final String pluginsRootPath;

	private final Pf4jPluginBox pluginBox;

	private final DataStore dataStore;

	@Autowired
	public LoadPluginsJob(@Value("${rp.plugins.path}") String pluginsRootPath, IntegrationTypeRepository integrationTypeRepository,
			PluginLoaderService pluginLoaderService, Pf4jPluginBox pf4jPluginBox, DataStore dataStore) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginLoaderService = pluginLoaderService;
		this.pluginBox = pf4jPluginBox;
		this.dataStore = dataStore;
		this.pluginsRootPath = pluginsRootPath;
	}

	@Scheduled(fixedDelayString = "${com.ta.reportportal.job.load.plugins.cron}")
	public void execute() {
		List<PluginInfo> notLoadedPlugins = pluginLoaderService.getNotLoadedPluginsInfo();

		notLoadedPlugins.forEach(pluginInfo -> {
			try (InputStream inputStream = dataStore.load(pluginInfo.getFileId())) {
				LOGGER.debug("Plugin loading has started...");

				if (!Files.exists(Paths.get(pluginsRootPath, pluginInfo.getFileName()))) {
					LOGGER.debug("Copying plugin file...");
					FileUtils.copyToFile(inputStream, new File(pluginsRootPath, pluginInfo.getFileName()));
				}

				if (pluginInfo.isEnabled()) {
					IntegrationType integrationType = integrationTypeRepository.findByName(pluginInfo.getId())
							.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginInfo.getId()));

					boolean isLoaded = pluginBox.loadPlugin(integrationType.getName(), integrationType.getDetails());

					if (isLoaded) {
						LOGGER.debug(Suppliers.formattedSupplier("Plugin - '{}' has been successfully started.", integrationType.getName())
								.get());
					} else {
						LOGGER.error(Suppliers.formattedSupplier("Plugin - '{}' has not been started.", integrationType.getName()).get());
					}
				}

			} catch (IOException ex) {
				LOGGER.error("Error has occurred during plugin copying from the Data store", ex);
				//do nothing
			}
		});

	}

}
