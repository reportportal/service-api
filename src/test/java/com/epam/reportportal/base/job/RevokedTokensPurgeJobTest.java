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

package com.epam.reportportal.base.job;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.base.infrastructure.persistence.dao.RevokedTokenRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RevokedTokensPurgeJobTest {

  private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 3600L;

  @Mock
  private RevokedTokenRepository revokedTokenRepository;

  @InjectMocks
  private RevokedTokensPurgeJob job;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(job, "accessTokenValiditySeconds", ACCESS_TOKEN_VALIDITY_SECONDS);
  }

  @Test
  @DisplayName("Should call deleteExpired with cutoff based on token validity period")
  void executeShouldDeleteExpiredEntries() {
    // Given
    var captor = ArgumentCaptor.forClass(Instant.class);
    var earliestCutoff = Instant.now().minus(Duration.ofSeconds(ACCESS_TOKEN_VALIDITY_SECONDS));

    // When
    job.execute(null);

    // Then
    verify(revokedTokenRepository).deleteExpired(captor.capture());
    var cutoff = captor.getValue();
    assertFalse(cutoff.isBefore(earliestCutoff));
    assertFalse(cutoff.isAfter(Instant.now()));
  }
}
