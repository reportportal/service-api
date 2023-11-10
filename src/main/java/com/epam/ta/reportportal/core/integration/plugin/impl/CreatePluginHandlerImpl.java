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

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.activity.PluginUploadedEvent;
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
import com.epam.ta.reportportal.ws.model.activity.PluginActivityResource;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreatePluginHandlerImpl implements CreatePluginHandler {

  private static final Logger logger = LoggerFactory.getLogger(CreatePluginHandlerImpl.class);

  private final PluginFileManager pluginFileManager;
  private final PluginInfoResolver pluginInfoResolver;
  private final Pf4jPluginBox pluginBox;

  private final IntegrationTypeHandler integrationTypeHandler;

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public CreatePluginHandlerImpl(PluginFileManager pluginFileManager,
      PluginInfoResolver pluginInfoResolver, Pf4jPluginBox pluginBox,
      IntegrationTypeHandler integrationTypeHandler,
      ApplicationEventPublisher applicationEventPublisher) {
    this.pluginFileManager = pluginFileManager;
    this.pluginInfoResolver = pluginInfoResolver;
    this.pluginBox = pluginBox;
    this.integrationTypeHandler = integrationTypeHandler;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public EntryCreatedRS uploadPlugin(MultipartFile pluginFile, ReportPortalUser user) {
    final Path tempPluginPath = pluginFileManager.uploadTemp(pluginFile);
    final PluginInfo pluginInfo = pluginInfoResolver.resolveInfo(tempPluginPath);
    final PluginPathInfo pluginPathInfo = pluginFileManager.upload(pluginInfo);
    final IntegrationType integrationType = loadPlugin(pluginInfo, pluginPathInfo);
    publishEvent(user, integrationType);
    return new EntryCreatedRS(integrationType.getId());
  }

  private void publishEvent(ReportPortalUser user, IntegrationType integrationType) {
    PluginActivityResource pluginActivityResource = new PluginActivityResource();
    pluginActivityResource.setId(integrationType.getId());
    pluginActivityResource.setName(integrationType.getName());
    applicationEventPublisher.publishEvent(
        new PluginUploadedEvent(pluginActivityResource, user.getUserId(), user.getUsername()));
  }

  private IntegrationType loadPlugin(PluginInfo pluginInfo, PluginPathInfo pluginPathInfo) {
    final Optional<PluginWrapper> previousPlugin = unloadPreviousPlugin(pluginInfo);
    try {
      final IntegrationType integrationType = savePluginData(
          new PluginMetadata(pluginInfo, pluginPathInfo));
      pluginBox.startUpPlugin(pluginPathInfo.getPluginPath());
      previousPlugin.map(PluginWrapper::getPluginPath).ifPresent(pluginFileManager::delete);
      return integrationType;
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      previousPlugin.ifPresent(p -> loadPreviousPlugin(p, pluginPathInfo));
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, ex.getMessage());
    } finally {
      pluginFileManager.delete(pluginInfo.getOriginalFilePath());
    }
  }

  private Optional<PluginWrapper> unloadPreviousPlugin(PluginInfo pluginInfo) {
    final Optional<PluginWrapper> previousPlugin = pluginBox.getPluginById(pluginInfo.getId());
    previousPlugin.ifPresent(
        pluginWrapper -> BusinessRule.expect(pluginBox.unloadPlugin(pluginWrapper),
                BooleanUtils::isTrue)
            .verify(ErrorType.PLUGIN_UPLOAD_ERROR,
                Suppliers.formattedSupplier("Failed to unload plugin with id = '{}'",
                    pluginWrapper.getPluginId()).get()
            ));
    return previousPlugin;
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
    final Optional<ReportPortalExtensionPoint> extensionPoint = getExtensionPoint(
        pluginMetadata.getPluginInfo());
    extensionPoint.ifPresent(extension -> {
      pluginMetadata.setIntegrationGroup(
          IntegrationGroupEnum.valueOf(extension.getIntegrationGroup().name()));
      pluginMetadata.setPluginParams(extension.getPluginParams());
    });
  }

  private Optional<ReportPortalExtensionPoint> getExtensionPoint(PluginInfo pluginInfo) {
    return pluginBox.getInstance(pluginInfo.getId(), ReportPortalExtensionPoint.class);
  }

  /**
   * Load and start up the previous plugin.
   *
   * @param previousPlugin    {@link PluginWrapper} with mandatory data for plugin loading: {@link
   *                          PluginWrapper#getPluginPath()}
   * @param newPluginPathInfo {@link PluginPathInfo} of the plugin which uploading ended up with an
   *                          error
   */
  private void loadPreviousPlugin(PluginWrapper previousPlugin, PluginPathInfo newPluginPathInfo) {
    pluginFileManager.delete(newPluginPathInfo.getFileId());

    final PluginInfo previousPluginInfo = pluginInfoResolver.resolveInfo(
        previousPlugin.getPluginPath());
    final PluginPathInfo previousPluginPathInfo = pluginFileManager.upload(previousPluginInfo);

    pluginBox.getPluginById(previousPlugin.getPluginId()).ifPresent(pluginBox::deletePlugin);
    pluginBox.loadPlugin(previousPluginPathInfo.getPluginPath())
        .flatMap(pluginBox::getPluginById)
        .ifPresentOrElse(pluginBox::startUpPlugin, () -> {
          throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
              Suppliers.formattedSupplier("Unable to reload previousPlugin with id = '{}'",
                      previousPlugin.getPluginId())
                  .get()
          );
        });
  }

}
