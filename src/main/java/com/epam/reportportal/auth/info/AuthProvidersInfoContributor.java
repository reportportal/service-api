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

package com.epam.reportportal.auth.info;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

import com.epam.reportportal.auth.oauth.OAuthProvider;
import com.epam.reportportal.auth.store.MutableClientRegistrationRepository;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.common.ExtensionPoint;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * Shows list of supported authentication providers.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class AuthProvidersInfoContributor implements InfoContributor {

  public static final String SSO_LOGIN_PATH = "/oauth/login";

  private final MutableClientRegistrationRepository clientRegistrationRepository;
  private final Pf4jPluginBox pluginBox;
  private final Map<String, OAuthProvider> providersMap;

  @Autowired
  public AuthProvidersInfoContributor(MutableClientRegistrationRepository clientRegistrationRepository,
      Pf4jPluginBox pluginBox,
      Map<String, OAuthProvider> providersMap) {
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.pluginBox = pluginBox;
    this.providersMap = providersMap;
  }

  @Override
  public void contribute(Info.Builder builder) {
    final List<Integration> configuredOAuthProviders = clientRegistrationRepository.findAll();
    final Map<String, Object> extensions = providersMap.values()
        .stream()
        .filter(p -> !p.isConfigDynamic() || configuredOAuthProviders.stream()
            .anyMatch(it -> p.getName().equalsIgnoreCase(it.getType().getName())))
        .collect(Collectors.toMap(OAuthProvider::getName,
            p -> new OAuthProviderInfo(p.getButton(), p.buildPath(getAuthBasePath()))
        ));

    // Dynamic providers from plugins
    pluginBox.getPlugins().stream()
        .filter(plugin -> ExtensionPoint.AUTH.equals(plugin.getType()))
        .forEach(plugin -> pluginBox.getInstance(plugin.getId(), AuthExtension.class)
            .flatMap(AuthExtension::getAuthProviderInfo)
            .ifPresent(info -> extensions.put(plugin.getId(), info)));

    builder.withDetail("authExtensions", extensions);
  }

  private String getAuthBasePath() {
    return fromCurrentContextPath().path(SSO_LOGIN_PATH).build().getPath();
  }

}
