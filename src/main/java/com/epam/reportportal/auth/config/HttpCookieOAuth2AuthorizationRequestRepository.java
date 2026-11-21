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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cookie-based OAuth2AuthorizationRequestRepository for stateless deployments. Stores the authorization request in a
 * short-lived secure cookie instead of the HTTP session, so the callback can be handled by any pod in a cluster.
 *
 * <p>The cookie value is {@code base64url(json).base64url(HMAC-SHA256(json))} and the JSON carries an expiry
 * timestamp, the same scheme as the SAML authentication request cookie. A cookie with a bad signature, a malformed
 * value or a past expiry is treated as absent, so a client able to set cookies on the host cannot plant a request
 * (state, redirect URI, nonce, PKCE verifier). The key must be shared by all instances.
 */
@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2_auth_request";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final char SIGNATURE_SEPARATOR = '.';
  private static final String EXPIRES_AT = "exp";
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private final JsonMapper objectMapper;
  private final boolean enforceHttps;
  private final SecretKeySpec signingKey;
  private final Clock clock;
  private final AtomicBoolean secureWarningLogged = new AtomicBoolean(false);

  /**
   * Creates the repository.
   *
   * @param objectMapper JSON mapper for the cookie payload
   * @param enforceHttps whether to always issue the cookie with the {@code Secure} flag
   * @param signingKey   HMAC key shared by all instances (at least 32 bytes)
   */
  public HttpCookieOAuth2AuthorizationRequestRepository(JsonMapper objectMapper, boolean enforceHttps,
      byte[] signingKey) {
    this(objectMapper, enforceHttps, signingKey, Clock.systemUTC());
  }

  HttpCookieOAuth2AuthorizationRequestRepository(JsonMapper objectMapper, boolean enforceHttps, byte[] signingKey,
      Clock clock) {
    if (signingKey == null || signingKey.length < 32) {
      throw new IllegalArgumentException("OAuth2 authorization request signing key must be at least 32 bytes");
    }
    this.objectMapper = objectMapper;
    this.enforceHttps = enforceHttps;
    this.signingKey = new SecretKeySpec(signingKey, HMAC_ALGORITHM);
    this.clock = clock;
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
    } catch (IOException | JacksonException | GeneralSecurityException _) {
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
          } catch (IOException | GeneralSecurityException | RuntimeException _) {
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

  private String serialize(OAuth2AuthorizationRequest authorizationRequest)
      throws IOException, GeneralSecurityException {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("grantType", authorizationRequest.getGrantType().getValue());
    data.put("authorizationUri", authorizationRequest.getAuthorizationUri());
    data.put("clientId", authorizationRequest.getClientId());
    data.put("redirectUri", authorizationRequest.getRedirectUri());
    data.put("scopes", authorizationRequest.getScopes());
    data.put("state", authorizationRequest.getState());
    data.put("additionalParameters", authorizationRequest.getAdditionalParameters());
    data.put("attributes", authorizationRequest.getAttributes());
    data.put(EXPIRES_AT, clock.instant().getEpochSecond() + COOKIE_EXPIRE_SECONDS);
    String payload = ENCODER.encodeToString(objectMapper.writeValueAsBytes(data));
    return payload + SIGNATURE_SEPARATOR + ENCODER.encodeToString(sign(payload));
  }

  private byte[] sign(String payload) throws GeneralSecurityException {
    Mac mac = Mac.getInstance(HMAC_ALGORITHM);
    mac.init(signingKey);
    return mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII));
  }

  /**
   * Returns the decoded payload if the signature is valid, otherwise {@code null}.
   */
  private byte[] verify(String value) throws GeneralSecurityException {
    int separator = value.indexOf(SIGNATURE_SEPARATOR);
    if (separator <= 0 || separator == value.length() - 1) {
      return null;
    }
    String payload = value.substring(0, separator);
    byte[] signature = DECODER.decode(value.substring(separator + 1));
    if (!MessageDigest.isEqual(sign(payload), signature)) {
      return null;
    }
    return DECODER.decode(payload);
  }

  @SuppressWarnings("unchecked")
  private OAuth2AuthorizationRequest deserialize(String value) throws IOException, GeneralSecurityException {
    byte[] payload = verify(value);
    if (payload == null) {
      log.warn("OAuth2 authorization request cookie has an invalid signature");
      return null;
    }
    Map<String, Object> data = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
    });
    if (!(data.get(EXPIRES_AT) instanceof Number expiresAt)
        || expiresAt.longValue() <= clock.instant().getEpochSecond()) {
      log.debug("OAuth2 authorization request cookie has expired");
      return null;
    }

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
