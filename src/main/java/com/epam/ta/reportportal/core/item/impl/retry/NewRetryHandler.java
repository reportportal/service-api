package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.core.events.activity.item.ItemRetryEvent;
import com.epam.ta.reportportal.core.item.repository.DeleteItemContext;
import com.epam.ta.reportportal.core.item.repository.RetryRepository;
import com.epam.ta.reportportal.core.statistics.TestItemStatisticsService;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Retry handler that finds ALL active items with the same {@code uniqueId} and {@code parentId},
 * picks the one with the latest {@code start_time} as the "main", and demotes all others to
 * retries.
 *
 * <p>An "active" item is one that is still part of the launch tree:
 * {@code path IS NOT NULL AND retry_of IS NULL}.
 *
 * <p>After determining the latestTry, existing retries that pointed to any of the losers are
 * re-pointed ("flattened") to the latestTry so that every retry always references the current main
 * item directly — no chains.
 *
 * @author Pavel Bortnik
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class NewRetryHandler implements RetryHandler {

  private final RetryRepository retryRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final TestItemStatisticsService testItemStatisticsService;

  /**
   * Finds the latestTry among all active items sharing {@code newTry.uniqueId} and
   * {@code newTry.parentId}, demotes losers, and flattens existing retry chains so every retry
   * points directly to the latestTry.
   *
   * <p>{@code previousTryId} is accepted for interface compatibility but is not used — the handler
   * discovers all candidates by itself.
   */
  @Override
  public void handleRetry(Launch launch, TestItem newTry, String retryOf) {
    String uniqueId = newTry.getUniqueId();
    Long parentId = newTry.getParentId();

    if (uniqueId == null || parentId == null) {
      log.warn("Cannot handle retry: uniqueId or parentId is null for item {}",
          newTry.getItemId());
      return;
    }

    // 1. Advisory lock — serialize all retry operations within the launch
    retryRepository.advisoryXactLock(launch.getId());

    // 2. Find the latestTry (max start_time, then max item_id) among active items
    Optional<Long> latestTry = retryRepository.findLatestTryByUniqueIdAndParentId(uniqueId,
        parentId);

    if (latestTry.isEmpty()) {
      return;
    }

    Long lastestTryId = latestTry.get();

    List<DeleteItemContext> previousTries = retryRepository.getPreviousTries(uniqueId, parentId,
        lastestTryId);

    if (previousTries.isEmpty()) {
      return;
    }
    List<Long> previousTriesIds = previousTries.stream().map(DeleteItemContext::getItemId)
        .toList();

    // 3. Demote all other active items to retries of the latestTry
    retryRepository.changeActiveTyPreviousTry(previousTriesIds, lastestTryId);

    // 4. Flatten: re-point any existing retries to the latestTry directly (no chains)
    retryRepository.pointPreviousTriesToLatest(previousTriesIds, lastestTryId);

    // 5. Mark the latestTry as having retries
    retryRepository.markAsHavingRetries(lastestTryId);

    if (!launch.isHasRetries()) {
      launch.setHasRetries(true);
    }

    previousTries.forEach(testItemStatisticsService::deleteItemStatistics);

    eventPublisher.publishEvent(
        ItemRetryEvent.of(launch.getProjectId(), launch.getId(), lastestTryId));
  }

  @Override
  public void finishRetries(TestItem item, JStatusEnum status, Instant endTime) {
//    retryRepository.advisoryXactLock(item.getLaunchId());
//    testItemRepository.updateStatusAndEndTimeByRetryOfId(
//        item.getItemId(), JStatusEnum.IN_PROGRESS, JStatusEnum.valueOf(status.name()), endTime);
  }
}
