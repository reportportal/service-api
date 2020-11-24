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

import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.entity.plugin.PluginFileExtension;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationTypeBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
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

	private static final long MAXIMUM_UPLOADED_PLUGINS = 50;
	private static final long PLUGIN_LIVE_TIME = 2;

	private final String pluginsDir;
	private final String pluginsTempDir;
	private final String resourcesDir;

	private final Cache<String, Path> uploadingPlugins;

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
		this.uploadingPlugins = CacheBuilder.newBuilder()
				.maximumSize(MAXIMUM_UPLOADED_PLUGINS)
				.expireAfterWrite(PLUGIN_LIVE_TIME, TimeUnit.MINUTES)
				.build();
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

	@Override
	public boolean isInUploadingState(String fileName) {
		return uploadingPlugins.asMap().containsKey(fileName);
	}

	@Override
	public IntegrationType uploadPlugin(final String uploadedPluginName, final InputStream fileStream) {
		PluginInfo newPluginInfo = resolvePluginInfo(uploadedPluginName, fileStream);
		IntegrationTypeDetails pluginDetails = pluginLoader.resolvePluginDetails(newPluginInfo);

		Optional<PluginWrapper> previousPlugin = getPluginById(newPluginInfo.getId());
		previousPlugin.ifPresent(this::unloadPreviousPlugin);

		return ofNullable(pluginManager.loadPlugin(Paths.get(pluginsTempDir, uploadedPluginName))).map(pluginId -> {
			IntegrationTypeDetails newPluginDetails = copyPlugin(newPluginInfo, pluginDetails, uploadedPluginName);
			try {
				IntegrationType newIntegrationType = startUpPlugin(newPluginDetails);
				applicationEventPublisher.publishEvent(new PluginEvent(newIntegrationType.getName(), LOAD_KEY));
				previousPlugin.ifPresent(this::deletePreviousPlugin);
				deleteTempPlugin(uploadedPluginName);
				return newIntegrationType;
			} catch (Exception ex) {
				previousPlugin.ifPresent(p -> loadPreviousPlugin(p, newPluginDetails));
				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, ex.getMessage());
			}
		}).orElseThrow(() -> {
			previousPlugin.ifPresent(p -> loadPreviousPlugin(p, pluginDetails));
			deleteTempPlugin(uploadedPluginName);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Failed to load new plugin from file = '{}'", uploadedPluginName).get()
			);
		});
	}

	/**
	 * Uploads the plugin file to the temp directory and extracts it's info.
	 * Presence of the plugin version is mandatory
	 *
	 * @param fileName   Plugin file name to upload
	 * @param fileStream {@link InputStream} of the plugin file
	 * @return {@link PluginInfo}
	 */
	private PluginInfo resolvePluginInfo(final String fileName, InputStream fileStream) {
		Path tempPluginPath = uploadTempPlugin(fileName, fileStream);

		try {
			PluginInfo newPluginInfo = pluginLoader.extractPluginInfo(tempPluginPath);
			BusinessRule.expect(validatePluginMetaInfo(newPluginInfo), equalTo(Boolean.TRUE))
					.verify(ErrorType.PLUGIN_UPLOAD_ERROR, "Plugin version should be specified.");
			return newPluginInfo;
		} catch (PluginException e) {
			removeUploadingPlugin(fileName);
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
		}
	}

	/**
	 * Upload plugin file to the temporary plugins directory.
	 *
	 * @param fileName   Plugin file name to upload
	 * @param fileStream {@link InputStream} of the plugin file
	 * @return {@link Path} to the temporary uploaded  plugin file
	 */
	private Path uploadTempPlugin(String fileName, InputStream fileStream) {
		Path pluginsTempDirPath = Paths.get(pluginsTempDir);
		createTempPluginsFolderIfNotExists(pluginsTempDirPath);

		validateFileExtension(fileName);

		try {
			Path pluginPath = Paths.get(pluginsTempDir, fileName);
			addUploadingPlugin(fileName, pluginPath);
			pluginLoader.savePlugin(pluginPath, fileStream);
			return pluginPath;
		} catch (IOException e) {
			removeUploadingPlugin(fileName);
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to copy the new plugin file with name = '{}' to the temp directory", fileName).get()
			);
		}
	}

	/**
	 * Create a new temporary directory for plugins if not exists
	 *
	 * @param path Path of the new directory
	 */
	private void createTempPluginsFolderIfNotExists(Path path) {
		if (!Files.isDirectory(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to create directory = '{}'", path).get()
				);
			}
		}
	}

	/**
	 * Resolve and validate file type.
	 * Allowed values: {@link PluginFileExtension#values()}
	 *
	 * @param fileName uploaded plugin file name
	 */
	private void validateFileExtension(String fileName) {
		String resolvedExtension = FilenameUtils.getExtension(fileName);
		Optional<PluginFileExtension> byExtension = PluginFileExtension.findByExtension("." + resolvedExtension);
		BusinessRule.expect(byExtension, Optional::isPresent)
				.verify(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unsupported plugin file extension = '{}'", resolvedExtension).get()
				);
	}

	/**
	 * Add plugin file name to the uploading plugins holder
	 *
	 * @param fileName Name of the plugin file to put to the {@link com.epam.ta.reportportal.plugin.Pf4jPluginManager#uploadingPlugins}
	 * @param path     Full path to the plugin file
	 * @see com.epam.ta.reportportal.plugin.Pf4jPluginManager
	 */
	private void addUploadingPlugin(String fileName, Path path) {
		uploadingPlugins.put(fileName, path);
	}

	private boolean validatePluginMetaInfo(PluginInfo newPluginInfo) {
		return ofNullable(newPluginInfo.getVersion()).map(StringUtils::isNotBlank).orElse(Boolean.FALSE);
	}

	private void unloadPreviousPlugin(PluginWrapper pluginWrapper) {
		destroyDependency(pluginWrapper.getPluginId());
		if (!pluginManager.unloadPlugin(pluginWrapper.getPluginId())) {
			throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
					Suppliers.formattedSupplier("Failed to stop old plugin with id = '{}'", pluginWrapper.getPluginId()).get()
			);
		}
	}

	private void destroyDependency(String name) {
		AbstractAutowireCapableBeanFactory beanFactory = (AbstractAutowireCapableBeanFactory) this.autowireCapableBeanFactory;
		if (beanFactory.containsSingleton(name)) {
			beanFactory.destroySingleton(name);
		}
	}

	/**
	 * Validates the new plugin in the temporary plugins' directory, uploads it to the root plugins' directory and to the {@link DataStore}
	 *
	 * @param newPluginInfo      Resolved {@link PluginInfo} of the new plugin
	 * @param uploadedPluginName Original plugin file name
	 * @param pluginDetails      {@link IntegrationTypeDetails} with the info about the new plugin
	 * @return updated {@link IntegrationTypeDetails}
	 */
	private IntegrationTypeDetails copyPlugin(PluginInfo newPluginInfo, IntegrationTypeDetails pluginDetails, String uploadedPluginName) {
		String newPluginId = newPluginInfo.getId();
		startUpPlugin(newPluginId);
		validateNewPluginExtensionClasses(newPluginId, uploadedPluginName);
		pluginManager.unloadPlugin(newPluginId);

		final String newPluginFileName = generatePluginFileName(newPluginInfo, uploadedPluginName);
		IntegrationTypeProperties.FILE_NAME.setValue(pluginDetails, newPluginFileName);

		final String fileId = savePlugin(uploadedPluginName, newPluginFileName);
		IntegrationTypeProperties.FILE_ID.setValue(pluginDetails, fileId);

		copyPluginToRootDirectory(newPluginId, fileId, newPluginFileName);
		removeUploadingPlugin(uploadedPluginName);

		return pluginDetails;
	}

	/**
	 * Validates plugin's extension class/classes and reloads the previous plugin if it is present and the validation failed
	 *
	 * @param newPluginId       Id of the new plugin
	 * @param newPluginFileName New plugin file name
	 * @see PluginLoader#validatePluginExtensionClasses(PluginWrapper))
	 */
	private void validateNewPluginExtensionClasses(String newPluginId, String newPluginFileName) {
		PluginWrapper newPlugin = getPluginById(newPluginId).orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Plugin with id = '{}' has not been found.", newPluginId).get()
		));
		if (!pluginLoader.validatePluginExtensionClasses(newPlugin)) {
			pluginManager.unloadPlugin(newPluginId);
			deleteTempPlugin(newPluginFileName);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("New plugin with id = '{}' doesn't have mandatory extension classes.", newPluginId).get()
			);

		}
	}

	private void deleteTempPlugin(String tempPluginFileName) {
		try {
			pluginLoader.deleteTempPlugin(pluginsTempDir, tempPluginFileName);
		} catch (IOException e) {
			//error during temp plugin is not crucial, temp files cleaning will be delegated to the plugins cleaning job
			LOGGER.error("Error during temp plugin file removing: '{}'", e.getMessage());
		} finally {
			removeUploadingPlugin(tempPluginFileName);
		}
	}

	private String generatePluginFileName(PluginInfo pluginInfo, final String originalFileName) {
		return pluginInfo.getId() + "-" + pluginInfo.getVersion() + "." + FilenameUtils.getExtension(originalFileName);
	}

	/**
	 * Saves plugin file to the instance of the configured {@link DataStore}
	 *
	 * @param uploadedPluginName Original plugin file name
	 * @param newPluginFileName  New plugin file name
	 * @return File id
	 */
	private String savePlugin(final String uploadedPluginName, final String newPluginFileName) {
		try (InputStream fileStream = FileUtils.openInputStream(FileUtils.getFile(pluginsTempDir, uploadedPluginName))) {
			return pluginLoader.saveToDataStore(newPluginFileName, fileStream);
		} catch (Exception e) {
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to upload new plugin file = '{}' to the data store", uploadedPluginName).get()
			);
		}
	}

	private void copyPluginToRootDirectory(final String newPluginId, final String fileId, final String newPluginFileName) {
		try {
			pluginLoader.copyFromDataStore(fileId, Paths.get(pluginsDir, newPluginFileName), Paths.get(resourcesDir, newPluginId));
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to copy new plugin file = '{}' from the data store to the root directory",
							newPluginFileName
					).get()
			);
		}
	}

	/**
	 * Remove plugin file name from the uploading plugins holder
	 *
	 * @param fileName Name of the plugin file to remove from the {@link com.epam.ta.reportportal.plugin.Pf4jPluginManager#uploadingPlugins}
	 * @see com.epam.ta.reportportal.plugin.Pf4jPluginManager
	 */
	private void removeUploadingPlugin(String fileName) {
		uploadingPlugins.invalidate(fileName);
	}

	/**
	 * Starts the new plugin and saves it's info as {@link IntegrationType} object in the database
	 *
	 * @param pluginDetails {@link IntegrationTypeDetails} with the info about the new plugin
	 * @return {@link IntegrationType} object with the updated info about the new plugin
	 */
	private IntegrationType startUpPlugin(final IntegrationTypeDetails pluginDetails) {

		String newPluginFileName = IntegrationTypeProperties.FILE_NAME.getValue(pluginDetails.getDetails())
				.map(String::valueOf)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, "Unable to resolve 'fileName' property"));

		return ofNullable(pluginManager.loadPlugin(Paths.get(pluginsDir, newPluginFileName))).map(newLoadedPluginId -> {
			startUpPlugin(newLoadedPluginId);

			Optional<IntegrationType> oldIntegrationType = integrationTypeRepository.findByName(newLoadedPluginId);
			oldIntegrationType.ifPresent(it -> IntegrationTypeProperties.FILE_ID.getValue(pluginDetails.getDetails())
					.map(String::valueOf)
					.ifPresent(fileId -> deletePreviousPluginFile(it, fileId)));

			IntegrationTypeBuilder integrationTypeBuilder = oldIntegrationType.map(IntegrationTypeBuilder::new)
					.orElseGet(IntegrationTypeBuilder::new);
			integrationTypeBuilder.setName(newLoadedPluginId).setIntegrationGroup(IntegrationGroupEnum.OTHER);

			Optional<ReportPortalExtensionPoint> instance = getInstance(newLoadedPluginId, ReportPortalExtensionPoint.class);

			instance.ifPresent(extensionPoint -> {
				pluginDetails.getDetails().putAll(extensionPoint.getPluginParams());
				pluginDetails.getDetails()
						.put(IntegrationTypeProperties.RESOURCES_DIRECTORY.getAttribute(),
								Paths.get(resourcesDir, newLoadedPluginId).toString()
						);
				integrationTypeBuilder.setDetails(pluginDetails);
				integrationTypeBuilder.setIntegrationGroup(IntegrationGroupEnum.valueOf(extensionPoint.getIntegrationGroup().name()));
			});

			integrationTypeBuilder.setEnabled(true);
			return integrationTypeRepository.save(integrationTypeBuilder.get());

		})
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Error during loading the plugin file = '{}'", newPluginFileName).get()
				));

	}

	private void deletePreviousPluginFile(IntegrationType oldIntegrationType, String newFileId) {
		try {
			ofNullable(oldIntegrationType.getDetails()).flatMap(details -> ofNullable(details.getDetails()))
					.flatMap(IntegrationTypeProperties.FILE_ID::getValue)
					.map(String::valueOf)
					.ifPresent(oldFileId -> {
						if (!oldFileId.equals(newFileId)) {
							pluginLoader.deleteFromDataStore(oldFileId);
						}
					});
		} catch (Exception ex) {
			LOGGER.error("Error during removing old plugin file from the Data store: {}", ex.getMessage());
		}
	}

	private void deletePreviousPlugin(PluginWrapper previousPlugin) {
		try {
			Files.deleteIfExists(previousPlugin.getPluginPath());
		} catch (IOException e) {
			LOGGER.error("Unable to delete the old plugin file with id = '{}'", previousPlugin.getPluginId());
		}
	}

	/**
	 * Load and start up the previous plugin
	 *
	 * @param previousPlugin   {@link PluginWrapper} with mandatory data for plugin loading: {@link PluginWrapper#getPluginPath()}
	 * @param newPluginDetails {@link IntegrationTypeDetails} of the plugin which uploading ended up with an error
	 * @return {@link PluginState}
	 */
	private PluginState loadPreviousPlugin(PluginWrapper previousPlugin, IntegrationTypeDetails newPluginDetails) {
		if (previousPlugin.getPluginState() == PluginState.STARTED) {
			return previousPlugin.getPluginState();
		}

		IntegrationTypeProperties.FILE_ID.getValue(newPluginDetails.getDetails()).map(String::valueOf).ifPresent(fileId -> {
			try {
				pluginLoader.deleteFromDataStore(fileId);
			} catch (Exception e) {
				LOGGER.error("Unable to delete new plugin file from the DataStore: '{}'", e.getMessage());
			}
		});

		PluginState pluginState = ofNullable(pluginManager.getPlugin(previousPlugin.getPluginId())).map(loadedPlugin -> {
			if (previousPlugin.getDescriptor().getVersion().equals(loadedPlugin.getDescriptor().getVersion())) {
				return loadedPlugin.getPluginState();
			} else {
				pluginManager.deletePlugin(loadedPlugin.getPluginId());
				deletePluginResources(String.valueOf(Paths.get(resourcesDir, loadedPlugin.getPluginId())));
				return PluginState.DISABLED;
			}
		}).orElse(PluginState.DISABLED);

		if (pluginState != PluginState.STARTED) {
			try {
				Path oldPluginPath = previousPlugin.getPluginPath();
				PluginInfo oldPluginInfo = pluginLoader.extractPluginInfo(oldPluginPath);
				String oldPluginFileName = generatePluginFileName(oldPluginInfo, oldPluginPath.toFile().getName());
				try (InputStream fileStream = Files.newInputStream(oldPluginPath)) {
					pluginLoader.saveToDataStore(oldPluginFileName, fileStream);
				} catch (Exception e) {
					throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
							Suppliers.formattedSupplier("Unable to upload old plugin file = '{}' to the data store", oldPluginFileName)
									.get()
					);
				}
				copyPluginResources(oldPluginPath, previousPlugin.getPluginId());
				return startUpPlugin(ofNullable(pluginManager.loadPlugin(oldPluginPath)).orElseThrow(() -> new ReportPortalException(
						ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to reload previousPlugin with id = '{}': '{}'", previousPlugin.getPluginId())
								.get()
				)));
			} catch (PluginException e) {
				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to reload previousPlugin with id = '{}': '{}'",
								previousPlugin.getPluginId(),
								e.getMessage()
						).get()
				);
			}
		}

		return PluginState.STARTED;

	}
}
