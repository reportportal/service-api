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

package com.epam.reportportal.auth;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.inject.Provider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

class OAuthSuccessHandlerTest {

  private final TokenServicesFacade facade = mock(TokenServicesFacade.class);
  private final Provider<TokenServicesFacade> provider = () -> facade;
  private final OAuthSuccessHandler handler = new OAuthSuccessHandler(provider, mock(ApplicationEventPublisher.class));

  @Test
  void refusesPrincipalThatWasNotBuiltByAPluginUserService() {
    var foreign = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("sub", "admin"),
        "sub");
    var auth = new OAuth2AuthenticationToken(foreign, foreign.getAuthorities(), "rogue");

    assertThrows(ReportPortalException.class, () -> handler.getToken(auth));
    verifyNoInteractions(facade);
  }

  @Test
  void refusesNonOAuthAuthentication() {
    assertThrows(ReportPortalException.class,
        () -> handler.getToken(new UsernamePasswordAuthenticationToken("u", "p")));
    verifyNoInteractions(facade);
  }
}
