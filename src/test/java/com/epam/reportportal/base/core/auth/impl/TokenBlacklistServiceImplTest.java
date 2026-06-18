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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.RevokedTokenRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.RevokedToken;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

  @Mock
  private RevokedTokenRepository revokedTokenRepository;

  @InjectMocks
  private TokenBlacklistServiceImpl service;

  @Test
  @DisplayName("Should persist a per-jti RevokedToken with the given jti and revokedAt")
  void revokeWhenCalledShouldSavePerJtiRevokedToken() {
    // Given
    var jti = "abc-123";
    var captor = ArgumentCaptor.forClass(RevokedToken.class);

    // When
    service.revoke(jti);

    // Then
    verify(revokedTokenRepository).save(captor.capture());
    assertEquals(jti, captor.getValue().getJti());
    assertNotNull(captor.getValue().getRevokedAt());
  }

  @Test
  @DisplayName("Should persist a per-subject RevokedToken with subject and revokedAt")
  void revokeSubjectWhenCalledShouldSavePerSubjectRevokedToken() {
    // Given
    var subject = "user@example.com";
    var captor = ArgumentCaptor.forClass(RevokedToken.class);

    // When
    service.revokeSubject(subject);

    // Then
    verify(revokedTokenRepository).save(captor.capture());
    var saved = captor.getValue();
    assertEquals(subject, saved.getSubject());
    assertNull(saved.getJti());
    assertNotNull(saved.getRevokedAt());
  }

  @Test
  @DisplayName("Should return true when token is revoked by jti or subject")
  void isRevokedWhenMatchExistsShouldReturnTrue() {
    // Given
    var jti = "abc-123";
    var subject = "user@example.com";
    var issuedAt = Instant.parse("2026-01-01T00:00:00Z");
    when(revokedTokenRepository.isRevoked(jti, subject, issuedAt)).thenReturn(true);

    // When
    var result = service.isRevoked(jti, subject, issuedAt);

    // Then
    assertTrue(result);
    verify(revokedTokenRepository).isRevoked(jti, subject, issuedAt);
  }

  @Test
  @DisplayName("Should return false when token is not revoked")
  void isRevokedWhenNoMatchShouldReturnFalse() {
    // Given
    var jti = "abc-123";
    var subject = "user@example.com";
    var issuedAt = Instant.parse("2026-01-01T00:00:00Z");
    when(revokedTokenRepository.isRevoked(jti, subject, issuedAt)).thenReturn(false);

    // When
    var result = service.isRevoked(jti, subject, issuedAt);

    // Then
    assertFalse(result);
    verify(revokedTokenRepository).isRevoked(jti, subject, issuedAt);
  }
}
