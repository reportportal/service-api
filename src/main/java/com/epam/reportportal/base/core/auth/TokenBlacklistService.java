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

package com.epam.reportportal.base.core.auth;

import java.time.Instant;

/**
 * JWT blacklist used to support immediate, server-side token revocation.
 */
public interface TokenBlacklistService {

  /**
   * Adds the given JWT identifier to the blacklist until its natural expiration.
   *
   * @param jti       JWT ID (the {@code jti} claim) of the token being revoked
   * @param expiresAt original {@code exp} claim of the token; bounds the row's lifetime
   */
  void revoke(String jti, Instant expiresAt);

  /**
   * Revokes every currently active JWT issued to the given subject. JWTs whose {@code iat} is earlier than the moment
   * this method is called will be rejected on the next request.
   *
   * @param subject JWT subject ({@code sub} claim) — typically the user's login or external id
   */
  void revokeSubject(String subject);

  /**
   * Checks whether a JWT identified by the given parameters has been revoked, either directly by {@code jti} or by a
   * subject-level revocation that post-dates the JWT's {@code iat}.
   *
   * @param jti      JWT ID
   * @param subject  JWT subject ({@code sub} claim)
   * @param issuedAt JWT {@code iat} claim
   * @return {@code true} if the JWT is revoked
   */
  boolean isRevoked(String jti, String subject, Instant issuedAt);
}
