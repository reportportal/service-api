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

package com.epam.reportportal.auth.integration.ldap;

import static java.util.Collections.singletonList;

import com.epam.reportportal.auth.EnableableAuthProvider;
import com.epam.reportportal.auth.TokenServicesFacade;
import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.parameter.LdapParameter;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.infrastructure.persistence.commons.accessible.Accessible;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import java.util.Collections;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;

/**
 * Plain LDAP auth provider.
 *
 * @author Andrei Varabyeu
 */
public class LdapAuthProvider extends EnableableAuthProvider {

  //millis
  public static final String LDAP_TIMEOUT = "3000";
  private final DetailsContextMapper detailsContextMapper;
  private final FeatureFlagHandler featureFlagHandler;
  private final BasicTextEncryptor encryptor;

  public LdapAuthProvider(IntegrationRepository integrationRepository,
      ApplicationEventPublisher eventPublisher,
      DetailsContextMapper detailsContextMapper, TokenServicesFacade tokenService,
      FeatureFlagHandler featureFlagHandler, BasicTextEncryptor encryptor) {
    super(integrationRepository, eventPublisher, tokenService);
    this.detailsContextMapper = detailsContextMapper;
    this.featureFlagHandler = featureFlagHandler;
    this.encryptor = encryptor;
  }

  @Override
  protected boolean isEnabled() {
    return integrationRepository.findAllByTypeIn(AuthIntegrationType.LDAP.getName()).stream()
        .findFirst().isPresent();
  }

  @Override
  protected AuthenticationProvider getDelegate() {

    Integration integration = integrationRepository.findAllByTypeIn(
            AuthIntegrationType.LDAP.getName())
        .stream()
        .findFirst()
        .orElseThrow(() -> new BadCredentialsException("LDAP is not configured"));

    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(
        singletonList(LdapParameter.URL.getRequiredParameter(
            integration)), LdapParameter.BASE_DN.getRequiredParameter(integration));
    LdapParameter.MANAGER_PASSWORD.getParameter(integration)
        .ifPresent(it -> contextSource.setPassword(encryptor.decrypt(it)));
    LdapParameter.MANAGER_DN.getParameter(integration).ifPresent(contextSource::setUserDn);
    contextSource.setBaseEnvironmentProperties(
        Collections.singletonMap("com.sun.jndi.ldap.connect.timeout", LDAP_TIMEOUT));
    contextSource.afterPropertiesSet();

    LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> builder =
        new LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder>()
            .contextSource(contextSource)
            .ldapAuthoritiesPopulator(new NullLdapAuthoritiesPopulator())
            .userDetailsContextMapper(detailsContextMapper);

    /*
     * Basically, groups are not used
     */
    LdapParameter.GROUP_SEARCH_FILTER.getParameter(integration)
        .ifPresent(builder::groupSearchFilter);
    LdapParameter.GROUP_SEARCH_BASE.getParameter(integration).ifPresent(builder::groupSearchBase);
    LdapParameter.USER_SEARCH_FILTER.getParameter(integration).ifPresent(builder::userSearchFilter);

    //TODO: temporary solution for working with encoded passwords
    if (featureFlagHandler.isEnabled(FeatureFlag.DEFAULT_LDAP_ENCODER)) {
      LdapParameter.PASSWORD_ENCODER_TYPE.getParameter(integration).ifPresent(it -> {
        LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder>
            .PasswordCompareConfigurer passwordCompareConfigurer = builder.passwordCompare();
        LdapParameter.PASSWORD_ATTRIBUTE.getParameter(integration)
            .ifPresent(passwordCompareConfigurer::passwordAttribute);

        /*
         * DIRTY HACK. If LDAP password has salt, ldaptemplate.compare operation does not work
         * since we don't know server's salt.
         * To enable local password comparison, we need to provide password encoder from crypto's
         * package
         * This is why we just wrap old encoder with new one interface
         * New encoder cannot be used everywhere since it does not have implementation for LDAP
         */
        final PasswordEncoder delegate = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        builder.passwordEncoder(new org.springframework.security.crypto.password.PasswordEncoder() {

          @Override
          public String encode(CharSequence rawPassword) {
            return delegate.encode(rawPassword);
          }

          @Override
          public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return delegate.matches(rawPassword, encodedPassword);
          }
        });
      });
    }

    LdapParameter.USER_DN_PATTERN.getParameter(integration).ifPresent(builder::userDnPatterns);

    try {
      return (AuthenticationProvider) Accessible.on(builder)
          .method(LdapAuthenticationProviderConfigurer.class.getDeclaredMethod("build"))
          .invoke();
    } catch (Throwable e) {
      throw new ReportPortalException("Cannot build LDAP auth provider", e);
    }
  }

}
