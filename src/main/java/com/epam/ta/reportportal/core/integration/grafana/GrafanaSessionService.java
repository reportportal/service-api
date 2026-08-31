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

import java.util.UUID;

/**
 * Backs the {@code /grafana/*} nginx {@code auth_request} gate: issues opaque session ids on session-restore,
 * validates them on every proxied request, and revokes them on request.
 *
 * @author Siarhei Hrabko
 */
public interface GrafanaSessionService {

  /** Name of the cookie that carries the session id between {@code GET /v1/users} and the nginx auth gate. */
  String SESSION_COOKIE_NAME = "grafana_session";

  /**
   * Issues a new session for the given subject.
   *
   * @param subject JWT subject ({@code sub} claim, i.e. the user's login) the session is issued to
   * @return the new session's id, to be carried in the {@code grafana_session} cookie
   */
  UUID create(String subject);

  /**
   * Checks whether the given session id is currently valid (exists and not expired).
   *
   * @param id session id, i.e. the value carried in the {@code grafana_session} cookie
   * @return {@code true} if the session is valid
   */
  boolean isValid(UUID id);

  /**
   * Revokes every active session for the given subject. Meant to be called when the client is about to drop its
   * JWT (logout), so Grafana access is cut off immediately rather than lingering until the cookie's
   * {@code Max-Age}.
   *
   * @param subject JWT subject ({@code sub} claim, i.e. the user's login) whose sessions should be revoked
   */
  void revokeForSubject(String subject);
}
