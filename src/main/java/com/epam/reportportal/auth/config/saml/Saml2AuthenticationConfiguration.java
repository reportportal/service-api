/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.auth.config.saml;

import com.epam.reportportal.auth.AuthFailureHandler;
import com.epam.reportportal.auth.integration.saml.ReportPortalSamlAuthenticationManager;
import com.epam.reportportal.auth.integration.saml.SamlAuthSuccessHandler;
import com.epam.reportportal.auth.integration.saml.SamlUserReplicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.Saml2WebSsoAuthenticationRequestFilter;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Configuration
public class Saml2AuthenticationConfiguration {

  private static final String SAML_PROCESSING_URL = "/login/saml2/sso/{registrationId}";

  private final SamlAuthSuccessHandler successHandler;

  private final AuthFailureHandler failureHandler;

  private final SamlUserReplicator samlUserReplicator;

  private final RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

  public Saml2AuthenticationConfiguration(SamlAuthSuccessHandler successHandler,
      AuthFailureHandler failureHandler, SamlUserReplicator samlUserReplicator,
      RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
    this.samlUserReplicator = samlUserReplicator;
    this.relyingPartyRegistrationRepository = relyingPartyRegistrationRepository;
  }

  @Bean
  @Order(4)
  public SecurityFilterChain samlSecurityFilterChain(HttpSecurity http) throws Exception {
    Saml2WebSsoAuthenticationFilter saml2Filter = new Saml2WebSsoAuthenticationFilter(
        relyingPartyRegistrationRepository,
        SAML_PROCESSING_URL
    );
    saml2Filter.setAuthenticationManager(new ReportPortalSamlAuthenticationManager(samlUserReplicator));
    saml2Filter.setAuthenticationSuccessHandler(successHandler);
    saml2Filter.setAuthenticationFailureHandler(failureHandler);

    Saml2RegistrationValidationFilter validationFilter =
        new Saml2RegistrationValidationFilter(relyingPartyRegistrationRepository, failureHandler);

    http
        .securityMatcher("/saml2/**", "/login/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/saml2/**").permitAll()
            .anyRequest().authenticated()
        )
        .saml2Login(Customizer.withDefaults())
        .addFilterBefore(validationFilter, Saml2WebSsoAuthenticationRequestFilter.class)
        .addFilterBefore(saml2Filter, Saml2WebSsoAuthenticationFilter.class)
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
