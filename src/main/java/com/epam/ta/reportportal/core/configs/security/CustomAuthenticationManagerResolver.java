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

package com.epam.ta.reportportal.core.configs.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.stereotype.Component;

/**
 * Custom authentication manager resolver that selects between API key and JWT authentication
 * based on the request's Authorization header.
 *
 * <p>This resolver checks if the token is a JWT or an API key and returns the appropriate AuthenticationManager.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Slf4j
@Component
public class CustomAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

  private final AuthenticationManager apiKeyManager;
  private final JwtIssuerAuthenticationManagerResolver jwtResolver;

  /**
   * Constructs a CustomAuthenticationManagerResolver with the provided API key authentication provider
   * and JWT issuer authentication manager resolver.
   *
   * @param apiKeyAuthenticationProvider The provider for API key authentication.
   * @param jwtIssuerAuthenticationManagerResolver The resolver for JWT issuer authentication.
   */
  public CustomAuthenticationManagerResolver(
      ApiKeyAuthenticationProvider apiKeyAuthenticationProvider,
      JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver
  ) {
    this.apiKeyManager = new ProviderManager(apiKeyAuthenticationProvider);
    this.jwtResolver = jwtIssuerAuthenticationManagerResolver;
  }

  /**
   * Resolves the appropriate AuthenticationManager based on the request's Authorization header.
   * If the token is a JWT, it uses the JWT resolver; otherwise, it uses the API key manager.
   *
   * @param request The HTTP request containing the Authorization header.
   * @return The resolved AuthenticationManager.
   */
  @Override
  public AuthenticationManager resolve(HttpServletRequest request) {
    try {
      boolean isJwtToken = isJwt(request);

      if (isJwtToken) {
        AuthenticationManager manager = jwtResolver.resolve(request);
        log.debug("Selected JWT manager: {}", manager != null ? manager.getClass().getSimpleName() : "null");
        return manager;
      } else {
        log.debug("Selected API key manager");
        return apiKeyManager;
      }
    } catch (Exception e) {
      log.error("Error resolving authentication manager", e);
      throw e;
    }
  }

  private boolean isJwt(HttpServletRequest request) {
    try {
      var token = getBearerValue(request);
      var parts = token.split("\\.");
      
      log.debug("Token analysis: parts count={}", parts.length);
      
      if (parts.length != 3) {
        log.debug("Not a JWT: wrong parts count");
        return false;
      }
      
      try {
        java.util.Base64.getUrlDecoder().decode(parts[1]);
        log.debug("Valid JWT format detected");
        return true;
      } catch (Exception e) {
        log.debug("Not a JWT: base64 decode failed - {}", e.getMessage());
        return false;
      }
    } catch (Exception e) {
      log.debug("Not a JWT: error getting bearer token - {}", e.getMessage());
      return false;
    }
  }

  private String getBearerValue(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    log.debug("Authorization header present: {}", authHeader != null);
    
    if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
      String token = authHeader.substring(7);
      log.debug("Bearer token extracted, length: {}", token.length());
      return token;
    }
    
    log.debug("No Bearer token found in Authorization header");
    throw new IllegalArgumentException("No Bearer token found");
  }
}