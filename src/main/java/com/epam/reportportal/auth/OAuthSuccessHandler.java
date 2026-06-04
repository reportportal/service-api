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

package com.epam.reportportal.auth;

import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.normalizeId;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.oauth.RPOAuth2User;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.inject.Provider;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Success handler for external oauth. Generates internal token for authenticated user to be used on UI/Agents side
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class OAuthSuccessHandler extends AuthSuccessHandler {

  @Autowired
  public OAuthSuccessHandler(Provider<TokenServicesFacade> tokenServicesFacade,
      ApplicationEventPublisher eventPublisher) {
    super(tokenServicesFacade, eventPublisher);
  }

  @Override
  protected Jwt getToken(Authentication authentication) {
    OAuth2AuthenticationToken oAuth2Authentication = ofNullable((OAuth2AuthenticationToken) authentication)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));
    var principal = oAuth2Authentication.getPrincipal();
    return tokenServicesFacade.get().createToken(
        ReportPortalClient.ui,
        normalizeId(principal.getName()),
        authentication,
        principal instanceof RPOAuth2User rpUser && rpUser.getAccessToken() != null
            ? Collections.singletonMap("upstream_token", rpUser.getAccessToken())
            : Collections.emptyMap()
    );
  }
}
