/*
 * Copyright 2025 EPAM Systems
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

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.integration.plugin.PluginMarketPlaceHandler;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.MarketplaceRepository;
import com.epam.ta.reportportal.entity.plugin.Marketplace;
import com.epam.ta.reportportal.model.marketplace.MarketplaceResource;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultVersionManager;
import org.pf4j.PluginManager;
import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.pf4j.update.UpdateManager;
import org.pf4j.update.UpdateRepository;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginMarketPlaceHandlerImp implements PluginMarketPlaceHandler {

  private final PluginManager pluginManager;

  private final Pf4jPluginBox pluginBox;

  private final IntegrationTypeRepository integrationTypeRepository;

  private final MarketplaceRepository marketplaceRepository;

  @Override
  public List<PluginInfo> getAvailablePlugins() {
    UpdateManager updateManager = new UpdateManager(pluginManager, getDefaultPluginRepositories());
    List<PluginInfo> plugins = updateManager.getPlugins();
    return plugins;
  }

  @Override
  public void installPlugin(String pluginId, String version) {
    UpdateManagerImpl updateManager = new UpdateManagerImpl(pluginManager, getDefaultPluginRepositories());
    loadVersion(updateManager, pluginId, version);
  }

  @Override
  public List<MarketplaceResource> getAllMarketplaces() {
    List<Marketplace> marketplaces = marketplaceRepository.findAll();
    return marketplaces.stream()
        .map(marketplace -> MarketplaceResource.builder()
            .name(marketplace.getName())
            .url(marketplace.getURI())
            .build())
        .collect(Collectors.toList());
  }

  @Override
  public void addMarketPlace(MarketplaceResource marketplaceRs) {
    Marketplace marketplace = new Marketplace();
    marketplace.setName(marketplaceRs.getName());
    marketplace.setURI(marketplaceRs.getUrl());
    marketplaceRepository.save(marketplace);
  }

  @Override
  public void deleteMarketPlace(String name) {
    Marketplace marketplace = marketplaceRepository.findById(name)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, name));
    marketplaceRepository.delete(marketplace);
  }

  @Override
  public void editMarketPlace(MarketplaceResource marketplaceRs) {
    Marketplace marketplace = marketplaceRepository.findById(marketplaceRs.getName())
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, marketplaceRs.getName()));
    marketplace.setURI(marketplaceRs.getUrl());
    marketplaceRepository.save(marketplace);
  }

  private List<UpdateRepository> getDefaultPluginRepositories() {
    List<Marketplace> marketplaces = marketplaceRepository.findAll();
    List<UpdateRepository> repositories = new ArrayList<>();
    for (Marketplace marketplace : marketplaces) {
      try {
        DefaultUpdateRepository repository = new DefaultUpdateRepository(
            marketplace.getName(),
            URI.create(marketplace.getURI()).toURL(),
            extractJsonFileName(marketplace.getURI())
        );
        repositories.add(repository);
      } catch (Exception e) {
        log.error("Failed to create repository from marketplace entry: {} - {}", marketplace.getName(), e.getMessage());
      }
    }

    return repositories;
  }

  public String extractJsonFileName(String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    String[] parts = path.split("/");
    if (parts.length == 0) {
      return null;
    }

    String fileName = parts[parts.length - 1];

    if (fileName.endsWith(".json")) {
      return fileName;
    } else {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "File should be json");
    }
  }

  private void loadVersion(UpdateManagerImpl updateManager, String pluginId, String version) {
    try {
      PluginRelease releaseForPlugin = updateManager.findReleaseForPlugin(pluginId, version);
      Path pluginPath = updateManager.downloadPlugin(pluginId, version);
      DefaultVersionManager defaultVersionManager = new DefaultVersionManager();
      if (!defaultVersionManager.checkVersionConstraint("5.14.0", releaseForPlugin.requires)) {
        throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, "Version constrain");
      }
      pluginBox.uploadPlugin(pluginPath.getFileName().toString(), Files.newInputStream(pluginPath));
    } catch (IOException e) {
      log.warn("Can't load default remote plugin with pluginId {}. Error: {}", pluginId,
          e.getMessage());
    }
  }

  private boolean isVersionUploaded(PluginInfo pluginInfo, PluginRelease lastRelease) {
    var res = integrationTypeRepository.findByName(pluginInfo.id)
        .flatMap(it -> ofNullable(it.getDetails())).flatMap(
            typeDetails -> IntegrationTypeProperties.VERSION.getValue(typeDetails.getDetails())
                .map(String::valueOf))
        .filter(version -> version.equalsIgnoreCase(lastRelease.version));
    if (res.isPresent()) {
      log.info("Plugin with the latest version {} is already loaded", lastRelease.version);
      return true;
    }
    return false;
  }
}
