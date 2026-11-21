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

package com.epam.reportportal.auth.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.auth.ReportPortalClient;
import com.epam.reportportal.auth.TokenServicesFacade;
import com.epam.reportportal.auth.model.AccessTokenRs;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private TokenServicesFacade tokenServicesFacade;

  @InjectMocks
  private LoginService loginService;

  @Test
  void loginWhenValidCredentialsShouldReturnBearerTokenWithExpiresIn() {
    // Given
    Authentication authResult = new UsernamePasswordAuthenticationToken("default", "1q2w3e");
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willReturn(authResult);

    Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
    Instant expiresAt = issuedAt.plusSeconds(86400);
    Jwt jwt = Jwt.withTokenValue("jwt-token-value")
        .header("alg", "HS256")
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .claim("sub", "default")
        .build();
    given(tokenServicesFacade.createToken(ReportPortalClient.ui, "default", authResult, Map.of()))
        .willReturn(jwt);

    // When
    AccessTokenRs result = loginService.login("default", "1q2w3e");

    // Then
    assertEquals("jwt-token-value", result.accessToken());
    assertEquals("Bearer", result.tokenType());
    assertEquals(86400L, result.expiresIn());
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void loginWhenAuthenticationFailsShouldPropagateException() {
    // Given
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new BadCredentialsException("Bad credentials"));

    // When
    BadCredentialsException thrown = assertThrows(BadCredentialsException.class,
        () -> loginService.login("default", "wrong"));

    // Then
    assertEquals("Bad credentials", thrown.getMessage());
  }
}
