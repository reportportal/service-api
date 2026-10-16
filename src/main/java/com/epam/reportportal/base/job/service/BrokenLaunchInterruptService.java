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

package com.epam.reportportal.base.job.service;

import com.epam.reportportal.base.core.events.domain.LaunchFinishedEvent;
import com.epam.reportportal.base.core.launch.changes.LaunchChangesHandler;
import com.epam.reportportal.base.core.statistics.TestItemStatisticsService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
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
  public void interruptIfStillBroken(Long launchId, Long organizationId, Duration maxDuration) {
    statisticsService.acquireAdvisoryLock(launchId);
    launchRepository.findById(launchId)
        .filter(launch -> StatusEnum.IN_PROGRESS == launch.getStatus())
        .ifPresent(launch -> interruptIfStillBroken(launch, organizationId, maxDuration));
  }

  private void interruptIfStillBroken(Launch launch, Long organizationId, Duration maxDuration) {
    Long launchId = launch.getId();
    if (!testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)) {
      interruptLaunch(launch, organizationId);
      return;
    }

    if (!testItemRepository.hasItemsInStatusAddedLately(launchId, maxDuration,
        StatusEnum.IN_PROGRESS)) {
      if (testItemRepository.hasLogs(launchId, maxDuration, StatusEnum.IN_PROGRESS)) {
        if (!logRepository.hasLogsAddedLately(maxDuration, launchId, StatusEnum.IN_PROGRESS)) {
          interruptItems(launch, organizationId);
        }
      } else {
        interruptItems(launch, organizationId);
      }
    }
  }

  private void interruptLaunch(Launch launch, Long organizationId) {
    var beforeSnapshot = launchChangesHandler.captureSnapshot(launch);
    launch.setStatus(StatusEnum.INTERRUPTED);
    launch.setEndTime(Instant.now());

    launchRepository.save(launch);
    launchChangesHandler.handleIfChanged(launch, beforeSnapshot);
    publishFinishEvent(launch, organizationId);
  }

  private void interruptItems(Launch launch, Long organizationId) {
    Long launchId = launch.getId();
    testItemRepository.interruptInProgressItems(launchId);
    statisticsService.addInterruptionStatistics(launchId);
    interruptLaunch(launch, organizationId);
  }

  private void publishFinishEvent(Launch launch, Long organizationId) {
    final LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, organizationId);
    eventPublisher.publishEvent(launchFinishedEvent);
  }
}
