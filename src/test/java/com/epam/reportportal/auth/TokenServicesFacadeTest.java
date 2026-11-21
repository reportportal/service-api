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

package com.epam.reportportal.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class TokenServicesFacadeTest {

  @Test
  void tokenLifetimeFollowsConfiguredValidityPeriod() {
    JwtEncoder encoder = mock(JwtEncoder.class);
    when(encoder.encode(any())).thenAnswer(inv -> {
      JwtEncoderParameters params = inv.getArgument(0);
      return new Jwt("t", params.getClaims().getIssuedAt(), params.getClaims().getExpiresAt(),
          params.getJwsHeader().getHeaders(), params.getClaims().getClaims());
    });
    TokenServicesFacade facade = new TokenServicesFacade(encoder, "issuer", 3600L);

    Jwt jwt = facade.createToken("ui", "user", List.of(new SimpleGrantedAuthority("ROLE_USER")), null);

    assertEquals(Duration.ofSeconds(3600), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
  }
}
