/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.auth.config.saml;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Forwards legacy SAML ACS POST requests from the old spring-security-saml URL format
 * ({@code /saml/sp/SSO/alias/{registrationId}}) to the Spring Security SAML2 format
 * ({@code /login/saml2/sso/{registrationId}}).
 *
 * <p>A server-side forward is used rather than a redirect so that the POST body
 * (containing the {@code SAMLResponse} parameter) is preserved.
 */
public class LegacySamlAcsForwardFilter extends OncePerRequestFilter {

  private static final PathPattern LEGACY_ACS_PATTERN =
      new PathPatternParser().parse("/saml/sp/SSO/alias/{registrationId}");

  private static final String NEW_ACS_URL_TEMPLATE = "/login/saml2/sso/%s";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (!HttpMethod.POST.matches(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    var pathContainer = org.springframework.http.server.PathContainer.parsePath(request.getRequestURI());
    var matchInfo = LEGACY_ACS_PATTERN.matchAndExtract(pathContainer);

    if (matchInfo == null) {
      filterChain.doFilter(request, response);
      return;
    }

    String registrationId = matchInfo.getUriVariables().get("registrationId");
    String forwardUrl = String.format(NEW_ACS_URL_TEMPLATE, registrationId);
    request.getRequestDispatcher(forwardUrl).forward(request, response);
  }
}
