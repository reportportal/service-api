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

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.reporting.FinishExecutionRQ;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gives the finish requests of the children of a launch a short time to arrive after the finish request of the launch.
 *
 * <p>Agents send the finish requests of the last items and of the launch almost at once, and network or broker delays
 * can deliver the launch finish first. Finishing the launch then interrupts the items whose finish requests are still
 * on their way. A finish request that finds items of the launch in progress is therefore processed only when they are
 * all finished or when the grace period, counted from the moment the request was received, is over. A launch without
 * items in progress is finished at once.
 *
 * <p>The end time of the launch never depends on this delay: it is the end time of the request or, when the request
 * has none, the moment the request was received.
 *
 * <p>The waiting is done outside of any transaction and without the lock of the launch, so the late finish requests
 * of the children can be processed meanwhile.
 */
@Slf4j
@Component
public class LaunchFinishGracePeriod {

  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;
  private final Duration gracePeriod;
  private final Duration pollInterval;
  private final Clock clock;

  @Autowired
  public LaunchFinishGracePeriod(LaunchRepository launchRepository, TestItemRepository testItemRepository,
      @Value("${reporting.launch-finish.grace-period:PT3S}") Duration gracePeriod,
      @Value("${reporting.launch-finish.poll-interval:PT0.2S}") Duration pollInterval) {
    this(launchRepository, testItemRepository, gracePeriod, pollInterval, Clock.systemUTC());
  }

  LaunchFinishGracePeriod(LaunchRepository launchRepository, TestItemRepository testItemRepository,
      Duration gracePeriod, Duration pollInterval, Clock clock) {
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.gracePeriod = gracePeriod == null || gracePeriod.isNegative() ? Duration.ZERO : gracePeriod;
    this.pollInterval = pollInterval == null || pollInterval.isNegative() || pollInterval.isZero()
        ? Duration.ofMillis(200) : pollInterval;
    this.clock = clock;
  }

  /**
   * @return the moment a finish request is received at, to be passed to the other methods of this class
   */
  public Instant receivedAt() {
    return clock.instant();
  }

  /**
   * Sets the end time of a finish request without one to the moment the request was received.
   *
   * @param request    the finish request of a launch
   * @param receivedAt the moment the request was received
   */
  public static void applyReceivedTime(FinishExecutionRQ request, Instant receivedAt) {
    if (request.getEndTime() == null) {
      request.setEndTime(receivedAt);
    }
  }

  /**
   * Returns how long the finish of a launch should still be delayed: the rest of the grace period if the launch is in
   * progress and has items in progress, otherwise zero.
   *
   * @param launchUuid UUID of the launch
   * @param projectId  ID of the project the request was sent to; a launch of another project is never delayed
   * @param receivedAt the moment the finish request was received
   * @return the remaining delay, zero if the launch should be finished now
   */
  public Duration remainingDelay(String launchUuid, Long projectId, Instant receivedAt) {
    Duration remaining = remaining(receivedAt);
    if (remaining.isZero()) {
      return Duration.ZERO;
    }
    return findLaunchInProgress(launchUuid, projectId)
        .filter(launch -> hasItemsInProgress(launch.getId()))
        .map(launch -> remaining(receivedAt))
        .orElse(Duration.ZERO);
  }

  /**
   * Blocks the calling thread while the launch has items in progress, until the grace period counted from
   * {@code receivedAt} is over. Must not be called within a transaction.
   *
   * @param launchUuid UUID of the launch
   * @param projectId  ID of the project the request was sent to; a launch of another project is never waited for
   * @param receivedAt the moment the finish request was received
   */
  public void awaitItemsInProgress(String launchUuid, Long projectId, Instant receivedAt) {
    if (remaining(receivedAt).isZero()) {
      return;
    }
    Optional<Long> launchId = findLaunchInProgress(launchUuid, projectId).map(Launch::getId);
    if (launchId.isEmpty()) {
      return;
    }
    boolean waited = false;
    while (hasItemsInProgress(launchId.get())) {
      Duration remaining = remaining(receivedAt);
      if (remaining.isZero()) {
        log.debug("Launch '{}' still has items in progress after the finish grace period of {}", launchUuid,
            gracePeriod);
        return;
      }
      waited = true;
      try {
        Thread.sleep(Math.max(1, Math.min(pollInterval.toMillis(), remaining.toMillis())));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
    if (waited) {
      log.debug("Items of launch '{}' finished within the finish grace period", launchUuid);
    }
  }

  private Duration remaining(Instant receivedAt) {
    if (gracePeriod.isZero() || receivedAt == null) {
      return Duration.ZERO;
    }
    Duration remaining = Duration.between(clock.instant(), receivedAt.plus(gracePeriod));
    return remaining.isNegative() ? Duration.ZERO : remaining;
  }

  private Optional<Launch> findLaunchInProgress(String launchUuid, Long projectId) {
    return launchRepository.findByUuid(launchUuid)
        .filter(launch -> launch.getStatus() == StatusEnum.IN_PROGRESS)
        .filter(launch -> Objects.equals(launch.getProjectId(), projectId));
  }

  private boolean hasItemsInProgress(Long launchId) {
    return Boolean.TRUE.equals(testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS));
  }
}
