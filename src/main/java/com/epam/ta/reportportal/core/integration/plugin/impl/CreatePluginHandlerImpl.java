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

import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.IntegrationTypeHandler;
import com.epam.ta.reportportal.core.integration.plugin.file.PluginFileManager;
import com.epam.ta.reportportal.core.integration.plugin.info.PluginInfoResolver;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.core.plugin.PluginMetadata;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
public class CreatePluginHandlerImpl implements CreatePluginHandler {

	public static final Logger LOGGER = LoggerFactory.getLogger(CreatePluginHandlerImpl.class);

	public static final String LOAD_KEY = "load";

	private final String pluginsTempDir;
	private final String pluginsDir;
	private final String resourcesDir;
	private final PluginFileManager pluginFileManager;
	private final PluginInfoResolver pluginInfoResolver;
	private final Pf4jPluginBox pluginBox;
	private final IntegrationTypeHandler integrationTypeHandler;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public CreatePluginHandlerImpl(@Value("${rp.plugins.temp.path}") String pluginsTempPath, @Value("${rp.plugins.path}") String pluginsDir,
			@Value("${rp.plugins.resources.path}") String resourcesDir, PluginFileManager pluginFileManager,
			PluginInfoResolver pluginInfoResolver, Pf4jPluginBox pluginBox, IntegrationTypeHandler integrationTypeHandler,
			ApplicationEventPublisher applicationEventPublisher) throws IOException {
		this.pluginsTempDir = pluginsTempPath;
		this.pluginsDir = pluginsDir;
		this.resourcesDir = resourcesDir;

		Files.createDirectories(Paths.get(this.pluginsTempDir));
		Files.createDirectories(Paths.get(this.pluginsDir));
		Files.createDirectories(Paths.get(this.resourcesDir));

		this.pluginFileManager = pluginFileManager;
		this.pluginInfoResolver = pluginInfoResolver;
		this.pluginBox = pluginBox;
		this.integrationTypeHandler = integrationTypeHandler;
		this.applicationEventPublisher = applicationEventPublisher;

	}

	@Override
	public EntryCreatedRS uploadPlugin(MultipartFile pluginFile) {
		final Path tempPluginPath = pluginFileManager.uploadTemp(pluginFile, Paths.get(resourcesDir));
		final PluginInfo pluginInfo = pluginInfoResolver.resolveInfo(tempPluginPath);
		final PluginPathInfo pluginPathInfo = upload(pluginInfo);

		return loadPlugin(pluginInfo, pluginPathInfo);
	}

	private EntryCreatedRS loadPlugin(PluginInfo pluginInfo, PluginPathInfo pluginPathInfo) {
		final Optional<PluginWrapper> previousPlugin = unloadPreviousPlugin(pluginInfo);
		try {
			pluginBox.startUpPlugin(pluginPathInfo.getPluginPath());

			final IntegrationType integrationType = savePluginData(new PluginMetadata(pluginInfo, pluginPathInfo));
			applicationEventPublisher.publishEvent(new PluginEvent(integrationType.getName(), LOAD_KEY));

			previousPlugin.map(PluginWrapper::getPluginPath).ifPresent(pluginFileManager::delete);

			return new EntryCreatedRS(integrationType.getId());
		} catch (Exception ex) {
			previousPlugin.ifPresent(p -> loadPreviousPlugin(p, pluginPathInfo));
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, ex.getMessage());
		} finally {
			pluginFileManager.delete(pluginInfo.getOriginalFilePath());
		}
	}

	private Optional<PluginWrapper> unloadPreviousPlugin(PluginInfo pluginInfo) {
		try {
			return pluginBox.unloadPlugin(pluginInfo.getId());
		} catch (PluginException e) {
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
		}
	}

	private PluginPathInfo upload(PluginInfo pluginInfo) {
		return pluginFileManager.upload(pluginInfo, Paths.get(pluginsDir), Paths.get(resourcesDir));
	}

	private IntegrationType savePluginData(PluginMetadata pluginMetadata) {
		final PluginInfo pluginInfo = pluginMetadata.getPluginInfo();
		return integrationTypeHandler.getByName(pluginInfo.getId())
				.map(existing -> updateExisting(pluginMetadata, existing))
				.orElseGet(() -> createNew(pluginMetadata));
	}

	private IntegrationType updateExisting(PluginMetadata pluginMetadata, IntegrationType existing) {
		deletePreviousPluginFile(existing, pluginMetadata.getPluginPathInfo().getFileId());
		updateWithExtensionParams(pluginMetadata);
		return integrationTypeHandler.update(existing, pluginMetadata);
	}

	private void deletePreviousPluginFile(IntegrationType oldIntegrationType, String newFileId) {
		ofNullable(oldIntegrationType.getDetails()).flatMap(details -> ofNullable(details.getDetails()))
				.flatMap(IntegrationTypeProperties.FILE_ID::getValue)
				.map(String::valueOf)
				.filter(oldFileId -> !oldFileId.equals(newFileId))
				.ifPresent(pluginFileManager::delete);
	}

	private IntegrationType createNew(PluginMetadata pluginMetadata) {
		updateWithExtensionParams(pluginMetadata);
		return integrationTypeHandler.create(pluginMetadata);
	}

	private void updateWithExtensionParams(PluginMetadata pluginMetadata) {
		final Optional<ReportPortalExtensionPoint> extensionPoint = getExtensionPoint(pluginMetadata.getPluginInfo());
		extensionPoint.ifPresent(extension -> {
			pluginMetadata.setIntegrationGroup(IntegrationGroupEnum.valueOf(extension.getIntegrationGroup().name()));
			pluginMetadata.setPluginParams(extension.getPluginParams());
		});
	}

	private Optional<ReportPortalExtensionPoint> getExtensionPoint(PluginInfo pluginInfo) {
		return pluginBox.getInstance(pluginInfo.getId(), ReportPortalExtensionPoint.class);
	}

	/**
	 * Load and start up the previous plugin
	 *
	 * @param previousPlugin    {@link PluginWrapper} with mandatory data for plugin loading: {@link PluginWrapper#getPluginPath()}
	 * @param newPluginPathInfo {@link PluginPathInfo} of the plugin which uploading ended up with an error
	 */
	private void loadPreviousPlugin(PluginWrapper previousPlugin, PluginPathInfo newPluginPathInfo) {
		pluginFileManager.delete(newPluginPathInfo.getFileId());

		final PluginInfo previousPluginInfo = pluginInfoResolver.resolveInfo(previousPlugin.getPluginPath());
		final PluginPathInfo previousPluginPathInfo = upload(previousPluginInfo);
		pluginBox.loadPreviousPlugin(previousPlugin.getPluginId(), previousPluginPathInfo);
	}

}
