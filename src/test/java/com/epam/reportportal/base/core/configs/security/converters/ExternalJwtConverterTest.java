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

package com.epam.reportportal.base.core.configs.security.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import com.epam.reportportal.base.core.configs.security.JwtIssuer;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;

class ExternalJwtConverterTest {

  private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
  private final TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);

  private static Jwt adminClaimJwt() {
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject("user@corp.com")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .claim("authorities", List.of("ROLE_ADMINISTRATOR"))
        .build();
  }

  private Set<String> convertAuthorities(JwtIssuer issuer) {
    when(userDetailsService.loadUserByUsername("user@corp.com")).thenReturn(User.withUsername("user@corp.com")
        .password("pwd")
        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
        .build());
    var converter = new ExternalJwtConverter(userDetailsService, tokenBlacklistService, issuer);
    return converter.convert(adminClaimJwt()).getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
  }

  @Test
  void authoritiesClaimIsIgnoredWhenNotConfigured() {
    assertEquals(Set.of("ROLE_USER"), convertAuthorities(new JwtIssuer()));
  }

  @Test
  void authoritiesClaimIsUsedWhenConfigured() {
    var issuer = new JwtIssuer();
    issuer.setAuthoritiesClaim("authorities");

    assertEquals(Set.of("ROLE_ADMINISTRATOR"), convertAuthorities(issuer));
  }
}
