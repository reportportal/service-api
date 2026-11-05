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

package com.epam.reportportal.auth.config.saml;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2PostAuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2RedirectAuthenticationRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestRepository;
import org.springframework.util.StringUtils;

/**
 * Cookie-based {@link Saml2AuthenticationRequestRepository} for stateless, multi-instance deployments. The default
 * {@code HttpSessionSaml2AuthenticationRequestRepository} keeps the AuthnRequest in the HTTP session of the instance
 * that initiated the login, so the IdP response fails {@code InResponseTo} validation when it is routed to another
 * instance. This implementation stores the minimal request data in a short-lived cookie instead.
 *
 * <p>The {@code SAMLRequest} payload itself is not stored (it can exceed cookie size limits for POST binding and is
 * not needed to validate the response); only the request id, relay state, binding and registration id are kept.
 *
 * <p>The cookie value is {@code base64url(json).base64url(HMAC-SHA256(json))}, and the JSON carries an expiry
 * timestamp. Cookies with an invalid signature or past their expiry are ignored, so a client cannot forge a request
 * id to make a captured SAML response pass {@code InResponseTo} validation. The key must be shared by all instances.
 *
 * <p>The IdP returns the response via a cross-site POST, so the cookie is issued with {@code SameSite=None; Secure}.
 * Browsers reject {@code SameSite=None} without {@code Secure}, so on plain HTTP no {@code SameSite} attribute is set,
 * matching the behaviour of the session cookie this repository replaces. This does not weaken protection: the cookie
 * is only read by the ACS endpoint, and the HMAC signature together with {@code InResponseTo} validation is what
 * binds the IdP response to the login started in this browser.
 */
