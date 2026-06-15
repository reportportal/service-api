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

package com.epam.reportportal.base.core.auth.impl;

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import com.epam.reportportal.base.infrastructure.persistence.dao.RevokedTokenRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.RevokedToken;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed implementation of {@link TokenBlacklistService}.
 *
 * <p>Per-token rows expire alongside their underlying JWT; per-user rows expire after the
 * configured access-token validity (no JWT issued before {@code revokeBefore} can outlive that window).
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

  private final RevokedTokenRepository revokedTokenRepository;

  @Value("${rp.jwt.token.validity-period}")
  private long accessTokenValiditySeconds;

  @Override
  @Transactional
  public void revoke(String jti, Instant expiresAt) {
    revokedTokenRepository.save(RevokedToken.forJti(jti, expiresAt));
  }

  @Override
  @Transactional
  public void revokeSubject(String subject) {
    var now = Instant.now();
    var expiresAt = now.plus(Duration.ofSeconds(accessTokenValiditySeconds));
    revokedTokenRepository.save(RevokedToken.forSubject(subject, now, expiresAt));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isRevoked(String jti, String subject, Instant issuedAt) {
    return revokedTokenRepository.isRevoked(jti, subject, issuedAt);
  }
}
