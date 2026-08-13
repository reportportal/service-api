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

package com.epam.reportportal.base.core.file;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

class FileSignedLinkServiceTest {

  private static final long ATTACHMENT_ID = 42L;
  private static final String PROJECT_KEY = "my-project";
  private static final long USER_ID = 15L;
  private static final Instant NOW = Instant.parse("2026-08-11T12:00:00Z");

  private ServerSettingsRepository serverSettingsRepository;
  private FileSignedLinkService tokenService;

  @BeforeEach
  void setUp() {
    serverSettingsRepository = mock(ServerSettingsRepository.class);
    tokenService = createService(Duration.ofMinutes(30));
  }

  @Test
  void shouldVerifyValidSignature() {
    var link = tokenService.createLink(ATTACHMENT_ID, PROJECT_KEY, USER_ID);

    var payload = tokenService.verify(
        ATTACHMENT_ID,
        PROJECT_KEY,
        USER_ID,
        link.expiresAtEpochSecond(),
        link.signature()
    );

    assertAll(
        () -> assertEquals(ATTACHMENT_ID, payload.attachmentId()),
        () -> assertEquals(PROJECT_KEY, payload.projectKey()),
        () -> assertEquals(USER_ID, payload.userId()),
        () -> assertEquals(NOW.plus(Duration.ofMinutes(30)).getEpochSecond(),
            payload.expiresAtEpochSecond())
    );
  }

  @Test
  void shouldRejectTamperedSignature() {
    var link = tokenService.createLink(ATTACHMENT_ID, PROJECT_KEY, USER_ID);

    assertInvalid(() -> tokenService.verify(
        ATTACHMENT_ID,
        PROJECT_KEY,
        USER_ID,
        link.expiresAtEpochSecond(),
        link.signature() + "A"
    ));
  }

  @Test
  void shouldRejectTamperedExpiration() {
    var link = tokenService.createLink(ATTACHMENT_ID, PROJECT_KEY, USER_ID);

    assertInvalid(() -> tokenService.verify(
        ATTACHMENT_ID,
        PROJECT_KEY,
        USER_ID,
        link.expiresAtEpochSecond() + 1,
        link.signature()
    ));
  }

  @Test
  void shouldRejectTamperedUserId() {
    var link = tokenService.createLink(ATTACHMENT_ID, PROJECT_KEY, USER_ID);

    assertInvalid(() -> tokenService.verify(
        ATTACHMENT_ID,
        PROJECT_KEY,
        USER_ID + 1,
        link.expiresAtEpochSecond(),
        link.signature()
    ));
  }

  @Test
  void shouldRejectExpiredToken() {
    var expiredTokenService = createService(Duration.ofSeconds(-1));
    var link = expiredTokenService.createLink(ATTACHMENT_ID, PROJECT_KEY, USER_ID);

    assertInvalid(() -> expiredTokenService.verify(
        ATTACHMENT_ID,
        PROJECT_KEY,
        USER_ID,
        link.expiresAtEpochSecond(),
        link.signature()
    ));
  }

  private FileSignedLinkService createService(Duration ttl) {
    var service = new FileSignedLinkService(
        serverSettingsRepository,
        "master-secret",
        ttl,
        Clock.fixed(NOW, ZoneOffset.UTC)
    );
    service.initialize();
    return service;
  }

  private void assertInvalid(Runnable verification) {
    assertThrows(BadCredentialsException.class, verification::run);
  }
}
