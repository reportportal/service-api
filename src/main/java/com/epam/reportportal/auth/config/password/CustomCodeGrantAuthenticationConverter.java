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
package com.epam.reportportal.auth.config.password;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class CustomCodeGrantAuthenticationConverter implements AuthenticationConverter {

  @Nullable
  @Override
  public Authentication convert(HttpServletRequest request) {
    String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
    if (!"password".equals(grantType)) {
      return null;
    }
    Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

    ClientToken usernamePasswordAuthenticationToken = new ClientToken(
        request.getParameter(OAuth2ParameterNames.USERNAME), request.getParameter(OAuth2ParameterNames.PASSWORD),
        clientPrincipal.getAuthorities());
    usernamePasswordAuthenticationToken.setClientPrincipal(clientPrincipal);

    return usernamePasswordAuthenticationToken;
  }
}
