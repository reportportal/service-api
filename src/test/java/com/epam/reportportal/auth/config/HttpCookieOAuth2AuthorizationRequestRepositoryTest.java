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

package com.epam.reportportal.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import tools.jackson.databind.json.JsonMapper;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

  private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
  private static final byte[] OTHER_KEY = "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.UTF_8);
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  private final JsonMapper mapper = JsonMapper.builder().build();

  private HttpCookieOAuth2AuthorizationRequestRepository repo(byte[] key, Instant now) {
    return new HttpCookieOAuth2AuthorizationRequestRepository(mapper, true, key, Clock.fixed(now, ZoneOffset.UTC));
  }

  private static OAuth2AuthorizationRequest authRequest() {
    return OAuth2AuthorizationRequest.authorizationCode()
        .authorizationUri("http://idp/auth")
        .clientId("client")
        .redirectUri("http://rp/cb")
        .state("state-1")
        .attributes(Map.of("nonce", "n-1", "registration_id", "gh"))
        .build();
  }

  private static String saved(HttpCookieOAuth2AuthorizationRequestRepository repo) {
    var response = new MockHttpServletResponse();
    repo.saveAuthorizationRequest(authRequest(), new MockHttpServletRequest(), response);
    return response.getCookie("oauth2_auth_request").getValue();
  }

  private static MockHttpServletRequest withCookie(String value) {
    var request = new MockHttpServletRequest();
    request.setCookies(new Cookie("oauth2_auth_request", value));
    return request;
  }

  @Test
  void roundTripsAValidCookie() {
    var repo = repo(KEY, NOW);

    var loaded = repo.loadAuthorizationRequest(withCookie(saved(repo)));

    assertNotNull(loaded);
    assertEquals("state-1", loaded.getState());
    assertEquals("n-1", loaded.getAttribute("nonce"));
  }

  @Test
  void unsignedLegacyCookieIsTreatedAsAbsent() {
    var legacy = Base64.getUrlEncoder().encodeToString(("{\"grantType\":\"authorization_code\","
        + "\"authorizationUri\":\"http://evil/auth\",\"clientId\":\"c\",\"redirectUri\":\"http://evil\","
        + "\"state\":\"planted\"}").getBytes(StandardCharsets.UTF_8));

    assertNull(repo(KEY, NOW).loadAuthorizationRequest(withCookie(legacy)));
  }

  @Test
  void tamperedPayloadIsTreatedAsAbsent() {
    var repo = repo(KEY, NOW);
    String[] parts = saved(repo).split("\\.");
    String forged = Base64.getUrlEncoder().withoutPadding().encodeToString(
        new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8)
            .replace("state-1", "planted").getBytes(StandardCharsets.UTF_8));

    assertNull(repo.loadAuthorizationRequest(withCookie(forged + "." + parts[1])));
  }

  @Test
  void cookieSignedWithAnotherKeyIsTreatedAsAbsent() {
    assertNull(repo(OTHER_KEY, NOW).loadAuthorizationRequest(withCookie(saved(repo(KEY, NOW)))));
  }

  @Test
  void expiredCookieIsTreatedAsAbsent() {
    String cookie = saved(repo(KEY, NOW));

    assertNull(repo(KEY, NOW.plusSeconds(181)).loadAuthorizationRequest(withCookie(cookie)));
  }

  @Test
  void garbageCookieIsTreatedAsAbsent() {
    assertNull(repo(KEY, NOW).loadAuthorizationRequest(withCookie("not-a-cookie")));
    assertNull(repo(KEY, NOW).loadAuthorizationRequest(withCookie("a.b")));
  }

  @Test
  void shortKeyIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> repo(new byte[8], NOW));
  }
}
