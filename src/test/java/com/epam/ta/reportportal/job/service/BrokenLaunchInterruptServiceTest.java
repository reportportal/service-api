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

package com.epam.ta.reportportal.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.launch.changes.LaunchChangesHandler;
import com.epam.ta.reportportal.core.statistics.TestItemStatisticsService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class BrokenLaunchInterruptServiceTest {

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private LogRepository logRepository;

  @Mock
  private TestItemStatisticsService statisticsService;

  @Mock
  private LaunchChangesHandler launchChangesHandler;

  @InjectMocks
  private BrokenLaunchInterruptService brokenLaunchInterruptService;

  @Test
  void interruptsLaunchWithoutInProgressItems() {
    long launchId = 1L;
    Launch launch = launch(launchId, StatusEnum.IN_PROGRESS);

    when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch));
    when(testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)).thenReturn(
        false);

    brokenLaunchInterruptService.interruptIfStillBroken(launchId, Duration.ofDays(1));

    assertEquals(StatusEnum.INTERRUPTED, launch.getStatus());
    verify(statisticsService).acquireAdvisoryLock(launchId);
    verify(launchRepository).save(launch);
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void skipsLaunchThatIsNoLongerInProgress() {
    long launchId = 1L;
    Launch launch = launch(launchId, StatusEnum.PASSED);

    when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch));

    brokenLaunchInterruptService.interruptIfStillBroken(launchId, Duration.ofDays(1));

    verify(statisticsService).acquireAdvisoryLock(launchId);
    verify(launchRepository, never()).save(any());
    verify(testItemRepository, never()).interruptInProgressItems(any());
  }

  @Test
  void interruptsItemsAndLaunchWhenInProgressItemsAndLogsAreStale() {
    long launchId = 1L;
    Duration maxDuration = Duration.ofDays(1);
    Launch launch = launch(launchId, StatusEnum.IN_PROGRESS);

    when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch));
    when(testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)).thenReturn(
        true);
    when(testItemRepository.hasItemsInStatusAddedLately(launchId, maxDuration,
        StatusEnum.IN_PROGRESS)).thenReturn(false);
    when(testItemRepository.hasLogs(launchId, maxDuration, StatusEnum.IN_PROGRESS)).thenReturn(
        true);
    when(
        logRepository.hasLogsAddedLately(maxDuration, launchId, StatusEnum.IN_PROGRESS)).thenReturn(
        false);

    brokenLaunchInterruptService.interruptIfStillBroken(launchId, maxDuration);

    verify(statisticsService).acquireAdvisoryLock(launchId);
    verify(testItemRepository).interruptInProgressItems(launchId);
    verify(statisticsService).addInterruptionStatistics(launchId);
    assertEquals(StatusEnum.INTERRUPTED, launch.getStatus());
  }

  @Test
  void interruptIfStillBrokenRunsInNewTransaction() throws NoSuchMethodException {
    Method method = BrokenLaunchInterruptService.class.getMethod("interruptIfStillBroken",
        Long.class, Duration.class);

    Transactional transactional = method.getAnnotation(Transactional.class);

    assertNotNull(transactional);
    assertEquals(Propagation.REQUIRES_NEW, transactional.propagation());
  }

  private Launch launch(Long launchId, StatusEnum status) {
    Launch launch = new Launch();
    launch.setId(launchId);
    launch.setStatus(status);
    return launch;
  }
}