@Slf4j
public class HttpCookieSaml2AuthenticationRequestRepository
    implements Saml2AuthenticationRequestRepository<AbstractSaml2AuthenticationRequest> {

  static final String COOKIE_NAME = "saml2_auth_request";
  private static final int COOKIE_EXPIRE_SECONDS = 300;
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final char SIGNATURE_SEPARATOR = '.';

  /**
   * Placeholder for the omitted {@code SAMLRequest}: it must be non-empty, but response validation only uses the id.
   */
  private static final String OMITTED_SAML_REQUEST = "omitted";

  private static final String ID = "id";
  private static final String RELAY_STATE = "relayState";
  private static final String REGISTRATION_ID = "registrationId";
  private static final String AUTHN_REQUEST_URI = "authenticationRequestUri";
  private static final String BINDING = "binding";
  private static final String EXPIRES_AT = "exp";

  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private final RelyingPartyRegistrationRepository registrationRepository;
  private final ObjectMapper objectMapper;
  private final boolean enforceHttps;
  private final SecretKeySpec signingKey;
  private final Clock clock;
  private final AtomicBoolean secureWarningLogged = new AtomicBoolean(false);

  /**
   * Creates the repository.
   *
   * @param registrationRepository repository used to rebuild the request for the stored registration id
   * @param objectMapper           JSON mapper for the cookie payload
   * @param enforceHttps           whether to always issue the cookie with the {@code Secure} flag
   * @param signingKey             HMAC key shared by all instances (at least 32 bytes)
   */
  public HttpCookieSaml2AuthenticationRequestRepository(RelyingPartyRegistrationRepository registrationRepository,
      ObjectMapper objectMapper, boolean enforceHttps, byte[] signingKey) {
    this(registrationRepository, objectMapper, enforceHttps, signingKey, Clock.systemUTC());
  }

  HttpCookieSaml2AuthenticationRequestRepository(RelyingPartyRegistrationRepository registrationRepository,
      ObjectMapper objectMapper, boolean enforceHttps, byte[] signingKey, Clock clock) {
    if (signingKey == null || signingKey.length < 32) {
      throw new IllegalArgumentException("SAML2 authentication request signing key must be at least 32 bytes");
    }
    this.registrationRepository = registrationRepository;
    this.objectMapper = objectMapper;
    this.enforceHttps = enforceHttps;
    this.signingKey = new SecretKeySpec(signingKey, HMAC_ALGORITHM);
    this.clock = clock;
  }

  @Override
  public AbstractSaml2AuthenticationRequest loadAuthenticationRequest(HttpServletRequest request) {
    return readCookie(request).orElse(null);
  }

  @Override
  public void saveAuthenticationRequest(AbstractSaml2AuthenticationRequest authenticationRequest,
      HttpServletRequest request, HttpServletResponse response) {
    if (authenticationRequest == null) {
      deleteCookie(request, response);
      return;
    }
    boolean secure = isSecure(request);
    if (!secure && secureWarningLogged.compareAndSet(false, true)) {
      log.warn("SAML authentication request cookie is issued over plain HTTP without SameSite; some browsers "
          + "may not send it on the IdP's cross-site POST to the ACS endpoint. Use HTTPS and ensure your reverse "
          + "proxy forwards X-Forwarded-Proto: https.");
    }
    try {
      response.addCookie(buildCookie(serialize(authenticationRequest), COOKIE_EXPIRE_SECONDS, secure));
    } catch (GeneralSecurityException | JsonProcessingException _) {
      log.error("Failed to save SAML2 authentication request to cookie");
    }
  }

  @Override
  public AbstractSaml2AuthenticationRequest removeAuthenticationRequest(HttpServletRequest request,
      HttpServletResponse response) {
    AbstractSaml2AuthenticationRequest authRequest = loadAuthenticationRequest(request);
    if (authRequest != null) {
      deleteCookie(request, response);
    }
    return authRequest;
  }

  private boolean isSecure(HttpServletRequest request) {
    return enforceHttps || request.isSecure();
  }

  private Cookie buildCookie(String value, int maxAge, boolean secure) {
    Cookie cookie = new Cookie(COOKIE_NAME, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);
    cookie.setSecure(secure);
    if (secure) {
      cookie.setAttribute("SameSite", "None");
    }
    return cookie;
  }

  private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
    response.addCookie(buildCookie("", 0, isSecure(request)));
  }

  private Optional<AbstractSaml2AuthenticationRequest> readCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(c -> COOKIE_NAME.equals(c.getName()))
        .findFirst()
        .map(Cookie::getValue)
        .filter(StringUtils::hasText)
        .map(this::deserialize);
  }

  private String serialize(AbstractSaml2AuthenticationRequest authRequest)
      throws GeneralSecurityException, JsonProcessingException {
    Map<String, String> data = new LinkedHashMap<>();
    data.put(ID, authRequest.getId());
    data.put(RELAY_STATE, authRequest.getRelayState());
    data.put(REGISTRATION_ID, authRequest.getRelyingPartyRegistrationId());
    data.put(AUTHN_REQUEST_URI, authRequest.getAuthenticationRequestUri());
    data.put(BINDING, authRequest.getBinding().name());
    data.put(EXPIRES_AT, Long.toString(clock.instant().getEpochSecond() + COOKIE_EXPIRE_SECONDS));
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

  private AbstractSaml2AuthenticationRequest deserialize(String value) {
    try {
      byte[] payload = verify(value);
      if (payload == null) {
        log.warn("SAML2 authentication request cookie has an invalid signature");
        return null;
      }
      Map<String, String> data = objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {
      });
      String expiresAt = data.get(EXPIRES_AT);
      if (expiresAt == null || Long.parseLong(expiresAt) <= clock.instant().getEpochSecond()) {
        log.debug("SAML2 authentication request cookie has expired");
        return null;
      }
      RelyingPartyRegistration registration = registrationRepository.findByRegistrationId(
          data.get(REGISTRATION_ID));
      if (registration == null) {
        log.warn("SAML2 authentication request cookie references unknown registration");
        return null;
      }
      Saml2MessageBinding binding = Saml2MessageBinding.valueOf(data.get(BINDING));
      if (binding == Saml2MessageBinding.POST) {
        return Saml2PostAuthenticationRequest.withRelyingPartyRegistration(registration)
            .id(data.get(ID))
            .relayState(data.get(RELAY_STATE))
            .authenticationRequestUri(data.get(AUTHN_REQUEST_URI))
            .samlRequest(OMITTED_SAML_REQUEST)
            .build();
      }
      return Saml2RedirectAuthenticationRequest.withRelyingPartyRegistration(registration)
          .id(data.get(ID))
          .relayState(data.get(RELAY_STATE))
          .authenticationRequestUri(data.get(AUTHN_REQUEST_URI))
          .samlRequest(OMITTED_SAML_REQUEST)
          .build();
    } catch (JacksonException | IllegalArgumentException | GeneralSecurityException _) {
      log.warn("Failed to deserialize SAML2 authentication request from cookie");
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
