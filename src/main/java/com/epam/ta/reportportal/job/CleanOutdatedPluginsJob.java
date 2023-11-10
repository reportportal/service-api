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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.configs.Conditions;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Conditional(Conditions.NotTestCondition.class)
@Service
public class CleanOutdatedPluginsJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(CleanOutdatedPluginsJob.class);

  private final IntegrationTypeRepository integrationTypeRepository;

  private final Pf4jPluginBox pluginBox;

  @Autowired
  public CleanOutdatedPluginsJob(IntegrationTypeRepository integrationTypeRepository,
      Pf4jPluginBox pf4jPluginBox) {
    this.integrationTypeRepository = integrationTypeRepository;
    this.pluginBox = pf4jPluginBox;
  }

  @Scheduled(fixedDelayString = "${com.ta.reportportal.job.clean.outdated.plugins.cron}")
  public void execute() {
    final List<IntegrationType> integrationTypes = integrationTypeRepository.findAll();
    deleteRemovedPlugins(integrationTypes);
    unloadDisabledPlugins(integrationTypes);
  }

  //TODO this two methods could be moved to separate services (Single Responsibility violation)
  // + tests writing will be easier
  private void deleteRemovedPlugins(List<IntegrationType> integrationTypes) {

    LOGGER.debug("Unloading of removed plugins...");

    final Set<String> pluginIds = pluginBox.getPlugins().stream().map(Plugin::getId)
        .collect(Collectors.toSet());

    pluginIds.removeAll(
        integrationTypes.stream().map(IntegrationType::getName).collect(Collectors.toSet()));

    pluginIds.forEach(pluginId -> pluginBox.getPluginById(pluginId).ifPresent(plugin -> {
      if (!pluginBox.deletePlugin(plugin)) {
        LOGGER.error("Error has occurred during plugin file removing from the plugins directory");
      }
    }));

    LOGGER.debug("Unloading of removed plugins has finished...");
  }

  //TODO this two methods could be moved to separate services (Single Responsibility violation)
  // + tests writing will be easier
  private void unloadDisabledPlugins(List<IntegrationType> integrationTypes) {

    List<IntegrationType> disabledPlugins = integrationTypes.stream()
        .filter(it -> BooleanUtils.isFalse(it.isEnabled()))
        .collect(Collectors.toList());

    disabledPlugins.forEach(dp -> pluginBox.getPluginById(dp.getName()).ifPresent(plugin -> {
      if (pluginBox.unloadPlugin(plugin)) {
        LOGGER.debug(Suppliers.formattedSupplier("Plugin - '{}' has been successfully unloaded.",
            plugin.getPluginId()).get());
      } else {
        LOGGER.error(
            Suppliers.formattedSupplier("Error during unloading the plugin with id = '{}'.",
                plugin.getPluginId()).get());
      }
    }));
  }
}