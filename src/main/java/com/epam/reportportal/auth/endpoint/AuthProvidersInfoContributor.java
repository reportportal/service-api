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

package com.epam.reportportal.auth.endpoint;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

import com.epam.reportportal.auth.oauth.OAuthProvider;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.OAuthRegistrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.common.ExtensionPoint;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${rp.auth.saml.prefix}")
  private String samlPrefix;

  private final OAuthRegistrationRepository oAuthRegistrationRepository;
  private final Pf4jPluginBox pluginBox;
  private final Map<String, OAuthProvider> providersMap;

  @Autowired
  public AuthProvidersInfoContributor(OAuthRegistrationRepository oAuthRegistrationRepository,
      Pf4jPluginBox pluginBox,
      Map<String, OAuthProvider> providersMap) {
    this.oAuthRegistrationRepository = oAuthRegistrationRepository;
    this.pluginBox = pluginBox;
    this.providersMap = providersMap;
  }

  @Override
  public void contribute(Info.Builder builder) {
    final List<OAuthRegistration> oauth2Details = oAuthRegistrationRepository.findAll();

    final Map<String, Object> providers = providersMap.values()
        .stream()
        .filter(p -> !p.isConfigDynamic() || oauth2Details.stream()
            .anyMatch(it -> it.getId().equalsIgnoreCase(p.getName())))
        .collect(Collectors.toMap(OAuthProvider::getName,
            p -> new OAuthProviderInfo(p.getButton(), p.buildPath(getAuthBasePath()))
        ));

    // Dynamic providers from plugins
    pluginBox.getPlugins().stream()
        .filter(plugin -> ExtensionPoint.AUTH.equals(plugin.getType()))
        .forEach(plugin -> pluginBox.getInstance(plugin.getId(), AuthExtension.class)
            .flatMap(AuthExtension::getAuthProviderInfo)
            .ifPresent(info -> providers.put(plugin.getId(), info)));

    builder.withDetail("authExtensions", providers);
  }

  private String getAuthBasePath() {
    return fromCurrentContextPath().path(SSO_LOGIN_PATH).build().getPath();
  }

  @Getter
  public abstract static class AuthProviderInfo {

    private String button;

    public AuthProviderInfo(String button) {
      this.button = button;
    }

    public void setButton(String button) {
      this.button = button;
    }
  }

  public static class SamlProviderInfo extends AuthProviderInfo {

    private Map<String, String> providers;

    public SamlProviderInfo(String button, Map<String, String> providers) {
      super(button);
      this.providers = providers;
    }

    public Map<String, String> getProviders() {
      return providers;
    }

    public void setProviders(Map<String, String> providers) {
      this.providers = providers;
    }
  }

  public static class OAuthProviderInfo extends AuthProviderInfo {

    private String path;

    public OAuthProviderInfo(String button, String path) {
      super(button);
      this.path = path;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }

}
