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

package com.epam.reportportal.base.core.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.reporting.FinishExecutionRQ;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LaunchFinishGracePeriodUnitTest {

  private static final String UUID = "launch-uuid";
  private static final long PROJECT_ID = 2L;
  private static final long LAUNCH_ID = 11L;
  private static final Instant NOW = Instant.parse("2026-11-22T10:00:00Z");

  @Mock
  private LaunchRepository launchRepository;
  @Mock
  private TestItemRepository testItemRepository;

  private Launch launch;

  @BeforeEach
  void setUp() {
    launch = new Launch();
    launch.setId(LAUNCH_ID);
    launch.setProjectId(PROJECT_ID);
    launch.setStatus(StatusEnum.IN_PROGRESS);
    when(launchRepository.findByUuid(UUID)).thenReturn(Optional.of(launch));
    when(testItemRepository.hasItemsInStatusByLaunch(LAUNCH_ID, StatusEnum.IN_PROGRESS)).thenReturn(true);
  }

  @Test
  void remainingDelayIsTheRestOfTheGracePeriodFromTheReceiveTime() {
    LaunchFinishGracePeriod grace = fixed(Duration.ofSeconds(3));
    assertEquals(Duration.ofMillis(2_000), grace.remainingDelay(UUID, PROJECT_ID, NOW.minusSeconds(1)));
  }

  @Test
  void noDelayWithoutItemsInProgress() {
    when(testItemRepository.hasItemsInStatusByLaunch(LAUNCH_ID, StatusEnum.IN_PROGRESS)).thenReturn(false);
    assertEquals(Duration.ZERO, fixed(Duration.ofSeconds(3)).remainingDelay(UUID, PROJECT_ID, NOW));
  }

  @Test
  void noDelayOnceTheGracePeriodIsOver() {
    assertEquals(Duration.ZERO, fixed(Duration.ofSeconds(3)).remainingDelay(UUID, PROJECT_ID, NOW.minusSeconds(4)));
    verify(launchRepository, never()).findByUuid(UUID);
  }

  @Test
  void noDelayWhenTurnedOff() {
    assertEquals(Duration.ZERO, fixed(Duration.ZERO).remainingDelay(UUID, PROJECT_ID, NOW));
  }

  @Test
  void noDelayForAFinishedLaunchOrALaunchOfAnotherProject() {
    LaunchFinishGracePeriod grace = fixed(Duration.ofSeconds(3));
    assertEquals(Duration.ZERO, grace.remainingDelay(UUID, PROJECT_ID + 1, NOW));
    launch.setStatus(StatusEnum.PASSED);
    assertEquals(Duration.ZERO, grace.remainingDelay(UUID, PROJECT_ID, NOW));
    when(launchRepository.findByUuid(UUID)).thenReturn(Optional.empty());
    assertEquals(Duration.ZERO, grace.remainingDelay(UUID, PROJECT_ID, NOW));
  }

  @Test
  void waitEndsAsSoonAsTheItemsAreFinished() {
    when(testItemRepository.hasItemsInStatusByLaunch(LAUNCH_ID, StatusEnum.IN_PROGRESS)).thenReturn(true, true,
        false);
    LaunchFinishGracePeriod grace = new LaunchFinishGracePeriod(launchRepository, testItemRepository,
        Duration.ofSeconds(10), Duration.ofMillis(10));

    long started = System.nanoTime();
    grace.awaitItemsInProgress(UUID, PROJECT_ID, grace.receivedAt());
    Duration took = Duration.ofNanos(System.nanoTime() - started);

    verify(testItemRepository, times(3)).hasItemsInStatusByLaunch(LAUNCH_ID, StatusEnum.IN_PROGRESS);
    assertTrue(took.compareTo(Duration.ofSeconds(2)) < 0, "took " + took);
  }

  @Test
  void waitEndsWhenTheGracePeriodIsOver() {
    LaunchFinishGracePeriod grace = new LaunchFinishGracePeriod(launchRepository, testItemRepository,
        Duration.ofMillis(300), Duration.ofMillis(20));

    long started = System.nanoTime();
    grace.awaitItemsInProgress(UUID, PROJECT_ID, grace.receivedAt());
    Duration took = Duration.ofNanos(System.nanoTime() - started);

    assertTrue(took.compareTo(Duration.ofMillis(250)) >= 0 && took.compareTo(Duration.ofSeconds(3)) < 0,
        "took " + took);
  }

  @Test
  void receiveTimeIsUsedOnlyWhenTheRequestHasNoEndTime() {
    FinishExecutionRQ withoutEndTime = new FinishExecutionRQ();
    LaunchFinishGracePeriod.applyReceivedTime(withoutEndTime, NOW);
    assertEquals(NOW, withoutEndTime.getEndTime());

    FinishExecutionRQ withEndTime = new FinishExecutionRQ();
    Instant endTime = NOW.minusSeconds(30);
    withEndTime.setEndTime(endTime);
    LaunchFinishGracePeriod.applyReceivedTime(withEndTime, NOW);
    assertEquals(endTime, withEndTime.getEndTime());
  }

  private LaunchFinishGracePeriod fixed(Duration gracePeriod) {
    return new LaunchFinishGracePeriod(launchRepository, testItemRepository, gracePeriod, Duration.ofMillis(10),
        Clock.fixed(NOW, ZoneOffset.UTC));
  }
}
