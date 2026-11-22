/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.configs.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class MultiIdentityProviderConfigTest {

  private static final String ISSUER = "https://idp.example.com";

  private static Jwt jwt(String issuer, List<String> audience) {
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject("user")
        .issuer(issuer)
        .audience(audience)
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .build();
  }

  private static JwtIssuer provider(String audience) {
    var config = new JwtIssuer();
    config.setIssuerUri(ISSUER);
    config.setAudience(audience);
    return config;
  }

  @Test
  void tokenForAnotherAudienceIsRejected() {
    var validator = MultiIdentityProviderConfig.createJwtValidator(provider("reportportal"));

    assertTrue(validator.validate(jwt(ISSUER, List.of("other-app"))).hasErrors());
    assertFalse(validator.validate(jwt(ISSUER, List.of("other-app", "reportportal"))).hasErrors());
  }

  @Test
  void audienceIsNotCheckedWhenNotConfigured() {
    var validator = MultiIdentityProviderConfig.createJwtValidator(provider(null));

    assertFalse(validator.validate(jwt(ISSUER, List.of("other-app"))).hasErrors());
  }

  @Test
  void tokenFromAnotherIssuerIsRejected() {
    var validator = MultiIdentityProviderConfig.createJwtValidator(provider(null));

    assertTrue(validator.validate(jwt("https://evil.example.com", List.of("reportportal"))).hasErrors());
  }
}
