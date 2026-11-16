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
import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.util.StringUtils;

/**
 * SAML Service Provider configuration. Creates an empty, reloadable RP registration repository
 * that is populated by the SAML plugin when installed.
 */
@Configuration
@EnableWebSecurity
public class SamlServiceProviderConfiguration {

  private static final String SECRET_KEY = "secret.key";
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final String AUTHN_REQUEST_KEY_CONTEXT = "rp-saml2-authn-request-v1";

  @Bean
  public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
    return new ReloadableRelyingPartyRegistrationRepository(Collections.emptyList());
  }

  @Bean
  public RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
      RelyingPartyRegistrationRepository registrationRepository) {
    return new DefaultRelyingPartyRegistrationResolver(registrationRepository);
  }

  /**
   * Stores the outgoing AuthnRequest in a cookie rather than the HTTP session, so the IdP response can be validated
   * ({@code InResponseTo}) by any service instance. Picked up by {@code saml2Login()} as a bean.
   *
   * <p>The cookie is signed with a key derived from the JWT signing key ({@code rp.jwt.signing-key}, or the
   * {@code secret.key} server setting when unset), which is shared by all instances.
   */
  @Bean
  public Saml2AuthenticationRequestRepository<AbstractSaml2AuthenticationRequest> saml2AuthenticationRequestRepository(
      RelyingPartyRegistrationRepository registrationRepository, ObjectMapper objectMapper,
      ServerSettingsRepository serverSettingsRepository,
      @Value("${rp.jwt.signing-key:}") String jwtSigningKey,
      @Value("${rp.auth.cookie.secure.enforce-https:true}") boolean enforceHttps) throws GeneralSecurityException {
    String masterSecret = StringUtils.hasText(jwtSigningKey)
        ? jwtSigningKey
        : serverSettingsRepository.findByKey(SECRET_KEY).map(ServerSettings::getValue)
            .orElseGet(serverSettingsRepository::generateSecret);
    return new HttpCookieSaml2AuthenticationRequestRepository(registrationRepository, objectMapper, enforceHttps,
        deriveKey(masterSecret));
  }

  /**
   * Derives a feature-specific key so the master secret is never used directly for this cookie.
   */
  private static byte[] deriveKey(String masterSecret) throws GeneralSecurityException {
    Mac mac = Mac.getInstance(HMAC_ALGORITHM);
    mac.init(new SecretKeySpec(masterSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
    return mac.doFinal(AUTHN_REQUEST_KEY_CONTEXT.getBytes(StandardCharsets.UTF_8));
  }
}
