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

package com.epam.ta.reportportal.core.configs.utils;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.configs.security.ApiKeyAuthenticationProvider;
import com.epam.ta.reportportal.core.configs.security.JwtCustomAuthenticationProvider;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

public class CustomAuthenticationManagerResolver implements
    AuthenticationManagerResolver<HttpServletRequest> {

  private final ApiKeyAuthenticationProvider apiKeyAuthenticationProvider;

  private final JwtCustomAuthenticationProvider jwtCustomAuthenticationProvider;

  private final JwtDecoder jwtDecoder;

  public CustomAuthenticationManagerResolver(
      ApiKeyAuthenticationProvider apiKeyAuthenticationProvider,
      JwtCustomAuthenticationProvider jwtCustomAuthenticationProvider, JwtDecoder jwtDecoder) {
    this.apiKeyAuthenticationProvider = apiKeyAuthenticationProvider;
    this.jwtCustomAuthenticationProvider = jwtCustomAuthenticationProvider;
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  public AuthenticationManager resolve(HttpServletRequest request) {
    if (isJwt(request)) {
      return new ProviderManager(jwtCustomAuthenticationProvider);
    } else {
      return new ProviderManager(apiKeyAuthenticationProvider);
    }
  }

  private boolean isJwt(HttpServletRequest request) {
    boolean isJwt;
    try {
      jwtDecoder.decode(getBearerValue(request));
      isJwt = true;
    } catch (Exception e) {
      isJwt = false;
    }
    return isJwt;
  }

  private static String getBearerValue(HttpServletRequest request) {
    String authHeader = request.getHeader(AUTHORIZATION);
    if (authHeader != null && authHeader.toLowerCase().startsWith("bearer")) {
      String token = authHeader.substring(7);
      if (StringUtils.isEmpty(token)) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Bearer token");
      }
      return token;
    }
    throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Bearer token");

  }

}
