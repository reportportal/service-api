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

package com.epam.reportportal.base.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JWT blacklist row. Stores either a per-token revocation (by {@code jti}) or a per-user revocation (by JWT
 * {@code subject} with a {@code revokeBefore} timestamp).
 */
@Entity
@Table(name = "revoked_token", schema = "public")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RevokedToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "jti")
  private String jti;

  @Column(name = "subject")
  private String subject;

  @Column(name = "revoke_before")
  private Instant revokeBefore;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  /**
   * Creates a per-token revocation row.
   *
   * @param jti       JWT ID of the revoked token
   * @param expiresAt original {@code exp} of the JWT
   */
  public static RevokedToken forJti(String jti, Instant expiresAt) {
    return new RevokedToken(null, jti, null, null, expiresAt);
  }

  /**
   * Creates a per-user revocation row.
   *
   * @param subject      JWT {@code sub} claim of the user whose tokens are revoked
   * @param revokeBefore JWTs with {@code iat < revokeBefore} are rejected
   * @param expiresAt    row purge time (use now + max JWT lifetime)
   */
  public static RevokedToken forSubject(String subject, Instant revokeBefore, Instant expiresAt) {
    return new RevokedToken(null, null, subject, revokeBefore, expiresAt);
  }
}
