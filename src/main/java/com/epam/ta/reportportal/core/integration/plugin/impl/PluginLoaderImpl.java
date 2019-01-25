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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.pf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginLoaderImpl implements PluginLoader {

	private final String pluginsRootPath;

	private final PluginBox pluginBox;

	private final PluginDescriptorFinder pluginDescriptorFinder;

	private final IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	public PluginLoaderImpl(@Value("${rp.plugins.path}") String pluginsRootPath, PluginBox pluginBox,
			PluginDescriptorFinder pluginDescriptorFinder, IntegrationTypeRepository integrationTypeRepository) {
		this.pluginsRootPath = pluginsRootPath;
		this.pluginBox = pluginBox;
		this.pluginDescriptorFinder = pluginDescriptorFinder;
		this.integrationTypeRepository = integrationTypeRepository;
	}

	@Override
	public String extractPluginId(Path pluginPath) {
		try {

			PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginPath);
			return pluginDescriptor.getPluginId();

		} catch (PluginException e) {

			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, e);
		}
	}

	@Override
	public PluginState reloadPlugin(PluginWrapper plugin) {

		return pluginBox.startUpPlugin(ofNullable(pluginBox.loadPlugin(plugin.getPluginPath())).orElseThrow(() -> new ReportPortalException(
				ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Unable to reload plugin with id = '{}", plugin.getPluginId()).get()
		)));
	}

	@Override
	public boolean validatePluginExtensionClasses(String pluginId) {

		PluginWrapper newPlugin = pluginBox.getPluginById(pluginId).orElseThrow(() -> new ReportPortalException(
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Plugin with id = {} has not been found.", pluginId).get()
		));

		return newPlugin.getPluginManager()
				.getExtensionClasses(pluginId)
				.stream()
				.map(ExtensionPoint::findByExtension)
				.anyMatch(Optional::isPresent);
	}

	@Override
	public Optional<PluginWrapper> retrieveOldPlugin(String newPluginId, String newPluginFileName) {

		Optional<PluginWrapper> oldPlugin = pluginBox.getPluginById(newPluginId);

		oldPlugin.ifPresent(p -> {

			if (!pluginBox.unloadPlugin(p.getPluginId())) {
				throw new ReportPortalException(
						ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Failed to stop old plugin with id = {}", p.getPluginId()).get()
				);
			}

			integrationTypeRepository.deleteByName(p.getPluginId());
		});

		validateNewPluginFile(oldPlugin, newPluginFileName);

		return oldPlugin;
	}

	@Override
	public void deleteOldPlugin(PluginWrapper oldPluginWrapper, String newPluginFileName) {

		if (!oldPluginWrapper.getPluginPath().equals(Paths.get(pluginsRootPath, newPluginFileName))) {
			try {
				Files.deleteIfExists(oldPluginWrapper.getPluginPath());
			} catch (IOException e) {

				reloadPlugin(oldPluginWrapper);

				throw new ReportPortalException(
						ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Unable to delete the old plugin file with id = {}", oldPluginWrapper.getPluginId())
								.get()
				);
			}
		}
	}

	private void validateNewPluginFile(Optional<PluginWrapper> oldPlugin, String newPluginFileName) {

		if (new File(pluginsRootPath, newPluginFileName).exists()) {

			if (!oldPlugin.isPresent() || !Paths.get(pluginsRootPath, newPluginFileName).equals(oldPlugin.get().getPluginPath())) {
				throw new ReportPortalException(
						ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to rewrite plugin file = '{}' with different plugin type", newPluginFileName)
								.get()
				);
			}
		}
	}
}
