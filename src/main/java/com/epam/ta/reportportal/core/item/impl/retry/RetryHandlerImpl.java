package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.core.events.activity.item.ItemRetryEvent;
import com.epam.ta.reportportal.core.item.repository.RetryRepository;
import com.epam.ta.reportportal.core.item.repository.TestItemPathContext;
import com.epam.ta.reportportal.core.statistics.TestItemStatisticsService;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Retry handler supporting two modes:
 * <ul>
 *   <li><b>Explicit</b> — when {@code retryOf} UUID is provided, {@code newTry} is linked
 *       directly to the specific item identified by that UUID.</li>
 *   <li><b>Implicit</b> — when {@code retryOf} is absent, all active items with the same
 *       {@code uniqueId} and {@code parentId} are discovered and the one with the latest
 *       {@code start_time} becomes the "main".</li>
 * </ul>
 *
 * <p>An "active" item is one that is still part of the launch tree:
 * {@code path IS NOT NULL AND retry_of IS NULL}.
 *
 * <p>In both modes existing retry chains are flattened so that every retry always references the
 * current main item directly — no chains.
 *
 * @author Pavel Bortnik
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class RetryHandlerImpl implements RetryHandler {

  private final RetryRepository retryRepository;
  private final TestItemRepository testItemRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final TestItemStatisticsService testItemStatisticsService;

  @Override
  public void handleRetry(Launch launch, TestItem newTry, String retryOf) {
    retryRepository.advisoryXactLock(launch.getId());

    if (StringUtils.isNotBlank(retryOf)) {
      handleExplicitRetry(launch, newTry, retryOf);
    } else {
      handleImplicitRetry(launch, newTry);
    }
  }

  /**
   * Explicit mode: {@code newTry} is a new attempt of the specific item with
   * {@code uuid = retryOf}. The target is demoted and {@code newTry} becomes the main item. Falls
   * back to implicit mode if the target UUID is not found.
   */
  private void handleExplicitRetry(Launch launch, TestItem newTry, String retryOfUuid) {
    Optional<TestItem> targetOpt = testItemRepository.findByUuid(retryOfUuid);
    if (targetOpt.isEmpty()) {
      log.warn("retryOf UUID '{}' not found, falling back to implicit retry", retryOfUuid);
      handleImplicitRetry(launch, newTry);
      return;
    }

    TestItem target = targetOpt.get();

    if (target.getRetryOf() != null || target.getPath() == null) {
      log.warn("Target item {} (uuid={}) is already a retry or removed from tree, skipping",
          target.getItemId(), retryOfUuid);
      return;
    }

    Long latestTryId = newTry.getItemId();
    Long previousTryId = target.getItemId();

    retryRepository.changeActiveTyPreviousTry(List.of(previousTryId), latestTryId);
    retryRepository.pointPreviousTriesToLatest(List.of(previousTryId), latestTryId);
    retryRepository.markAsHavingRetries(latestTryId);

    if (!launch.isHasRetries()) {
      launch.setHasRetries(true);
    }

    testItemStatisticsService.deleteItemStatistics(
        new TestItemPathContext(target.getItemId(), target.getLaunchId(), target.getPath()));

    eventPublisher.publishEvent(
        ItemRetryEvent.of(launch.getProjectId(), launch.getId(), latestTryId));
  }

  /**
   * Implicit mode: discovers all active items sharing {@code newTry.uniqueId} and
   * {@code newTry.parentId}, picks the latest by {@code start_time} as the winner, and demotes the
   * rest.
   */
  private void handleImplicitRetry(Launch launch, TestItem newTry) {
    String uniqueId = newTry.getUniqueId();
    Long parentId = newTry.getParentId();

    if (uniqueId == null || parentId == null) {
      log.warn("Cannot handle retry: uniqueId or parentId is null for item {}",
          newTry.getItemId());
      return;
    }

    Optional<Long> latestTry =
        retryRepository.findLatestTryByUniqueIdAndParentId(uniqueId, parentId);
    if (latestTry.isEmpty()) {
      return;
    }

    Long latestTryId = latestTry.get();

    List<TestItemPathContext> previousTries =
        retryRepository.getPreviousTries(uniqueId, parentId, latestTryId);
    if (previousTries.isEmpty()) {
      return;
    }

    List<Long> previousTriesIds =
        previousTries.stream().map(TestItemPathContext::getItemId).toList();

    retryRepository.changeActiveTyPreviousTry(previousTriesIds, latestTryId);
    retryRepository.pointPreviousTriesToLatest(previousTriesIds, latestTryId);
    retryRepository.markAsHavingRetries(latestTryId);

    if (!launch.isHasRetries()) {
      launch.setHasRetries(true);
    }

    previousTries.forEach(testItemStatisticsService::deleteItemStatistics);

    eventPublisher.publishEvent(
        ItemRetryEvent.of(launch.getProjectId(), launch.getId(), latestTryId));
  }

  @Override
  public void finishRetries(TestItem item, JStatusEnum status, Instant endTime) {
    retryRepository.advisoryXactLock(item.getLaunchId());
    testItemRepository.updateStatusAndEndTimeByRetryOfId(
        item.getItemId(), JStatusEnum.IN_PROGRESS, JStatusEnum.valueOf(status.name()), endTime);
  }
}
