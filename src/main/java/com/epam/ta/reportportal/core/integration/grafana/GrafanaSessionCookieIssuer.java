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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sets the {@code grafana_session} cookie as a side effect of {@code GET /v1/users}, which already fires on every
 * page load / session-restore. This is what lets a Grafana panel iframe (which can't carry the RP JWT header) reach
 * the {@code /grafana/*} proxy: by the time a user opens one, the cookie is normally already there.
 *
 * @author Siarhei Hrabko
 */
@Component
public class GrafanaSessionCookieIssuer {

  private final GrafanaSessionService grafanaSessionService;
  private final boolean enforceHttps;
  private final int maxAgeSeconds;

  public GrafanaSessionCookieIssuer(GrafanaSessionService grafanaSessionService,
      @Value("${rp.auth.cookie.secure.enforce-https:true}") boolean enforceHttps,
      @Value("${rp.grafana.session.ttl:PT15M}") Duration ttl) {
    this.grafanaSessionService = grafanaSessionService;
    this.enforceHttps = enforceHttps;
    this.maxAgeSeconds = (int) ttl.toSeconds();
  }

  /**
   * Issues a fresh session for the current user and sets it as an {@code HttpOnly} cookie scoped to
   * {@code /grafana/}.
   *
   * @param currentUser currently authenticated user
   * @param request     current request, used to decide the cookie's {@code Secure} flag when HTTPS isn't enforced
   * @param response    current response, receives the {@code Set-Cookie} header
   */
  public void issue(ReportPortalUser currentUser, HttpServletRequest request,
      HttpServletResponse response) {
    UUID sessionId = grafanaSessionService.create(currentUser.getUsername());
    Cookie cookie = new Cookie(GrafanaSessionService.SESSION_COOKIE_NAME, sessionId.toString());
    cookie.setPath("/grafana/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAgeSeconds);
    cookie.setSecure(enforceHttps || request.isSecure());
    cookie.setAttribute("SameSite", "Lax");
    response.addCookie(cookie);
  }
}
