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

package com.epam.reportportal.auth.config;

import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationStrategy;
import com.epam.reportportal.auth.integration.handler.impl.strategy.AuthIntegrationStrategy;
import com.epam.reportportal.auth.integration.provider.AuthIntegrationStrategyProvider;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.common.ExtensionPoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers auth integration strategies from installed plugins.
 */
@Configuration
public class AuthIntegrationConfig {

  @Autowired
  private Pf4jPluginBox pluginBox;

  @Bean("getAuthIntegrationStrategyMapping")
  public Map<String, GetAuthIntegrationStrategy> getAuthIntegrationStrategyMapping() {
    Map<String, GetAuthIntegrationStrategy> mapping = new HashMap<>();
    getAuthExtensions().forEach(ext ->
        ext.getAuthIntegrationType().ifPresent(type ->
            ext.getListIntegrationStrategy().ifPresent(s -> mapping.put(type, s))
        )
    );
    return mapping;
  }

  @Bean("authIntegrationStrategyProvider")
  public AuthIntegrationStrategyProvider authIntegrationStrategyProvider() {
    Map<String, AuthIntegrationStrategy> map = new HashMap<>();
    getAuthExtensions().forEach(ext ->
        ext.getAuthIntegrationType().ifPresent(type ->
            ext.getStrategy().ifPresent(s -> map.put(type, s))
        )
    );
    return new AuthIntegrationStrategyProvider(map);
  }

  private List<AuthExtension> getAuthExtensions() {
    return pluginBox.getPlugins().stream()
        .filter(p -> ExtensionPoint.AUTH.equals(p.getType()))
        .map(p -> pluginBox.getInstance(p.getId(), AuthExtension.class))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
