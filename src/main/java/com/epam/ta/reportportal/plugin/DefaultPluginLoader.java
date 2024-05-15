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

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.UpdateManager;
import org.pf4j.update.UpdateRepository;
import org.springframework.batch.repeat.RepeatException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultPluginLoader {

  private final PluginManager pluginManager;

  private final Pf4jPluginBox pluginBox;

  private final DefaultUpdateManager defaultUpdateManager;

  @PostConstruct
  public void loadPlugins() {
    pluginBox.startUp();
    UpdateManager updateManager = new UpdateManager(pluginManager);
    if (updateManager.hasAvailablePlugins()) {
      List<PluginInfo> availablePlugins = updateManager.getAvailablePlugins();
      availablePlugins.forEach(pluginInfo -> {
        String latestVersion = updateManager.getLastPluginRelease(pluginInfo.id).version;
        Path path = defaultUpdateManager.downloadPlugin(pluginInfo.id, latestVersion);
        try {
          pluginBox.uploadPlugin(path.getFileName().toString(), Files.newInputStream(path));
        } catch (IOException e) {
          log.error(e.getMessage());
          throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
              e.getMessage());
        }
      });
    }
  }

}
