/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin.handler.impl;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.core.integration.plugin.IntegrationTypeHandler;
import com.epam.ta.reportportal.core.plugin.PluginMetadata;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationTypeBuilder;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link IntegrationTypeHandler} that provides CRU (create, read, update)
 * operations for {@link IntegrationType} entity
 *
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
@Service
public class IntegrationTypeHandlerImpl implements IntegrationTypeHandler {

  private final IntegrationTypeRepository integrationTypeRepository;

  public IntegrationTypeHandlerImpl(IntegrationTypeRepository integrationTypeRepository) {
    this.integrationTypeRepository = integrationTypeRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<IntegrationType> getByName(String name) {
    return integrationTypeRepository.findByName(name);
  }

  @Override
  @Transactional
  public IntegrationType create(PluginMetadata pluginMetadata) {
    final IntegrationTypeBuilder builder = new IntegrationTypeBuilder();
    builder.setName(pluginMetadata.getPluginInfo().getId())
        .setIntegrationGroup(IntegrationGroupEnum.OTHER)
        .setDetails(IntegrationTypeBuilder.createIntegrationTypeDetails());
    builder.setEnabled(true);
    fillBuilder(builder, pluginMetadata);
    return integrationTypeRepository.save(builder.get());
  }

  @Override
  @Transactional
  public IntegrationType update(IntegrationType integrationType, PluginMetadata pluginMetadata) {
    final IntegrationTypeBuilder builder = new IntegrationTypeBuilder(integrationType);
    builder.setEnabled(true);
    fillBuilder(builder, pluginMetadata);
    return integrationTypeRepository.save(builder.get());
  }

  private void fillBuilder(IntegrationTypeBuilder builder, PluginMetadata pluginMetadata) {
    builder.putDetails(IntegrationTypeProperties.VERSION.getAttribute(),
        pluginMetadata.getPluginInfo().getVersion());
    ofNullable(pluginMetadata.getPluginParams()).ifPresent(builder::putDetails);
    ofNullable(pluginMetadata.getIntegrationGroup()).ifPresent(builder::setIntegrationGroup);
    fillBuilder(builder, pluginMetadata.getPluginPathInfo());
  }

  private void fillBuilder(IntegrationTypeBuilder builder, PluginPathInfo pluginPathInfo) {
    builder.putDetails(IntegrationTypeProperties.FILE_ID.getAttribute(),
        pluginPathInfo.getFileId());
    builder.putDetails(IntegrationTypeProperties.FILE_NAME.getAttribute(),
        pluginPathInfo.getFileName());
    builder.putDetails(IntegrationTypeProperties.RESOURCES_DIRECTORY.getAttribute(),
        pluginPathInfo.getResourcesPath().toString());
  }

}