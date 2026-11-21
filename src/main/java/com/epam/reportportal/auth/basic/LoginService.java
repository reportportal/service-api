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

import com.epam.reportportal.auth.ReportPortalClient;
import com.epam.reportportal.auth.TokenServicesFacade;
import com.epam.reportportal.auth.model.AccessTokenRs;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Authenticates username/password logins (including LDAP, delegated via plugins) and mints a bearer access token.
 */
@Service
@RequiredArgsConstructor
public class LoginService {

  private final AuthenticationManager authenticationManager;
  private final TokenServicesFacade tokenServicesFacade;

  /**
   * Authenticates the given credentials and mints a bearer access token for the resulting principal.
   */
  public AccessTokenRs login(String username, String password) {
    Authentication authResult = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password));

    Jwt jwt = tokenServicesFacade.createToken(ReportPortalClient.ui, authResult.getName(), authResult, Map.of());

    return new AccessTokenRs(
        jwt.getTokenValue(),
        OAuth2AccessToken.TokenType.BEARER.getValue(),
        expiresInSeconds(jwt));
  }

  private static long expiresInSeconds(Jwt jwt) {
    Instant issuedAt = Objects.requireNonNull(jwt.getIssuedAt(), "JWT is missing issuedAt claim");
    Instant expiresAt = Objects.requireNonNull(jwt.getExpiresAt(), "JWT is missing expiresAt claim");
    return Duration.between(issuedAt, expiresAt).toSeconds();
  }
}
