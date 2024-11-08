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

import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.pf4j.update.SimpleFileDownloader;
import org.pf4j.update.UpdateManager;
import org.pf4j.update.UpdateRepository;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginStartUpService {

  private final PluginManager pluginManager;

  private final Pf4jPluginBox pluginBox;

  @PostConstruct
  public void loadPlugins() {
    pluginBox.startUp();
    UpdateManager updateManager = new UpdateManager(pluginManager, getDefaultPluginRepositories());
    if (updateManager.hasAvailablePlugins()) {
      updateManager.getAvailablePlugins()
          .forEach(pluginInfo -> loadLatestVersion(updateManager, pluginInfo));
    }
  }

  private void loadLatestVersion(UpdateManager updateManager, PluginInfo pluginInfo) {
    try {
      PluginRelease lastRelease = updateManager.getLastPluginRelease(pluginInfo.id);
      Path path = new SimpleFileDownloader().downloadFile(URI.create(lastRelease.url).toURL());
      pluginBox.uploadPlugin(path.getFileName().toString(), Files.newInputStream(path));
    } catch (IOException e) {
      log.warn("Can't load default remote plugin with id {}. Error: {}", pluginInfo.id,
          e.getMessage());
    }
  }

  private List<UpdateRepository> getDefaultPluginRepositories() {
    try {
      return Lists.newArrayList(new DefaultUpdateRepository(
          "plugin-import-junit", URI.create(
              "https://raw.githubusercontent.com/reportportal/plugin-import-junit/main/jars/plugins.json")
          .toURL()));
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Collections.emptyList();
  }
}
