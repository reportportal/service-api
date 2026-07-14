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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

/**
 * Cookie-based OAuth2AuthorizationRequestRepository for stateless deployments. Stores the authorization request in a
 * short-lived secure cookie instead of the HTTP session, so the callback can be handled by any pod in a cluster.
 */
@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2_auth_request";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  private final ObjectMapper objectMapper;
  private final boolean enforceHttps;
  private final AtomicBoolean secureWarningLogged = new AtomicBoolean(false);

  public HttpCookieOAuth2AuthorizationRequestRepository(ObjectMapper objectMapper, boolean enforceHttps) {
    this.objectMapper = objectMapper;
    this.enforceHttps = enforceHttps;
  }

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
    if (enforceHttps && !request.isSecure() && secureWarningLogged.compareAndSet(false, true)) {
      log.warn("rp.auth.cookie.secure.enforce-https is true but the request is not secure. "
          + "Ensure your reverse proxy forwards X-Forwarded-Proto: https.");
    }
    try {
      Cookie cookie = new Cookie(COOKIE_NAME, serialize(authorizationRequest));
      cookie.setPath("/");
      cookie.setHttpOnly(true);
      cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
      cookie.setSecure(enforceHttps || request.isSecure());
      cookie.setAttribute("SameSite", "Lax");
      response.addCookie(cookie);
    } catch (IOException _) {
      log.error("Failed to save OAuth2 authorization request to cookie");
    }
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
      HttpServletResponse response) {
    OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
    deleteCookie(request, response);
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
            return deserialize(c.getValue());
          } catch (IOException _) {
            log.warn("Failed to deserialize OAuth2 authorization request from cookie");
            return null;
          }
        });
  }

  private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie deletion = new Cookie(COOKIE_NAME, "");
    deletion.setPath("/");
    deletion.setHttpOnly(true);
    deletion.setMaxAge(0);
    deletion.setSecure(enforceHttps || request.isSecure());
    deletion.setAttribute("SameSite", "Lax");
    response.addCookie(deletion);
  }

  private String serialize(OAuth2AuthorizationRequest authorizationRequest) throws IOException {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("grantType", authorizationRequest.getGrantType().getValue());
    data.put("authorizationUri", authorizationRequest.getAuthorizationUri());
    data.put("clientId", authorizationRequest.getClientId());
    data.put("redirectUri", authorizationRequest.getRedirectUri());
    data.put("scopes", authorizationRequest.getScopes());
    data.put("state", authorizationRequest.getState());
    data.put("additionalParameters", authorizationRequest.getAdditionalParameters());
    data.put("attributes", authorizationRequest.getAttributes());
    return Base64.getUrlEncoder().encodeToString(objectMapper.writeValueAsBytes(data));
  }

  @SuppressWarnings("unchecked")
  private OAuth2AuthorizationRequest deserialize(String base64) throws IOException {
    Map<String, Object> data = objectMapper.readValue(
        Base64.getUrlDecoder().decode(base64),
        new TypeReference<Map<String, Object>>() {
        });

    String grantType = (String) data.get("grantType");
    if (!AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(grantType)) {
      throw new IOException("Unsupported OAuth2 grant type: " + grantType);
    }

    List<?> rawScopes = (List<?>) data.get("scopes");
    Set<String> scopes = new LinkedHashSet<>();
    if (rawScopes != null) {
      for (Object s : rawScopes) {
        scopes.add(String.valueOf(s));
      }
    }

    Map<String, Object> additionalParameters = (Map<String, Object>) data.get("additionalParameters");
    Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");

    return OAuth2AuthorizationRequest.authorizationCode()
        .authorizationUri((String) data.get("authorizationUri"))
        .clientId((String) data.get("clientId"))
        .redirectUri((String) data.get("redirectUri"))
        .scopes(scopes)
        .state((String) data.get("state"))
        .additionalParameters(additionalParameters != null ? additionalParameters : Map.of())
        .attributes(attributes != null ? attributes : Map.of())
        .build();
  }
}
