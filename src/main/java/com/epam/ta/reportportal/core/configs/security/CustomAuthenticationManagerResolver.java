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

@Slf4j
@Component
public class CustomAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

  private final AuthenticationManager apiKeyManager;
  private final JwtIssuerAuthenticationManagerResolver jwtResolver;

  public CustomAuthenticationManagerResolver(
      ApiKeyAuthenticationProvider apiKeyAuthenticationProvider,
      JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver
  ) {
    this.apiKeyManager = new ProviderManager(apiKeyAuthenticationProvider);
    this.jwtResolver = jwtIssuerAuthenticationManagerResolver;
  }

  @Override
  public AuthenticationManager resolve(HttpServletRequest request) {
    try {
      String token = getBearerValue(request);
      boolean isJwtToken = isJWT(request);
      
      log.debug("Resolving authentication manager for token: isJWT={}, tokenPrefix={}",
          isJwtToken, token.substring(0, Math.min(20, token.length())) + "...");
      
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

  private boolean isJWT(HttpServletRequest request) {
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