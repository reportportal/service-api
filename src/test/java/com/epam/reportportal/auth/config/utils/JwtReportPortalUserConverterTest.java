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

package com.epam.reportportal.auth.config.utils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtReportPortalUserConverterTest {

  private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
  private final TokenBlacklistService blacklist = mock(TokenBlacklistService.class);
  private final JwtReportPortalUserConverter converter = new JwtReportPortalUserConverter(userDetailsService,
      blacklist);

  private static Jwt jwt() {
    return Jwt.withTokenValue("token")
        .header("alg", "HS256")
        .subject("user")
        .claim("user_name", "user")
        .claim("authorities", List.of("ROLE_USER"))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .build();
  }

  @Test
  void enabledUserIsAccepted() {
    var user = User.withUsername("user").password("p").authorities("ROLE_USER").build();
    when(userDetailsService.loadUserByUsername("user")).thenReturn(user);

    assertSame(user, converter.convert(jwt()).getPrincipal());
  }

  @Test
  void disabledUserIsRejected() {
    when(userDetailsService.loadUserByUsername("user")).thenReturn(
        User.withUsername("user").password("p").authorities("ROLE_USER").disabled(true).build());

    assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(jwt()));
  }
}
