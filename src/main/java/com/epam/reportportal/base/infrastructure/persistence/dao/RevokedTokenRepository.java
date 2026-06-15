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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA repository for {@link RevokedToken} entries. Supports both per-jti and per-subject lookups in a single query,
 * plus a bulk delete used by the scheduled purge.
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

  /**
   * Returns {@code true} if a JWT identified by the given parameters is revoked, either by an exact-jti match or by a
   * per-subject revocation issued after the JWT was issued.
   *
   * @param jti      JWT ID
   * @param subject  JWT subject (sub claim)
   * @param issuedAt JWT {@code iat} claim
   * @return {@code true} if any matching blacklist row exists
   */
  @Query(value = """
      SELECT EXISTS (
          SELECT 1 FROM revoked_token
          WHERE jti = :jti
             OR (subject = :subject AND revoke_before >= :issuedAt)
      )
      """, nativeQuery = true)
  boolean isRevoked(@Param("jti") String jti, @Param("subject") String subject, @Param("issuedAt") Instant issuedAt);

  /**
   * Removes all blacklist rows whose {@code expiresAt} is in the past.
   *
   * @param now reference point; rows with {@code expiresAt < now} are deleted
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM RevokedToken t WHERE t.expiresAt < :now")
  void deleteExpired(Instant now);
}
