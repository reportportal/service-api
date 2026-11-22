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

import com.epam.reportportal.auth.store.OAuthRegistrationResourceRepository;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

class DelegatingPluginOAuth2UserServiceTest {

  @Test
  void failsClosedWhenNoPluginHandlesTheRegistration() {
    var service = new DelegatingPluginOAuth2UserService(mock(Pf4jPluginBox.class),
        mock(OAuthRegistrationResourceRepository.class));
    var registration = ClientRegistration.withRegistrationId("rogue")
        .clientId("id")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://localhost/cb")
        .authorizationUri("http://idp/auth")
        .tokenUri("http://idp/token")
        .userInfoUri("http://idp/userinfo")
        .userNameAttributeName("sub")
        .build();
    var token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "t", Instant.now(),
        Instant.now().plusSeconds(60));

    assertThrows(OAuth2AuthenticationException.class,
        () -> service.loadUser(new OAuth2UserRequest(registration, token)));
  }
}
