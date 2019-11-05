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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.plugin.PluginFileExtension;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.AbstractIdleService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.pf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * {@link Pf4jPluginManager#uploadingPlugins} Holder for the plugin cleaning job: {@link com.epam.ta.reportportal.job.CleanOutdatedPluginsJob}
 * to prevent the removing of the plugins that are still being processed within the database transaction with
 * {@link com.epam.ta.reportportal.entity.integration.IntegrationType} in uncommitted state
 */
public class Pf4jPluginManager extends AbstractIdleService implements Pf4jPluginBox {

	public static final Logger LOGGER = LoggerFactory.getLogger(Pf4jPluginManager.class);

	private static final long MAXIMUM_UPLOADED_PLUGINS = 50;
	private static final long PLUGIN_LIVE_TIME = 2;

	private final String pluginsDir;
	private final String pluginsTempDir;

	private final Cache<String, Path> uploadingPlugins;

	private final PluginLoader pluginLoader;
	private final IntegrationTypeRepository integrationTypeRepository;
	private final Collection<PluginDescriptorFinder> pluginDescriptorFinders;
	private final ExtensionFactory extensionFactory;

	private org.pf4j.PluginManager pluginManager;

	public Pf4jPluginManager(String pluginsDir, String pluginsTempPath, PluginLoader pluginLoader,
			IntegrationTypeRepository integrationTypeRepository, Collection<PluginDescriptorFinder> pluginDescriptorFinders,
			ExtensionFactory extensionFactory) throws IOException {
		this.pluginsDir = pluginsDir;
		Files.createDirectories(Paths.get(this.pluginsDir));
		this.pluginsTempDir = pluginsTempPath;
		Files.createDirectories(Paths.get(this.pluginsTempDir));
		this.pluginLoader = pluginLoader;
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginDescriptorFinders = pluginDescriptorFinders;
		this.extensionFactory = extensionFactory;
		this.uploadingPlugins = CacheBuilder.newBuilder()
				.maximumSize(MAXIMUM_UPLOADED_PLUGINS)
				.expireAfterWrite(PLUGIN_LIVE_TIME, TimeUnit.MINUTES)
				.build();
		this.pluginManager = new Pf4jExtension(this.pluginsDir);
	}

	public void setPluginManager(PluginManager pluginManager) {
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
	public <T> Optional<T> getInstance(Class<T> extension) {
		return pluginManager.getExtensions(extension).stream().findFirst();
	}

	@Override
	public PluginState startUpPlugin(String pluginId) {

		PluginWrapper pluginWrapper = ofNullable(pluginManager.getPlugin(pluginId)).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Plugin not found: " + pluginId
		));

		return pluginManager.startPlugin(pluginWrapper.getPluginId());
	}

	@Override
	@Nullable
	public String loadPlugin(Path path) {
		return pluginManager.loadPlugin(path);
	}

	@Override
	public boolean unloadPlugin(String pluginId) {
		return pluginManager.unloadPlugin(pluginId);
	}

	@Override
	public boolean deletePlugin(String pluginId) {
		return pluginManager.deletePlugin(pluginId);
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
	public <T> Optional<T> getInstance(String name, Class<T> extension) {
		return pluginManager.getExtensions(extension, name).stream().findFirst();
	}

	@EventListener(ContextRefreshedEvent.class)
	public void onApplicationEvent(ContextRefreshedEvent event) {
		event.getApplicationContext().getBean(Pf4jPluginManager.class).checkStoredPlugins();
	}

	@Transactional
	public void checkStoredPlugins() {
		pluginManager.getPlugins().forEach(plugin -> {
			if (!integrationTypeRepository.findByName(plugin.getPluginId()).isPresent()) {
				IntegrationType integrationType = pluginLoader.retrieveIntegrationType(resolvePluginInfo(plugin.getPluginPath()));
				storeIntegrationType(plugin, integrationType);
			}
		});
	}

	@Override
	protected void startUp() {
		// start and load all plugins of application
		pluginManager.loadPlugins();
		pluginManager.startPlugins();

	}

	@Override
	protected void shutDown() {
		// stop and unload all plugins
		pluginManager.stopPlugins();
		pluginManager.getPlugins().forEach(p -> pluginManager.unloadPlugin(p.getPluginId()));
	}

	@Override
	public IntegrationType uploadPlugin(final String newPluginFileName, final InputStream fileStream) {
		PluginInfo newPluginInfo = resolvePluginInfo(newPluginFileName, fileStream);
		IntegrationType integrationType = pluginLoader.retrieveIntegrationType(newPluginInfo);

		Optional<PluginWrapper> previousPlugin = getPluginById(newPluginInfo.getId());
		validateNewPluginFile(previousPlugin, newPluginFileName);
		previousPlugin.ifPresent(this::unloadPlugin);

		String newPluginId = loadPlugin(Paths.get(pluginsTempDir, newPluginFileName));

		if (ofNullable(newPluginId).isPresent()) {
			IntegrationType newIntegrationType = startUpPlugin(newPluginId, previousPlugin, newPluginFileName, integrationType);
			deleteTempPlugin(newPluginFileName);
			return newIntegrationType;
		} else {
			previousPlugin.ifPresent(this::loadAndStartUpPlugin);
			deleteTempPlugin(newPluginFileName);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Failed to load new plugin from file = {}", newPluginFileName).get()
			);
		}
	}

	public class Pf4jExtension extends DefaultPluginManager {

		public Pf4jExtension(String pluginsRoot) {
			super(FileSystems.getDefault().getPath(pluginsRoot));
		}

		@Override
		protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {
			CompoundPluginDescriptorFinder compoundPluginDescriptorFinder = new CompoundPluginDescriptorFinder();
			Pf4jPluginManager.this.pluginDescriptorFinders.forEach(compoundPluginDescriptorFinder::add);
			return
					// Demo is using the Manifest file
					// PropertiesPluginDescriptorFinder is commented out just to avoid error log
					//.add(new PropertiesPluginDescriptorFinder())
					compoundPluginDescriptorFinder;
		}

		@Override
		protected ExtensionFactory createExtensionFactory() {
			return Pf4jPluginManager.this.extensionFactory;
		}

		@Override
		protected ExtensionFinder createExtensionFinder() {
			RpExtensionFinder extensionFinder = new RpExtensionFinder(this);
			addPluginStateListener(extensionFinder);
			return extensionFinder;
		}

		private class RpExtensionFinder extends DefaultExtensionFinder {

			private RpExtensionFinder(PluginManager pluginManager) {
				super(pluginManager);
				finders.clear();
				finders.add(new LegacyExtensionFinder(pluginManager) {
					@Override
					public Set<String> findClassNames(String pluginId) {
						return ofNullable(super.findClassNames(pluginId)).orElseGet(Collections::emptySet);
					}
				});
			}
		}
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

		Path pluginsTempPath = Paths.get(pluginsTempDir);

		createTempPluginsFolderIfNotExists(pluginsTempPath);
		validateFileExtension(fileName);
		uploadTempPlugin(fileName, fileStream);

		try {
			PluginInfo newPluginInfo = pluginLoader.extractPluginInfo(Paths.get(pluginsTempDir, fileName));
			validatePluginVersion(newPluginInfo, fileName);
			return newPluginInfo;
		} catch (PluginException e) {
			removeUploadingPlugin(fileName);
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
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
						Suppliers.formattedSupplier("Unable to create directory = {}", path).get()
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
		BusinessRule.expect(byExtension, Optional::isPresent).verify(ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Unsupported plugin file extension = {}", resolvedExtension).get()
		);
	}

	private void validatePluginVersion(PluginInfo newPluginInfo, String fileName) {
		if (!ofNullable(newPluginInfo.getVersion()).isPresent()) {
			removeUploadingPlugin(fileName);
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, "Plugin version should be specified.");
		}
	}

	/**
	 * @param previousPlugin    Already loaded plugin with the same id as the new one
	 * @param newPluginFileName New plugin file name
	 * @throws ReportPortalException When a file with the same name as new one is already exists in the directory
	 *                               and it's not a file of the previous plugin with the same id
	 */
	private void validateNewPluginFile(Optional<PluginWrapper> previousPlugin, String newPluginFileName) {
		if (new File(pluginsDir, newPluginFileName).exists()) {

			if (!previousPlugin.isPresent() || !Paths.get(pluginsDir, newPluginFileName).equals(previousPlugin.get().getPluginPath())) {
				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to rewrite plugin file = '{}' with different plugin type", newPluginFileName)
								.get()
				);
			}
		}
	}

	/**
	 * Upload plugin file to the temporary plugins directory.
	 *
	 * @param fileName   Plugin file name to upload
	 * @param fileStream {@link InputStream} of the plugin file
	 */
	private void uploadTempPlugin(String fileName, InputStream fileStream) {
		try {
			Path pluginPath = Paths.get(pluginsTempDir, fileName);
			addUploadingPlugin(fileName, pluginPath);
			pluginLoader.savePlugin(pluginPath, fileStream);
		} catch (IOException e) {
			removeUploadingPlugin(fileName);
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to copy the new plugin file with name = {} to the temp directory", fileName).get()
			);
		}
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

	/**
	 * Remove plugin file name from the uploading plugins holder
	 *
	 * @param fileName Name of the plugin file to remove from the {@link com.epam.ta.reportportal.plugin.Pf4jPluginManager#uploadingPlugins}
	 * @see com.epam.ta.reportportal.plugin.Pf4jPluginManager
	 */
	private void removeUploadingPlugin(String fileName) {
		uploadingPlugins.invalidate(fileName);
	}

	private void unloadPlugin(PluginWrapper pluginWrapper) {
		if (!unloadPlugin(pluginWrapper.getPluginId())) {
			throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
					Suppliers.formattedSupplier("Failed to stop old plugin with id = {}", pluginWrapper.getPluginId()).get()
			);
		}
	}

	/**
	 * Validates the new plugin in the temporary plugins' directory, uploads it to the root plugins' directory and to the {@link DataStore},
	 * starts the new plugin and saves it's info as {@link IntegrationType} object in the database
	 *
	 * @param newPluginId       Id of the new plugin
	 * @param previousPlugin    Previous plugin with the same id as the new one
	 * @param newPluginFileName New plugin file name
	 * @param integrationType   {@link IntegrationType} with the info about the new plugin
	 * @return updated {@link IntegrationType} object with the updated info about the new plugin
	 */
	private IntegrationType startUpPlugin(String newPluginId, Optional<PluginWrapper> previousPlugin, final String newPluginFileName,
			final IntegrationType integrationType) {

		startUpPlugin(newPluginId);
		validateNewPluginExtensionClasses(newPluginId, previousPlugin, newPluginFileName);
		unloadPlugin(newPluginId);

		String fileId = uploadPlugin(newPluginFileName, previousPlugin, newPluginId);
		IntegrationDetailsProperties.FILE_ID.setValue(integrationType.getDetails(), fileId);

		copyPluginToRootDirectory(newPluginFileName, previousPlugin, newPluginId);
		previousPlugin.ifPresent(p -> this.deletePreviousPlugin(p, newPluginFileName));

		return ofNullable(loadPlugin(Paths.get(pluginsDir, newPluginFileName))).map(newLoadedPluginId -> {
			startUpPlugin(newLoadedPluginId);

			integrationType.setName(newLoadedPluginId);
			Optional<ReportPortalExtensionPoint> instance = getInstance(integrationType.getName(), ReportPortalExtensionPoint.class);

			if (instance.isPresent()) {
				IntegrationDetailsProperties.COMMANDS.setValue(integrationType.getDetails(), instance.get().getCommandNames());
				integrationType.setIntegrationGroup(IntegrationGroupEnum.valueOf(instance.get().getIntegrationGroup().name()));
			}

			IntegrationDetailsProperties.FILE_NAME.setValue(integrationType.getDetails(), newPluginFileName);
			integrationType.setEnabled(true);
			return integrationTypeRepository.save(integrationType);
		}).orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Error during loading the plugin file = '{}'", newPluginFileName).get()
		));

	}

	/**
	 * Retrieves existed plugin info
	 *
	 * @param pluginPath Path to the plugin
	 * @return Plugin info
	 */
	private PluginInfo resolvePluginInfo(Path pluginPath) {
		try {
			return pluginLoader.extractPluginInfo(pluginPath);
		} catch (PluginException e) {
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
		}
	}

	/**
	 * Store existed plugin info from filesystem into database
	 *
	 * @param pluginWrapper   Plugin wrapper
	 * @param integrationType Plugin representation in database
	 */
	private void storeIntegrationType(PluginWrapper pluginWrapper, IntegrationType integrationType) {
		integrationType.setName(pluginWrapper.getPluginId());
		IntegrationDetailsProperties.FILE_ID.setValue(integrationType.getDetails(), pluginWrapper.getPluginPath().toString());
		Optional<ReportPortalExtensionPoint> instance = getInstance(integrationType.getName(), ReportPortalExtensionPoint.class);
		if (instance.isPresent()) {
			IntegrationDetailsProperties.COMMANDS.setValue(integrationType.getDetails(), instance.get().getCommandNames());
			integrationType.setIntegrationGroup(IntegrationGroupEnum.valueOf(instance.get().getIntegrationGroup().name()));
		}

		IntegrationDetailsProperties.FILE_NAME.setValue(integrationType.getDetails(),
				pluginWrapper.getPluginPath().getFileName().toString()
		);
		integrationType.setEnabled(true);
		integrationTypeRepository.save(integrationType);
	}

	/**
	 * Validates plugin's extension class/classes and reloads the previous plugin if it is present and the validation failed
	 *
	 * @param newPluginId       Id of the new plugin
	 * @param previousPlugin    Previous plugin with the same id as the new one
	 * @param newPluginFileName New plugin file name
	 * @see PluginLoader#validatePluginExtensionClasses(PluginWrapper))
	 */
	private void validateNewPluginExtensionClasses(String newPluginId, Optional<PluginWrapper> previousPlugin, String newPluginFileName) {
		PluginWrapper newPlugin = getPluginById(newPluginId).orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Plugin with id = {} has not been found.", newPluginId).get()
		));
		if (!pluginLoader.validatePluginExtensionClasses(newPlugin)) {
			unloadPlugin(newPluginId);
			previousPlugin.ifPresent(this::loadAndStartUpPlugin);
			deleteTempPlugin(newPluginFileName);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("New plugin with id = {} doesn't have mandatory extension classes.", newPluginId).get()
			);

		}
	}

	private void deleteTempPlugin(String newPluginFileName) {
		try {
			pluginLoader.deleteTempPlugin(pluginsTempDir, newPluginFileName);
		} catch (IOException e) {
			//error during temp plugin is not crucial, temp files cleaning will be delegated to the plugins cleaning job
			LOGGER.error("Error during temp plugin file removing.", e.getMessage());
		} finally {
			removeUploadingPlugin(newPluginFileName);
		}
	}

	/**
	 * Uploads plugin file to the instance of the configured {@link DataStore} and saves the file path to the {@link IntegrationType} object
	 *
	 * @param fileName       New plugin file name
	 * @param previousPlugin Previous plugin with the same 'id' as the new one
	 * @param newPluginId    Id of the new plugin
	 * @return File id
	 */
	private String uploadPlugin(final String fileName, Optional<PluginWrapper> previousPlugin, String newPluginId) {
		try (InputStream fileStream = FileUtils.openInputStream(FileUtils.getFile(pluginsTempDir, fileName))) {
			return pluginLoader.savePlugin(fileName, fileStream);
		} catch (Exception e) {
			previousPlugin.ifPresent(this::loadAndStartUpPlugin);
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to upload the new plugin file with id = {} to the data store", newPluginId).get()
			);
		}
	}

	private void copyPluginToRootDirectory(String newPluginFileName, Optional<PluginWrapper> previousPlugin, String newPluginId) {

		File tempPluginFile = FileUtils.getFile(pluginsTempDir, newPluginFileName);

		try {
			org.apache.commons.io.FileUtils.copyFile(tempPluginFile, new File(pluginsDir, newPluginFileName));
		} catch (IOException e) {
			previousPlugin.ifPresent(this::loadAndStartUpPlugin);
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to copy the new plugin file with id = {} to the root directory", newPluginId).get()
			);
		} finally {
			removeUploadingPlugin(newPluginFileName);
		}
	}

	private void deletePreviousPlugin(PluginWrapper previousPlugin, String newPluginFileName) {
		try {
			pluginLoader.deletePreviousPlugin(previousPlugin, newPluginFileName);
		} catch (IOException e) {
			loadAndStartUpPlugin(previousPlugin);
			throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
					Suppliers.formattedSupplier("Unable to delete the old plugin file with id = {}", previousPlugin.getPluginId()).get()
			);
		}
	}

	/**
	 * Load and start up the plugin
	 *
	 * @param plugin {@link PluginWrapper} with mandatory data for plugin loading: {@link PluginWrapper#pluginPath}
	 * @return {@link PluginState}
	 */
	private PluginState loadAndStartUpPlugin(PluginWrapper plugin) {
		if (plugin.getPluginState() == PluginState.STARTED) {
			return plugin.getPluginState();
		}

		return startUpPlugin(ofNullable(loadPlugin(plugin.getPluginPath())).orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Unable to reload plugin with id = '{}", plugin.getPluginId()).get()
		)));
	}
}
