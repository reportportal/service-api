/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.auth.integration.saml;

import com.epam.reportportal.auth.AuthSuccessHandler;
import com.epam.reportportal.auth.ReportPortalClient;
import com.epam.reportportal.auth.TokenServicesFacade;
import jakarta.inject.Provider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Component;

/**
 * Used for handling successful authentication in SAML process.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Component
public class SamlAuthSuccessHandler extends AuthSuccessHandler {

  public SamlAuthSuccessHandler(Provider<TokenServicesFacade> tokenServicesFacade,
      ApplicationEventPublisher eventPublisher) {
    super(tokenServicesFacade, eventPublisher);
  }


  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication)
      throws IOException, ServletException {
    super.onAuthenticationSuccess(request, response, authentication);
  }

  @Override
  protected Jwt getToken(Authentication authentication) {
    Saml2Authentication samlAuthentication =
        (Saml2Authentication) authentication;

    return tokenServicesFacade.get()
        .createToken(ReportPortalClient.ui, samlAuthentication.getName(), samlAuthentication,
            Collections.emptyMap());
  }
}
