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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.PluginInfo;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.integration.plugin.util.PluginUtils;
import com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreatePluginHandlerImpl implements CreatePluginHandler {

	public static final String PLUGIN_TEMP_DIRECTORY = "/temp/";

	private final String pluginsRootPath;

	private final PluginBox pluginBox;

	private final PluginLoader pluginLoader;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final DataStore dataStore;

	@Autowired
	public CreatePluginHandlerImpl(@Value("${rp.plugins.path}") String pluginsRootPath, PluginBox pluginBox, PluginLoader pluginLoader,
			IntegrationTypeRepository integrationTypeRepository, DataStore dataStore) {
		this.pluginsRootPath = pluginsRootPath;
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

		final String pluginsTempPath = pluginsRootPath + PLUGIN_TEMP_DIRECTORY;

		PluginUtils.createTempPluginsFolderIfNotExists(pluginsTempPath);
		PluginUtils.resolveExtensionAndUploadTempPlugin(pluginFile, pluginsTempPath);

		Path newPluginTempPath = Paths.get(pluginsTempPath, newPluginFileName);
		PluginInfo newPluginInfo = pluginLoader.extractPluginInfo(newPluginTempPath);

		BusinessRule.expect(newPluginInfo.getVersion(), notNull())
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Plugin version should be specified.");

		ReportPortalIntegrationEnum reportPortalIntegration = ReportPortalIntegrationEnum.findByName(newPluginInfo.getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Unknown integration type - {} ", newPluginInfo.getId()).get()
				));

		Optional<PluginWrapper> oldPlugin = pluginLoader.retrieveOldPlugin(newPluginInfo.getId(), newPluginFileName);

		IntegrationType integrationType = retrieveIntegrationType(newPluginInfo, reportPortalIntegration);
		IntegrationDetailsProperties.VERSION.setValue(integrationType.getDetails(), newPluginInfo.getVersion());

		String newPluginId = pluginBox.loadPlugin(newPluginTempPath);

		pluginBox.startUpPlugin(newPluginId);

		if (ofNullable(newPluginId).isPresent()) {

			if (!pluginLoader.validatePluginExtensionClasses(newPluginId)) {

				pluginBox.unloadPlugin(newPluginId);
				oldPlugin.ifPresent(pluginLoader::reloadPlugin);
				PluginUtils.deleteTempPlugin(newPluginTempPath);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("New plugin with id = {} doesn't have mandatory extension classes.")
				);

			}

			pluginBox.unloadPlugin(newPluginId);

			try {

				String fileId = dataStore.save(newPluginFileName, pluginFile.getInputStream());
				IntegrationDetailsProperties.FILE_ID.setValue(integrationType.getDetails(), fileId);

			} catch (IOException e) {

				oldPlugin.ifPresent(pluginLoader::reloadPlugin);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to upload the new plugin file with id = {} to the data store", newPluginId)
								.get()
				);
			}

			try {

				org.apache.commons.io.FileUtils.copyFile(new File(pluginsTempPath, newPluginFileName),
						new File(pluginsRootPath, newPluginFileName)
				);

			} catch (IOException e) {

				oldPlugin.ifPresent(pluginLoader::reloadPlugin);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to copy the new plugin file with id = {} to the root directory", newPluginId)
								.get()
				);
			}

			oldPlugin.ifPresent(p -> pluginLoader.deleteOldPlugin(p, newPluginFileName));

			String newLoadedPluginId = pluginBox.loadPlugin(Paths.get(pluginsRootPath, newPluginFileName));
			pluginBox.startUpPlugin(newLoadedPluginId);

			integrationType.setName(newLoadedPluginId);

			IntegrationDetailsProperties.FILE_NAME.setValue(integrationType.getDetails(), newPluginFileName);

			integrationType = integrationTypeRepository.save(integrationType);

			PluginUtils.deleteTempPlugin(newPluginTempPath);

			return new EntryCreatedRS(integrationType.getId());

		} else {

			oldPlugin.ifPresent(pluginLoader::reloadPlugin);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Failed to load new plugin from file = {}", newPluginFileName).get()
			);
		}

	}

	private IntegrationType retrieveIntegrationType(PluginInfo pluginInfo, ReportPortalIntegrationEnum reportPortalIntegration) {

		IntegrationType integrationType = integrationTypeRepository.findByName(pluginInfo.getId()).orElseGet(this::getNewIntegrationType);
		if (integrationType.getDetails() == null) {
			integrationType.setDetails(getEmptyIntegrationTypeDetails());
		}

		integrationType.setIntegrationGroup(reportPortalIntegration.getIntegrationGroup());
		integrationType.setCreationDate(LocalDateTime.now());

		return integrationType;
	}

	private IntegrationType getNewIntegrationType() {
		IntegrationType type = new IntegrationType();

		type.setDetails(getEmptyIntegrationTypeDetails());

		return type;
	}

	private IntegrationTypeDetails getEmptyIntegrationTypeDetails() {

		IntegrationTypeDetails integrationTypeDetails = new IntegrationTypeDetails();
		integrationTypeDetails.setDetails(new HashMap<>());

		return integrationTypeDetails;
	}
}
