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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.grafana.GrafanaSession;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Backs the nginx {@code auth_request} gate (existence check by id) and the logout / scheduled-purge cleanup paths for
 * {@link GrafanaSession} rows.
 *
 * @author Siarhei Hrabko
 */
public interface GrafanaSessionRepository extends ReportPortalRepository<GrafanaSession, UUID> {

  /**
   * Returns {@code true} if a non-expired session with the given id exists.
   *
   * @param id  session id, i.e. the value carried in the {@code grafana_session} cookie
   * @param now reference point the expiry is checked against
   * @return {@code true} if the session is valid
   */
  boolean existsByIdAndExpiresAtAfter(UUID id, Instant now);

  /**
   * Deletes every session belonging to the given subject, used to cut off Grafana access immediately on logout.
   *
   * @param subject JWT subject ({@code sub} claim, i.e. the user's login) whose sessions should be revoked
   */
  @Modifying
  @Query("DELETE FROM GrafanaSession s WHERE s.subject = :subject")
  void deleteBySubject(@Param("subject") String subject);

  /**
   * Removes every session row whose {@code expiresAt} is older than the given cutoff.
   *
   * @param cutoff reference point; rows with {@code expiresAt < cutoff} are deleted
   */
  @Modifying
  @Query("DELETE FROM GrafanaSession s WHERE s.expiresAt < :cutoff")
  void deleteExpired(@Param("cutoff") Instant cutoff);
}
