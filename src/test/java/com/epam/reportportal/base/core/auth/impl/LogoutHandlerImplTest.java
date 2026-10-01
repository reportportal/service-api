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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import com.epam.reportportal.base.core.integration.grafana.GrafanaSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerImplTest {

  @Mock
  private TokenBlacklistService tokenBlacklistService;

  @Mock
  private GrafanaSessionService grafanaSessionService;

  @Mock
  private Jwt jwt;

  @InjectMocks
  private LogoutHandlerImpl handler;

  @Test
  @DisplayName("Should blacklist the JWT's jti and revoke the subject's Grafana sessions")
  void logoutWhenCalledShouldRevokeTokenAndGrafanaSessions() {
    // Given
    when(jwt.getId()).thenReturn("jti-123");
    when(jwt.getSubject()).thenReturn("user@example.com");

    // When
    handler.logout(jwt);

    // Then
    verify(tokenBlacklistService).revoke("jti-123");
    verify(grafanaSessionService).revokeForSubject("user@example.com");
  }
}
