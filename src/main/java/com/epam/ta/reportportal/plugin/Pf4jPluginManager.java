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

package com.epam.ta.reportportal.plugin;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FileUtils;
import org.pf4j.PluginException;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * {@link Pf4jPluginManager#uploadingPlugins} Holder for the plugin cleaning job: {@link com.epam.ta.reportportal.job.CleanOutdatedPluginsJob}
 * to prevent the removing of the plugins that are still being processed within the database transaction with
 * {@link com.epam.ta.reportportal.entity.integration.IntegrationType} in uncommitted state
 */
public class Pf4jPluginManager implements Pf4jPluginBox {

	public static final Logger LOGGER = LoggerFactory.getLogger(Pf4jPluginManager.class);

	public static final String LOAD_KEY = "load";
	public static final String UNLOAD_KEY = "unload";

	private final String pluginsDir;
	private final String pluginsTempDir;
	private final String resourcesDir;

	private final PluginLoader pluginLoader;
	private final IntegrationTypeRepository integrationTypeRepository;

	private final PluginManager pluginManager;
	private final AutowireCapableBeanFactory autowireCapableBeanFactory;

	private final ApplicationEventPublisher applicationEventPublisher;

	public Pf4jPluginManager(String pluginsDir, String pluginsTempPath, String resourcesDir, PluginLoader pluginLoader,
			IntegrationTypeRepository integrationTypeRepository, PluginManager pluginManager,
			AutowireCapableBeanFactory autowireCapableBeanFactory, ApplicationEventPublisher applicationEventPublisher) throws IOException {
		this.pluginsDir = pluginsDir;
		Files.createDirectories(Paths.get(this.pluginsDir));
		this.resourcesDir = resourcesDir;
		Files.createDirectories(Paths.get(this.resourcesDir));
		this.pluginsTempDir = pluginsTempPath;
		Files.createDirectories(Paths.get(this.pluginsTempDir));
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
		this.applicationEventPublisher = applicationEventPublisher;
		this.pluginLoader = pluginLoader;
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginManager = pluginManager;
	}

	@Override
	public List<Plugin> getPlugins() {
		return this.pluginManager.getPlugins()
				.stream()
				.flatMap(plugin -> pluginManager.getExtensionClasses(plugin.getPluginId())
						.stream()
						.map(ExtensionPoint::findByExtension)
						.filter(Optional::isPresent)
						.map(it -> new Plugin(plugin.getPluginId(), it.get())))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Plugin> getPlugin(String type) {
		return getPlugins().stream().filter(p -> p.getType().name().equalsIgnoreCase(type)).findAny();
	}

	@Override
	public <T> Optional<T> getInstance(String name, Class<T> extension) {
		return pluginManager.getExtensions(extension, name).stream().findFirst();
	}

	@Override
	public <T> Optional<T> getInstance(Class<T> extension) {
		return pluginManager.getExtensions(extension).stream().findFirst();
	}

	@Override
	public void startUp() {
		// load and start all enabled plugins of application
		integrationTypeRepository.findAll()
				.stream()
				.filter(IntegrationType::isEnabled)
				.forEach(integrationType -> ofNullable(integrationType.getDetails()).ifPresent(integrationTypeDetails -> {
					try {
						loadPlugin(integrationType.getName(), integrationTypeDetails);
					} catch (Exception ex) {
						LOGGER.error("Unable to load plugin '{}'", integrationType.getName());
					}
				}));

	}

	@Override
	public void shutDown() {
		// stop and unload all plugins
		pluginManager.stopPlugins();
		pluginManager.getPlugins().forEach(p -> pluginManager.unloadPlugin(p.getPluginId()));
	}

	@Override
	public PluginState startUpPlugin(String pluginId) {

		PluginWrapper pluginWrapper = ofNullable(pluginManager.getPlugin(pluginId)).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Plugin not found: " + pluginId
		));

		return pluginManager.startPlugin(pluginWrapper.getPluginId());
	}

	@Override
	public PluginState startUpPlugin(Path pluginPath) {
		return ofNullable(pluginManager.loadPlugin(pluginPath)).map(this::startUpPlugin)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Error during loading the plugin file = '{}'", pluginPath.getFileName().toString())
								.get()
				));
	}

	@Override
	public boolean loadPlugin(String pluginId, IntegrationTypeDetails integrationTypeDetails) {
		return ofNullable(integrationTypeDetails.getDetails()).map(details -> {
			String fileName = IntegrationTypeProperties.FILE_NAME.getValue(details)
					.map(String::valueOf)
					.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
							Suppliers.formattedSupplier("'File name' property of the plugin - '{}' is not specified", pluginId).get()
					));

			Path pluginPath = Paths.get(pluginsDir, fileName);
			if (Files.notExists(pluginPath)) {
				String fileId = IntegrationTypeProperties.FILE_ID.getValue(details)
						.map(String::valueOf)
						.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
								Suppliers.formattedSupplier("'File id' property of the plugin - '{}' is not specified", pluginId).get()
						));
				try {
					pluginLoader.copyFromDataStore(fileId, pluginPath, Paths.get(resourcesDir, pluginId));
				} catch (IOException e) {
					throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
							Suppliers.formattedSupplier("Unable to load plugin - '{}' from the data store", pluginId).get()
					);
				}
			} else {
				copyPluginResources(pluginPath, pluginId);
			}

			return ofNullable(pluginManager.loadPlugin(pluginPath)).map(id -> {
				if (PluginState.STARTED == pluginManager.startPlugin(pluginId)) {
					Optional<org.pf4j.ExtensionPoint> extensionPoint = this.getInstance(pluginId, org.pf4j.ExtensionPoint.class);
					extensionPoint.ifPresent(extension -> LOGGER.info(Suppliers.formattedSupplier("Plugin - '{}' initialized.", pluginId)
							.get()));
					applicationEventPublisher.publishEvent(new PluginEvent(pluginId, LOAD_KEY));
					return true;
				} else {
					return false;
				}
			}).orElse(Boolean.FALSE);
		}).orElse(Boolean.FALSE);

	}

	private void copyPluginResources(Path pluginPath, String pluginId) {
		try {
			pluginLoader.copyPluginResource(pluginPath, Paths.get(resourcesDir, pluginId));
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to load resources of the - '{}' plugin", pluginId).get()
			);
		}
	}

	@Override
	public boolean unloadPlugin(IntegrationType integrationType) {
		applicationEventPublisher.publishEvent(new PluginEvent(integrationType.getName(), UNLOAD_KEY));
		destroyDependency(integrationType.getName());
		return pluginManager.unloadPlugin(integrationType.getName());
	}

	@Override
	public boolean unloadPlugin(PluginWrapper pluginWrapper) {
		applicationEventPublisher.publishEvent(new PluginEvent(pluginWrapper.getPluginId(), UNLOAD_KEY));
		destroyDependency(pluginWrapper.getPluginId());
		return pluginManager.unloadPlugin(pluginWrapper.getPluginId());
	}

	@Override
	public Optional<PluginWrapper> unloadPlugin(String id) throws PluginException {
		Optional<PluginWrapper> previousPlugin = this.getPluginById(id);
		final Boolean unloaded = previousPlugin.map(this::unloadPlugin).orElse(Boolean.TRUE);
		if(!unloaded) {
			throw new PluginException(
					Suppliers.formattedSupplier("Failed to unload plugin with id = '{}'", id).get()
			);
		}
		return previousPlugin;
	}

	@Override
	public boolean deletePlugin(String pluginId) {
		return integrationTypeRepository.findByName(pluginId).map(this::deletePlugin).orElse(Boolean.TRUE);
	}

	@Override
	public boolean deletePlugin(PluginWrapper pluginWrapper) {
		return integrationTypeRepository.findByName(pluginWrapper.getPluginId()).map(this::deletePlugin).orElseGet(() -> {
			applicationEventPublisher.publishEvent(new PluginEvent(pluginWrapper.getPluginId(), UNLOAD_KEY));
			deletePluginResources(Paths.get(resourcesDir, pluginWrapper.getPluginId()).toString());
			destroyDependency(pluginWrapper.getPluginId());
			return pluginManager.deletePlugin(pluginWrapper.getPluginId());
		});
	}

	private boolean deletePlugin(IntegrationType integrationType) {
		Optional<Map<String, Object>> pluginData = ofNullable(integrationType.getDetails()).map(IntegrationTypeDetails::getDetails);
		pluginData.ifPresent(this::deletePluginResources);

		applicationEventPublisher.publishEvent(new PluginEvent(integrationType.getName(), UNLOAD_KEY));

		boolean pluginRemoved = ofNullable(pluginManager.getPlugin(integrationType.getName())).map(pluginWrapper -> {
			destroyDependency(pluginWrapper.getPluginId());
			if (integrationType.isEnabled()) {
				return pluginManager.deletePlugin(integrationType.getName());
			}
			return true;
		}).orElse(Boolean.TRUE);

		boolean pluginFileRemoved = pluginData.map(this::deletePluginFile).orElse(Boolean.TRUE);

		return pluginRemoved && pluginFileRemoved;
	}

	private void deletePluginResources(Map<String, Object> details) {
		IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(details).map(String::valueOf).ifPresent(this::deletePluginResources);
		IntegrationTypeProperties.FILE_ID.getValue(details).map(String::valueOf).ifPresent(pluginLoader::deleteFromDataStore);
	}

	private void deletePluginResources(String resourcesDir) {
		try {
			FileUtils.deleteDirectory(FileUtils.getFile(resourcesDir));
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR, "Unable to delete plugin resources.");
		}
	}

	private boolean deletePluginFile(Map<String, Object> details) {
		return IntegrationTypeProperties.FILE_NAME.getValue(details)
				.map(String::valueOf)
				.map(fileName -> Paths.get(pluginsDir, fileName))
				.map(path -> {
					try {
						if (Files.exists(path)) {
							return Files.deleteIfExists(path);
						} else {
							return true;
						}
					} catch (IOException e) {
						throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
								"Error during plugin file removing from the filesystem: " + e.getMessage()
						);
					}

				})
				.orElse(Boolean.TRUE);
	}

	@Override
	public Optional<PluginWrapper> getPluginById(String id) {
		return ofNullable(pluginManager.getPlugin(id));
	}

	private void destroyDependency(String name) {
		AbstractAutowireCapableBeanFactory beanFactory = (AbstractAutowireCapableBeanFactory) this.autowireCapableBeanFactory;
		if (beanFactory.containsSingleton(name)) {
			beanFactory.destroySingleton(name);
		}
	}

	/**
	 * Load and start up the previous plugin
	 *
	 * @param pluginId               {@link PluginWrapper} with mandatory data for plugin loading: {@link PluginWrapper#getPluginPath()}
	 * @param previousPluginPathInfo {@link PluginPathInfo} of the plugin which uploading ended up with an error
	 */
	@Override
	public void loadPreviousPlugin(String pluginId, PluginPathInfo previousPluginPathInfo) {
		ofNullable(pluginManager.getPlugin(pluginId)).map(PluginWrapper::getPluginId).ifPresent(this::deletePlugin);
		ofNullable(pluginManager.loadPlugin(previousPluginPathInfo.getPluginPath())).ifPresentOrElse(this::startUpPlugin, () -> {
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to reload previousPlugin with id = '{}'", pluginId).get()
			);
		});

	}
}
