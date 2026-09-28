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

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.launch.changes.LaunchChangesHandler;
import com.epam.ta.reportportal.core.statistics.TestItemStatisticsService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interrupts one broken launch inside its own transaction.
 */
@Service
@RequiredArgsConstructor
public class BrokenLaunchInterruptService {

  private final ApplicationEventPublisher eventPublisher;

  private final LaunchRepository launchRepository;

  private final TestItemRepository testItemRepository;

  private final LogRepository logRepository;

  private final TestItemStatisticsService statisticsService;

  private final LaunchChangesHandler launchChangesHandler;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void interruptIfStillBroken(Long launchId, Duration maxDuration) {
    statisticsService.acquireAdvisoryLock(launchId);
    launchRepository.findById(launchId)
        .filter(launch -> StatusEnum.IN_PROGRESS == launch.getStatus())
        .ifPresent(launch -> interruptIfStillBroken(launch, maxDuration));
  }

  private void interruptIfStillBroken(Launch launch, Duration maxDuration) {
    Long launchId = launch.getId();
    if (!testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)) {
      interruptLaunch(launch);
      return;
    }

    if (!testItemRepository.hasItemsInStatusAddedLately(launchId, maxDuration,
        StatusEnum.IN_PROGRESS)) {
      if (testItemRepository.hasLogs(launchId, maxDuration, StatusEnum.IN_PROGRESS)) {
        if (!logRepository.hasLogsAddedLately(maxDuration, launchId, StatusEnum.IN_PROGRESS)) {
          interruptItems(launch);
        }
      } else {
        interruptItems(launch);
      }
    }
  }

  private void interruptLaunch(Launch launch) {
    var beforeSnapshot = launchChangesHandler.captureSnapshot(launch);
    launch.setStatus(StatusEnum.INTERRUPTED);
    launch.setEndTime(Instant.now());

    launchRepository.save(launch);
    launchChangesHandler.handleIfChanged(launch, beforeSnapshot);
    publishFinishEvent(launch);
  }

  private void publishFinishEvent(Launch launch) {
    final LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch);
    eventPublisher.publishEvent(launchFinishedEvent);
  }

  private void interruptItems(Launch launch) {
    Long launchId = launch.getId();
    testItemRepository.interruptInProgressItems(launchId);
    statisticsService.addInterruptionStatistics(launchId);
    interruptLaunch(launch);
  }
}
