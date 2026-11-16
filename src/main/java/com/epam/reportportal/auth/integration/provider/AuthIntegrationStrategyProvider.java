/*
 * Copyright 2021 EPAM Systems
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

package com.epam.reportportal.auth.integration.provider;

import com.epam.reportportal.auth.integration.handler.impl.strategy.AuthIntegrationStrategy;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.common.ExtensionPoint;
import java.util.Optional;

public class AuthIntegrationStrategyProvider {

  private final Pf4jPluginBox pluginBox;

  public AuthIntegrationStrategyProvider(Pf4jPluginBox pluginBox) {
    this.pluginBox = pluginBox;
  }

  public Optional<AuthIntegrationStrategy> provide(String type) {
    return pluginBox.getPlugins().stream()
        .filter(p -> ExtensionPoint.AUTH.equals(p.getType()))
        .map(p -> pluginBox.getInstance(p.getId(), AuthExtension.class))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(ext -> ext.getAuthIntegrationType().map(type::equals).orElse(false))
        .findFirst()
        .flatMap(AuthExtension::getStrategy);
  }
}
