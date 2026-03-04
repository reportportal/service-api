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

import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.parameter.SamlParameter;
import com.epam.reportportal.auth.oauth.OAuthProvider;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.OAuthRegistrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Shows list of supported authentication providers.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class AuthProvidersInfoContributor implements InfoContributor {

  public static final String SSO_LOGIN_PATH = "/oauth/login";

  private static final String SAML_BUTTON = "<span>Login with SAML</span>";

  @Value("${rp.auth.saml.prefix}")
  private String samlPrefix;

  private final OAuthRegistrationRepository oAuthRegistrationRepository;
  private final IntegrationRepository integrationRepository;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final Map<String, OAuthProvider> providersMap;

  @Autowired
  public AuthProvidersInfoContributor(OAuthRegistrationRepository oAuthRegistrationRepository,
      IntegrationRepository integrationRepository,
      IntegrationTypeRepository integrationTypeRepository,
      Map<String, OAuthProvider> providersMap) {
    this.oAuthRegistrationRepository = oAuthRegistrationRepository;
    this.integrationRepository = integrationRepository;
    this.integrationTypeRepository = integrationTypeRepository;
    this.providersMap = providersMap;
  }

  @Override
  public void contribute(Info.Builder builder) {
    final List<OAuthRegistration> oauth2Details = oAuthRegistrationRepository.findAll();

    final Map<String, AuthProviderInfo> providers = providersMap.values()
        .stream()
        .filter(p -> !p.isConfigDynamic() || oauth2Details.stream()
            .anyMatch(it -> it.getId().equalsIgnoreCase(p.getName())))
        .collect(Collectors.toMap(OAuthProvider::getName,
            p -> new OAuthProviderInfo(p.getButton(), p.buildPath(getAuthBasePath()))
        ));

    Optional<IntegrationType> samlIntegrationType = integrationTypeRepository.findByName(
        AuthIntegrationType.SAML.getName());

    Map<String, String> samlProviders = Maps.newHashMap();

    if (samlIntegrationType.isPresent()) {
      samlProviders = integrationRepository.findAllGlobalByType(samlIntegrationType.get())
          .stream()
          .filter(Integration::isEnabled)
          .filter(it -> SamlParameter.IDP_URL.getParameter(it).isPresent())
          .collect(Collectors.toMap(
              Integration::getName,
              it -> fromCurrentContextPath().path(String.format("/saml2/authenticate/%s",
                  SamlParameter.IDP_NAME.getParameter(it).get()
              )).build().getPath()
          ));
    }

    if (!CollectionUtils.isEmpty(samlProviders)) {
      providers.put("samlProviders", new SamlProviderInfo(SAML_BUTTON, samlProviders));
    }

    builder.withDetail("authExtensions", providers);
  }

  private String getAuthBasePath() {
    return fromCurrentContextPath().path(SSO_LOGIN_PATH).build().getPath();
  }

  public abstract static class AuthProviderInfo {

    private String button;

    public AuthProviderInfo(String button) {
      this.button = button;
    }

    public String getButton() {
      return button;
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
