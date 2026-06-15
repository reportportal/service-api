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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class ReportPortalJwtConverterTest {

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private TokenBlacklistService tokenBlacklistService;

  private ReportPortalJwtConverter converter;

  @BeforeEach
  void setUp() {
    converter = new ReportPortalJwtConverter(userDetailsService, tokenBlacklistService);
  }

  private static Jwt jwt(String jti, String subject) {
    return Jwt.withTokenValue("token")
        .header("alg", "HS256")
        .subject(subject)
        .jti(jti)
        .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
        .expiresAt(Instant.parse("2026-01-02T00:00:00Z"))
        .claim("authorities", java.util.List.of("ROLE_USER"))
        .build();
  }

  @Test
  @DisplayName("Should produce RpJwtAuthenticationToken with UserDetails as principal and Jwt accessible")
  void convertWhenValidJwtShouldReturnRpJwtAuthenticationToken() {
    // Given
    var jwt = jwt("jti-1", "user@reportportal.io");
    UserDetails user = User.withUsername("user@reportportal.io")
        .password("pwd")
        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
        .build();
    when(tokenBlacklistService.isRevoked("jti-1", "user@reportportal.io",
        Instant.parse("2026-01-01T00:00:00Z"))).thenReturn(false);
    when(userDetailsService.loadUserByUsername("user@reportportal.io")).thenReturn(user);

    // When
    var auth = converter.convert(jwt);

    // Then
    assertThat(auth).isInstanceOf(RpJwtAuthenticationToken.class);
    var rpAuth = (RpJwtAuthenticationToken) auth;
    assertThat(rpAuth.getPrincipal()).isSameAs(user);
    assertThat(rpAuth.getToken()).isSameAs(jwt);
    assertThat(rpAuth.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("Should throw OAuth2AuthenticationException when token is revoked")
  void convertWhenTokenRevokedShouldThrow() {
    // Given
    var jwt = jwt("jti-revoked", "user@reportportal.io");
    when(tokenBlacklistService.isRevoked("jti-revoked", "user@reportportal.io",
        Instant.parse("2026-01-01T00:00:00Z"))).thenReturn(true);

    // Then
    assertThatThrownBy(() -> converter.convert(jwt))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessageContaining("revoked");
  }
}
