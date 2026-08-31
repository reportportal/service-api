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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.grafana.GrafanaSessionService;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Grafana proxy session endpoints: the unauthenticated nginx {@code auth_request} gate, and an authenticated
 * revoke hook a client is expected to call as part of its own logout flow.
 *
 * @author Siarhei Hrabko
 */
@RestController
@Tag(name = "Grafana", description = "Grafana proxy gate")
public class GrafanaController {

  private final GrafanaSessionService grafanaSessionService;
  private final String webAuthUser;

  public GrafanaController(GrafanaSessionService grafanaSessionService,
      @Value("${rp.grafana.webauth.user:shared-rp-user}") String webAuthUser) {
    this.grafanaSessionService = grafanaSessionService;
    this.webAuthUser = webAuthUser;
  }

  /**
   * Hit once per proxied request (iframe load and every asset/API/WebSocket sub-request); nginx forwards the
   * original request's {@code Cookie} header into this subrequest, but not the RP JWT (a plain iframe navigation
   * can't carry it), so this endpoint is deliberately unauthenticated and does its own validation against the
   * {@code grafana_session} cookie set on {@code GET /v1/users}.
   */
  @GetMapping("/v1/public/integration/grafana/session-check")
  @Operation(summary = "Validate the grafana_session cookie",
      description = "Internal endpoint for nginx auth_request; not part of the public API surface")
  public ResponseEntity<Void> sessionCheck(HttpServletRequest request) {
    UUID sessionId = readSessionId(request);
    if (sessionId == null || !grafanaSessionService.isValid(sessionId)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok().header("X-WEBAUTH-USER", webAuthUser).build();
  }

  /**
   * Revokes every active Grafana proxy session for the current user. There is no server-side JWT revocation in
   * this codebase yet, so this only cuts off the Grafana proxy specifically; a client should call it as part of
   * its own logout flow, before discarding the JWT.
   */
  @DeleteMapping("/v1/integration/grafana/session")
  @Operation(summary = "Revoke Grafana proxy sessions for the current user")
  public OperationCompletionRS revokeSession(@AuthenticationPrincipal ReportPortalUser currentUser) {
    grafanaSessionService.revokeForSubject(currentUser.getUsername());
    return new OperationCompletionRS("Grafana proxy sessions have been revoked");
  }

  private static UUID readSessionId(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(cookie -> GrafanaSessionService.SESSION_COOKIE_NAME.equals(cookie.getName()))
        .findFirst()
        .map(Cookie::getValue)
        .flatMap(GrafanaController::tryParse)
        .orElse(null);
  }

  private static Optional<UUID> tryParse(String value) {
    try {
      return Optional.of(UUID.fromString(value));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
