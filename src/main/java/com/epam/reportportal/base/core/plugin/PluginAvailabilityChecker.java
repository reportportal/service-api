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

package com.epam.reportportal.base.core.plugin;

import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginState;
import org.springframework.stereotype.Component;

/**
 * Generic check that a ReportPortal plugin identified by its PF4J plugin id is both loaded
 * (PluginState == STARTED) and enabled by an administrator (integration_type.enabled = true).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginAvailabilityChecker {

  private final Pf4jPluginBox pf4jPluginBox;
  private final IntegrationTypeRepository integrationTypeRepository;

  /**
   * @param pluginId PF4J plugin id (matches {@code integration_type.name})
   * @return {@code true} when the plugin is loaded, started and the integration type is enabled in
   * the database; {@code false} otherwise.
   */
  public boolean isAvailable(String pluginId) {
    boolean loaded = pf4jPluginBox.getPluginById(pluginId)
        .map(wrapper -> wrapper.getPluginState() == PluginState.STARTED)
        .orElse(false);
    if (!loaded) {
      log.debug("Plugin '{}' is not loaded or not in STARTED state", pluginId);
      return false;
    }

    boolean enabled = integrationTypeRepository.findByName(pluginId)
        .map(IntegrationType::isEnabled)
        .orElse(false);
    if (!enabled) {
      log.debug("Integration type '{}' is disabled or missing", pluginId);
    }
    return enabled;
  }
}
