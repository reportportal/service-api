/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.PluginInfo;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationTypeBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreatePluginHandlerImpl implements CreatePluginHandler {

	public static final String PLUGIN_TEMP_DIRECTORY = "/temp/";

	private final String pluginsRootPath;

	private final String pluginsTempFolderPath;

	private final Pf4jPluginBox pluginBox;

	private final PluginLoader pluginLoader;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final DataStore dataStore;

	@Autowired
	public CreatePluginHandlerImpl(@Value("${rp.plugins.path}") String pluginsRootPath, Pf4jPluginBox pluginBox, PluginLoader pluginLoader,
			IntegrationTypeRepository integrationTypeRepository, DataStore dataStore) {
		this.pluginsRootPath = pluginsRootPath;
		this.pluginsTempFolderPath = pluginsRootPath + PLUGIN_TEMP_DIRECTORY;
		this.pluginBox = pluginBox;
		this.pluginLoader = pluginLoader;
		this.integrationTypeRepository = integrationTypeRepository;
		this.dataStore = dataStore;
	}

	@Override
	public EntryCreatedRS uploadPlugin(MultipartFile pluginFile) {

		String newPluginFileName = pluginFile.getOriginalFilename();
		BusinessRule.expect(newPluginFileName, StringUtils::isNotBlank)
				.verify(ErrorType.BAD_REQUEST_ERROR, "File name should be not empty.");

		Path newPluginTempPath = Paths.get(pluginsTempFolderPath, newPluginFileName);

		PluginInfo newPluginInfo = resolvePluginInfo(pluginFile, newPluginTempPath);
		IntegrationType integrationType = resolveIntegrationType(newPluginInfo, newPluginFileName);

		Optional<PluginWrapper> previousPlugin = pluginLoader.retrievePreviousPlugin(newPluginInfo.getId(), newPluginFileName);

		String newPluginId = pluginBox.loadPlugin(newPluginTempPath);

		if (ofNullable(newPluginId).isPresent()) {

			IntegrationType newIntegrationType = startUpPlugin(newPluginId, previousPlugin, pluginFile, integrationType);

			return new EntryCreatedRS(newIntegrationType.getId());

		} else {

			previousPlugin.ifPresent(pluginLoader::loadAndStartUpPlugin);

			pluginLoader.deleteTempPlugin(pluginsTempFolderPath, newPluginFileName);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Failed to load new plugin from file = {}", newPluginFileName).get()
			);
		}

	}

	/**
	 * Uploads the plugin file to the temp directory and extracts it's info.
	 * Presence of the plugin version is mandatory
	 *
	 * @param pluginFile        Plugin file to upload
	 * @param newPluginTempPath Temporary directory for the new plugins for validation
	 * @return {@link PluginInfo}
	 */
	private PluginInfo resolvePluginInfo(MultipartFile pluginFile, Path newPluginTempPath) {

		Path pluginsTempPath = Paths.get(pluginsTempFolderPath);

		createTempPluginsFolderIfNotExists(pluginsTempPath);
		pluginLoader.resolveFileExtensionAndUploadTempPlugin(pluginFile, pluginsTempPath);

		PluginInfo newPluginInfo = pluginLoader.extractPluginInfo(newPluginTempPath);

		if (!ofNullable(newPluginInfo.getVersion()).isPresent()) {
			pluginBox.removeUploadingPlugin(pluginFile.getOriginalFilename());

			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Plugin version should be specified.");
		}

		return newPluginInfo;
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
	 * Creates the {@link IntegrationType} object based on the params of the plugin
	 *
	 * @param newPluginInfo     {@link PluginInfo} with {@link PluginInfo#id} and {@link PluginInfo#version}
	 * @param newPluginFileName Name of the new plugin's file
	 * @return {@link IntegrationType}
	 */
	private IntegrationType resolveIntegrationType(PluginInfo newPluginInfo, String newPluginFileName) {

		ReportPortalIntegrationEnum reportPortalIntegration = resolveIntegration(newPluginInfo, newPluginFileName);

		IntegrationType integrationType = retrieveIntegrationType(newPluginInfo, reportPortalIntegration);
		IntegrationDetailsProperties.VERSION.setValue(integrationType.getDetails(), newPluginInfo.getVersion());

		return integrationType;
	}

	/**
	 * Resolves the type of the plugin by it's 'id'. should be one of the {@link ReportPortalIntegrationEnum} values
	 *
	 * @param pluginInfo        {@link PluginInfo} with {@link PluginInfo#id}
	 * @param newPluginFileName Name of the new plugin file
	 * @return {@link ReportPortalIntegrationEnum} instance
	 */
	private ReportPortalIntegrationEnum resolveIntegration(PluginInfo pluginInfo, String newPluginFileName) {

		Optional<ReportPortalIntegrationEnum> reportPortalIntegration = ReportPortalIntegrationEnum.findByName(pluginInfo.getId());

		if (!reportPortalIntegration.isPresent()) {
			pluginBox.removeUploadingPlugin(newPluginFileName);

			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					Suppliers.formattedSupplier("Unknown integration type - {}", pluginInfo.getId()).get()
			);
		}

		return reportPortalIntegration.get();
	}

	private IntegrationType retrieveIntegrationType(PluginInfo pluginInfo, ReportPortalIntegrationEnum reportPortalIntegration) {

		IntegrationType integrationType = integrationTypeRepository.findByName(pluginInfo.getId()).map(it -> {
			IntegrationDetailsProperties.VERSION.getValue(it.getDetails().getDetails())
					.ifPresent(version -> BusinessRule.expect(version, v -> !v.equalsIgnoreCase(pluginInfo.getVersion()))
							.verify(ErrorType.PLUGIN_UPLOAD_ERROR, Suppliers.formattedSupplier(
									"Plugin with ID = '{}' of the same VERSION = '{}' has already been uploaded.",
									pluginInfo.getId(),
									pluginInfo.getVersion()
							)));
			return it;
		}).orElseGet(() -> new IntegrationTypeBuilder().get());
		if (integrationType.getDetails() == null) {
			integrationType.setDetails(IntegrationTypeBuilder.createIntegrationTypeDetails());
		}

		integrationType.setIntegrationGroup(reportPortalIntegration.getIntegrationGroup());
		integrationType.setCreationDate(LocalDateTime.now());

		return integrationType;
	}

	/**
	 * Validates the new plugin in the temporary plugins' directory, uploads it to the root plugins' directory and to the {@link DataStore},
	 * starts the new plugin and saves it's info as {@link IntegrationType} object in the database
	 *
	 * @param newPluginId     Id of the new plugin
	 * @param previousPlugin  Previous plugin with the same id as the new one
	 * @param pluginFile      New plugin file
	 * @param integrationType {@link IntegrationType} with the info about the new plugin
	 * @return updated {@link IntegrationType} object with the updated info about the new plugin
	 */
	private IntegrationType startUpPlugin(String newPluginId, Optional<PluginWrapper> previousPlugin, MultipartFile pluginFile,
			IntegrationType integrationType) {

		pluginBox.startUpPlugin(newPluginId);

		String newPluginFileName = pluginFile.getOriginalFilename();

		validateNewPluginExtensionClasses(newPluginId, previousPlugin, newPluginFileName);

		pluginBox.unloadPlugin(newPluginId);

		uploadPlugin(pluginFile, integrationType, previousPlugin, newPluginId);

		copyPluginToRootDirectory(newPluginFileName, previousPlugin, newPluginId);

		previousPlugin.ifPresent(p -> pluginLoader.deletePreviousPlugin(p, newPluginFileName));

		Optional<String> newLoadedPluginId = ofNullable(pluginBox.loadPlugin(Paths.get(pluginsRootPath, newPluginFileName)));

		if (newLoadedPluginId.isPresent()) {
			pluginBox.startUpPlugin(newLoadedPluginId.get());

			integrationType.setName(newLoadedPluginId.get());

			IntegrationDetailsProperties.FILE_NAME.setValue(integrationType.getDetails(), newPluginFileName);

			integrationType.setEnabled(true);

			integrationType = integrationTypeRepository.save(integrationType);

			pluginLoader.deleteTempPlugin(pluginsTempFolderPath, newPluginFileName);

			return integrationType;
		} else {
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					Suppliers.formattedSupplier("Error during loading the plugin file = '{}'", newPluginFileName).get()
			);
		}

	}

	/**
	 * Validates plugin's extension class/classes and reloads the previous plugin if it is present and the validation failed
	 *
	 * @param newPluginId       Id of the new plugin
	 * @param previousPlugin    Previous plugin with the same id as the new one
	 * @param newPluginFileName New plugin file name
	 * @see PluginLoader#validatePluginExtensionClasses(String)
	 */
	private void validateNewPluginExtensionClasses(String newPluginId, Optional<PluginWrapper> previousPlugin, String newPluginFileName) {
		if (!pluginLoader.validatePluginExtensionClasses(newPluginId)) {

			pluginBox.unloadPlugin(newPluginId);
			previousPlugin.ifPresent(pluginLoader::loadAndStartUpPlugin);
			pluginLoader.deleteTempPlugin(pluginsTempFolderPath, newPluginFileName);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("New plugin with id = {} doesn't have mandatory extension classes.", newPluginId).get()
			);

		}
	}

	/**
	 * Uploads plugin file to the instance of the configured {@link DataStore} and saves the file path to the {@link IntegrationType} object
	 *
	 * @param pluginFile      New plugin file
	 * @param integrationType {@link IntegrationType} object with info about plugin
	 * @param previousPlugin  Previous plugin with the same 'id' as the new one
	 * @param newPluginId     Id of the new plugin
	 */
	private void uploadPlugin(MultipartFile pluginFile, IntegrationType integrationType, Optional<PluginWrapper> previousPlugin,
			String newPluginId) {

		try {

			String fileId = dataStore.save(pluginFile.getOriginalFilename(), pluginFile.getInputStream());
			IntegrationDetailsProperties.FILE_ID.setValue(integrationType.getDetails(), fileId);

		} catch (IOException e) {

			previousPlugin.ifPresent(pluginLoader::loadAndStartUpPlugin);

			pluginBox.removeUploadingPlugin(pluginFile.getOriginalFilename());

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to upload the new plugin file with id = {} to the data store", newPluginId).get()
			);
		}
	}

	private void copyPluginToRootDirectory(String newPluginFileName, Optional<PluginWrapper> previousPlugin, String newPluginId) {

		File tempPluginFile = new File(pluginsTempFolderPath, newPluginFileName);

		if (tempPluginFile.exists()) {
			try {
				org.apache.commons.io.FileUtils.copyFile(tempPluginFile, new File(pluginsRootPath, newPluginFileName));
			} catch (IOException e) {

				previousPlugin.ifPresent(pluginLoader::loadAndStartUpPlugin);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to copy the new plugin file with id = {} to the root directory", newPluginId)
								.get()
				);
			}
		}
	}
}
