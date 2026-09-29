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

package com.epam.ta.reportportal.core.integration.grafana;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Sets the {@code grafana_session} cookie as a side effect of {@code GET /v1/users}, which already fires on every
 * page load / session-restore. This is what lets a Grafana panel iframe (which can't carry the RP JWT header) reach
 * the {@code /grafana/*} proxy: by the time a user opens one, the cookie is normally already there.
 *
 * <p>The session's lifetime tracks the caller's JWT {@code exp} claim, so it can't expire before the RP session it
 * stands in for; a fixed TTL unrelated to the JWT's actual lifetime previously caused the grafana session to expire
 * well before the JWT did. Grafana is UI-only and always reached with a JWT, so no cookie is issued for non-JWT
 * (e.g. API key) callers.
 *
 * @author Siarhei Hrabko
 */
@Slf4j
@Component
public class GrafanaSessionCookieIssuer {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String COOKIE_PATH = "/grafana/";
  private static final String SAME_SITE_LAX = "Lax";

  private final GrafanaSessionService grafanaSessionService;
  private final boolean enforceHttps;

  public GrafanaSessionCookieIssuer(GrafanaSessionService grafanaSessionService,
      @Value("${rp.auth.cookie.secure.enforce-https:true}") boolean enforceHttps) {
    this.grafanaSessionService = grafanaSessionService;
    this.enforceHttps = enforceHttps;
  }

  /**
   * Issues a fresh session for the current user, valid for as long as their JWT is, and sets it as an
   * {@code HttpOnly} cookie scoped to {@code /grafana/}. A no-op if the request wasn't authenticated with a JWT,
   * or if the current user isn't an instance admin — the Grafana dashboards aren't access-controlled per project,
   * so only admins get a session; everyone else would otherwise reach them directly via the proxy URL.
   *
   * @param currentUser currently authenticated user
   * @param request     current request, used to read the JWT's {@code exp} claim and to decide the cookie's
   *                     {@code Secure} flag when HTTPS isn't enforced
   * @param response    current response, receives the {@code Set-Cookie} header
   */
  public void issue(ReportPortalUser currentUser, HttpServletRequest request, HttpServletResponse response) {
    if (currentUser.getUserRole() != UserRole.ADMINISTRATOR) {
      return;
    }
    extractJwtExpiry(request)
        .map(expiresAt -> Duration.between(Instant.now(), expiresAt))
        .filter(ttl -> !ttl.isNegative())
        .ifPresent(ttl -> {
          UUID sessionId = grafanaSessionService.create(currentUser.getUsername(), ttl);
          ResponseCookie cookie = ResponseCookie.from(GrafanaSessionService.SESSION_COOKIE_NAME, sessionId.toString())
              .path(COOKIE_PATH)
              .httpOnly(true)
              .maxAge(ttl)
              .secure(enforceHttps || request.isSecure())
              .sameSite(SAME_SITE_LAX)
              .build();
          response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        });
  }

  private Optional<Instant> extractJwtExpiry(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!Strings.CI.startsWith(header, BEARER_PREFIX)) {
      return Optional.empty();
    }
    try {
      String token = Strings.CI.removeStart(header, BEARER_PREFIX);
      JWTClaimsSet claims = JWTParser.parse(token).getJWTClaimsSet();
      return Optional.ofNullable(claims.getExpirationTime()).map(Date::toInstant);
    } catch (ParseException e) {
      log.debug("Failed to read exp claim from bearer token", e);
      return Optional.empty();
    }
  }
}
