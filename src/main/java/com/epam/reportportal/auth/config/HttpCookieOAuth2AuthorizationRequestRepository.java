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

package com.epam.reportportal.auth.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

/**
 * Cookie-based OAuth2AuthorizationRequestRepository for stateless deployments. Stores the authorization request in a
 * short-lived secure cookie instead of the HTTP session, so the callback can be handled by any pod in a cluster.
 */
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final Logger log = LoggerFactory.getLogger(
      HttpCookieOAuth2AuthorizationRequestRepository.class);

  private static final String COOKIE_NAME = "oauth2_auth_request";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return readCookie(request).orElse(null);
  }

  @Override
  public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request, HttpServletResponse response) {
    if (authorizationRequest == null) {
      deleteCookie(request, response);
      return;
    }
    try {
      String encoded = serialize(authorizationRequest);
      Cookie cookie = new Cookie(COOKIE_NAME, encoded);
      cookie.setPath("/");
      cookie.setHttpOnly(true);
      cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
      cookie.setSecure(request.isSecure());
      response.addCookie(cookie);
    } catch (IOException _) {
      log.error("Failed to save OAuth2 authorization request to cookie");
    }
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
      HttpServletResponse response) {
    OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
    if (authRequest != null) {
      deleteCookie(request, response);
    }
    return authRequest;
  }

  private Optional<OAuth2AuthorizationRequest> readCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(c -> COOKIE_NAME.equals(c.getName()))
        .findFirst()
        .filter(c -> StringUtils.hasText(c.getValue()))
        .map(c -> {
          try {
            return (OAuth2AuthorizationRequest) deserialize(c.getValue());
          } catch (IOException | ClassNotFoundException _) {
            log.warn("Failed to deserialize OAuth2 authorization request from cookie");
            return null;
          }
        });
  }

  private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
    if (request.getCookies() != null) {
      Arrays.stream(request.getCookies())
          .filter(c -> COOKIE_NAME.equals(c.getName()))
          .findFirst()
          .ifPresent(c -> {
            c.setValue("");
            c.setPath("/");
            c.setMaxAge(0);
            response.addCookie(c);
          });
    }
  }

  private static String serialize(Object object) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(object);
    }
    return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
  }

  private static Object deserialize(String base64) throws IOException, ClassNotFoundException {
    byte[] bytes = Base64.getUrlDecoder().decode(base64);
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return ois.readObject();
    }
  }
}
