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
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.job.service.PluginLoaderService;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Conditional(Conditions.NotTestCondition.class)
@Service
public class CleanOutdatedPluginsJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(CleanOutdatedPluginsJob.class);

	private final String pluginsTempPath;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final Pf4jPluginBox pluginBox;

	private final PluginLoaderService pluginLoaderService;

	@Autowired
	public CleanOutdatedPluginsJob(@Value("${rp.plugins.temp.path}") String pluginsTempPath,
			IntegrationTypeRepository integrationTypeRepository, Pf4jPluginBox pf4jPluginBox, PluginLoaderService pluginLoaderService) {
		this.pluginsTempPath = pluginsTempPath;
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginBox = pf4jPluginBox;
		this.pluginLoaderService = pluginLoaderService;
	}

	@Scheduled(fixedDelayString = "${com.ta.reportportal.job.clean.outdated.plugins.cron}")
	public void execute() {

		removeTemporaryPlugins();

		List<IntegrationType> integrationTypes = integrationTypeRepository.findAll();
		integrationTypes.stream()
				.filter(it -> it.getDetails() == null || it.getDetails().getDetails() == null)
				.forEach(pluginLoaderService::checkAndDeleteIntegrationType);

		unloadRemovedPlugins(integrationTypes);
		unloadDisabledPlugins(integrationTypes);
	}

	private void removeTemporaryPlugins() {
		Path tempPluginsPath = Paths.get(pluginsTempPath);

		LOGGER.debug("Searching for temporary plugins...");
		try (Stream<Path> pathStream = Files.walk(tempPluginsPath)) {
			pathStream.filter(Files::isRegularFile).forEach(path -> ofNullable(path.getFileName()).ifPresent(fileName -> {
				if (!pluginBox.isInUploadingState(fileName.toString())) {
					try {
						Files.deleteIfExists(path);
						LOGGER.debug(Suppliers.formattedSupplier("Temporary plugin - '{}' has been removed", path).get());
					} catch (IOException e) {
						LOGGER.error("Error has occurred during temporary plugin file removing", e);
					}
				} else {
					LOGGER.debug(Suppliers.formattedSupplier("Uploading of the plugin - '{}' is still in progress.", path).get());
				}
			}));
		} catch (IOException e) {
			LOGGER.error("Error has occurred during temporary plugins folder listing", e);
		}
		LOGGER.debug("Temporary plugins removing has finished...");
	}

	private void unloadRemovedPlugins(List<IntegrationType> integrationTypes) {

		LOGGER.debug("Unloading of removed plugins...");

		List<String> pluginIds = pluginBox.getPlugins().stream().map(Plugin::getId).collect(Collectors.toList());

		pluginIds.removeAll(integrationTypes.stream().map(IntegrationType::getName).collect(Collectors.toList()));

		pluginIds.forEach(pluginId -> pluginBox.getPluginById(pluginId).ifPresent(plugin -> {

			if (!isPluginStillBeingUploaded(plugin)) {
				if (!pluginBox.deletePlugin(plugin)) {
					LOGGER.error("Error has occurred during plugin file removing from the plugins directory");
				}
			}
		}));

		LOGGER.debug("Unloading of removed plugins has finished...");
	}

	private boolean isPluginStillBeingUploaded(@NotNull PluginWrapper pluginWrapper) {
		return pluginBox.isInUploadingState(pluginWrapper.getPluginPath().getFileName().toString());
	}

	private void unloadDisabledPlugins(List<IntegrationType> integrationTypes) {

		List<IntegrationType> disabledPlugins = integrationTypes.stream().filter(it -> !it.isEnabled()).collect(Collectors.toList());

		disabledPlugins.forEach(dp -> pluginBox.getPluginById(dp.getName()).ifPresent(plugin -> {
			if (pluginBox.unloadPlugin(dp)) {
				LOGGER.debug(Suppliers.formattedSupplier("Plugin - '{}' has been successfully unloaded.", plugin.getPluginId()).get());
			} else {
				LOGGER.error(Suppliers.formattedSupplier("Error during unloading the plugin with id = '{}'.", plugin.getPluginId()).get());
			}
		}));
	}
}
