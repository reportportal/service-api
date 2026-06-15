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

package com.epam.reportportal.base.core.configs.security.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

class RpJwtAuthenticationTokenTest {

  private static final UserDetails USER = User.withUsername("user@reportportal.io")
      .password("ignored")
      .authorities(new SimpleGrantedAuthority("ROLE_USER"))
      .build();

  private static Jwt jwt() {
    return Jwt.withTokenValue("token-value")
        .header("alg", "HS256")
        .claim("sub", "user@reportportal.io")
        .jti("jti-1")
        .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
        .expiresAt(Instant.parse("2026-01-02T00:00:00Z"))
        .build();
  }

  @Test
  @DisplayName("Should expose UserDetails as principal and Jwt via getToken()")
  void constructorShouldExposePrincipalAndToken() {
    // Given
    var jwt = jwt();
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    // When
    var token = new RpJwtAuthenticationToken(jwt, USER, authorities);

    // Then
    assertThat(token.getPrincipal()).isSameAs(USER);
    assertThat(token.getToken()).isSameAs(jwt);
    assertThat(token.getAuthorities()).containsExactlyElementsOf(authorities);
    assertThat(token.getName()).isEqualTo(USER.getUsername());
  }

  @Test
  @DisplayName("Should preserve Jwt after eraseCredentials() so logout flow can read jti/exp")
  void eraseCredentialsShouldNotNullifyTheJwt() {
    // Given
    var jwt = jwt();
    var token = new RpJwtAuthenticationToken(jwt, USER,
        List.of(new SimpleGrantedAuthority("ROLE_USER")));

    // When
    token.eraseCredentials();

    // Then
    assertThat(token.getToken()).isSameAs(jwt);
    assertThat(token.getToken().getId()).isEqualTo("jti-1");
    assertThat(token.getToken().getExpiresAt()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
  }

  @Test
  @DisplayName("Should treat tokens with same JWT value and user as equal")
  void equalsAndHashCodeShouldBeBasedOnTokenValueAndUser() {
    // Given
    var sharedJwt = jwt();
    var sameJwtAgain = Jwt.withTokenValue("token-value")
        .header("alg", "HS256")
        .claim("sub", "user@reportportal.io")
        .claims(claims -> claims.putAll(Map.of("foo", "bar")))
        .jti("jti-1")
        .build();
    var t1 = new RpJwtAuthenticationToken(sharedJwt, USER,
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
    var t2 = new RpJwtAuthenticationToken(sameJwtAgain, USER,
        List.of(new SimpleGrantedAuthority("ROLE_USER")));

    // Then
    assertThat(t1).isEqualTo(t2).hasSameHashCodeAs(t2);
  }
}
