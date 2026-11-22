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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CleanExpiredCreationBidsJobTest {

  @Mock
  private UserCreationBidRepository repository;

  @InjectMocks
  private CleanExpiredCreationBidsJob job;

  @ParameterizedTest
  @CsvSource({
      // invitation TTL, email-change TTL, expected cutoff age
      "PT24H, PT24H, PT24H",
      "PT72H, PT24H, PT72H",
      "PT24H, PT48H, PT48H",
      "PT1H, PT2H, PT2H"
  })
  void cutoffIsTheLongerOfTheInvitationAndEmailChangeTtls(Duration invitationTtl, Duration emailChangeTtl,
      Duration expected) throws Exception {
    ReflectionTestUtils.setField(job, "invitationBidTtl", invitationTtl);
    ReflectionTestUtils.setField(job, "emailChangeTtl", emailChangeTtl);
    when(repository.expireBidsOlderThan(any())).thenReturn(0);

    var before = Instant.now();
    job.execute(null);
    var after = Instant.now();

    var cutoff = ArgumentCaptor.forClass(Instant.class);
    verify(repository).expireBidsOlderThan(cutoff.capture());
    assertTrue(!cutoff.getValue().isBefore(before.minus(expected))
        && !cutoff.getValue().isAfter(after.minus(expected)), "cutoff " + cutoff.getValue());
  }

  @Test
  void defaultsKeepOneDay() throws Exception {
    var expected = Duration.ofHours(24);
    when(repository.expireBidsOlderThan(any())).thenReturn(0);

    var before = Instant.now();
    job.execute(null);
    var after = Instant.now();

    var cutoff = ArgumentCaptor.forClass(Instant.class);
    verify(repository).expireBidsOlderThan(cutoff.capture());
    assertTrue(!cutoff.getValue().isBefore(before.minus(expected))
        && !cutoff.getValue().isAfter(after.minus(expected)), "cutoff " + cutoff.getValue());
  }
}
