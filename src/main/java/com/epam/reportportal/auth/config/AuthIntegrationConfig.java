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

import static com.epam.reportportal.auth.integration.AuthIntegrationType.LDAP;
import static com.epam.reportportal.auth.integration.AuthIntegrationType.SAML;

import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationStrategy;
import com.epam.reportportal.auth.integration.handler.impl.GetLdapStrategy;
import com.epam.reportportal.auth.integration.handler.impl.GetSamlIntegrationsStrategy;
import com.epam.reportportal.auth.integration.handler.impl.strategy.AuthIntegrationStrategy;
import com.epam.reportportal.auth.integration.handler.impl.strategy.LdapIntegrationStrategy;
import com.epam.reportportal.auth.integration.handler.impl.strategy.SamlIntegrationStrategy;
import com.epam.reportportal.auth.integration.provider.AuthIntegrationStrategyProvider;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class AuthIntegrationConfig {

  private ApplicationContext applicationContext;

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Bean("getAuthIntegrationStrategyMapping")
  public Map<AuthIntegrationType, GetAuthIntegrationStrategy> getAuthIntegrationStrategyMapping() {
    return new ImmutableMap.Builder<AuthIntegrationType, GetAuthIntegrationStrategy>()
        .put(LDAP, applicationContext.getBean(GetLdapStrategy.class))
        .put(SAML, applicationContext.getBean(GetSamlIntegrationsStrategy.class))
        .build();
  }

  @Bean("authIntegrationStrategyProvider")
  public AuthIntegrationStrategyProvider authIntegrationStrategyProvider() {
    final ImmutableMap<AuthIntegrationType, AuthIntegrationStrategy> authIntegrationStrategyMap =
        buildAuthIntegrationStrategyMap();
    return new AuthIntegrationStrategyProvider(authIntegrationStrategyMap);
  }

  private ImmutableMap<AuthIntegrationType, AuthIntegrationStrategy> buildAuthIntegrationStrategyMap() {
    return new ImmutableMap.Builder<AuthIntegrationType, AuthIntegrationStrategy>()
        .put(LDAP, applicationContext.getBean(LdapIntegrationStrategy.class))
        .put(SAML, applicationContext.getBean(SamlIntegrationStrategy.class))
        .build();
  }
}
