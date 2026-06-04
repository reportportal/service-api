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
package com.epam.reportportal.auth.config.saml;

import com.epam.reportportal.auth.integration.saml.ReloadableRelyingPartyRegistrationRepository;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;

/**
 * SAML Service Provider configuration. Creates an empty, reloadable RP registration repository
 * that is populated by the SAML plugin when installed.
 */
@Configuration
@EnableWebSecurity
public class SamlServiceProviderConfiguration {

  @Bean
  public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
    return new ReloadableRelyingPartyRegistrationRepository(Collections.emptyList());
  }

  @Bean
  public RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
      RelyingPartyRegistrationRepository registrationRepository) {
    return new DefaultRelyingPartyRegistrationResolver(registrationRepository);
  }
}
