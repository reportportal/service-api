package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.core.events.activity.item.ItemRetryEvent;
import com.epam.ta.reportportal.core.item.repository.RetryRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import java.time.Instant;
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
 * <p>After determining the winner, existing retries that pointed to any of the losers are
 * re-pointed ("flattened") to the winner so that every retry always references the current main
 * item directly — no chains.
 *
 * @author Pavel Bortnik
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class NewRetryHandler implements RetryHandler {

  private final TestItemRepository testItemRepository;
  private final RetryRepository retryRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Finds the winner among all active items sharing {@code newTry.uniqueId} and
   * {@code newTry.parentId}, demotes losers, and flattens existing retry chains so every retry
   * points directly to the winner.
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

    // 2. Find the winner (max start_time, then max item_id) among active items
    Optional<Long> latestTry =
        retryRepository.findLatestTryByUniqueIdAndParentId(uniqueId, parentId);

    if (latestTry.isEmpty()) {
      return;
    }

    Long lastestTryId = latestTry.get();

    // 3. Demote all other active items to retries of the winner
    int demoted = retryRepository.changeActiveTyPreviousTry(uniqueId, parentId, lastestTryId);

    if (demoted == 0) {
      return; // no duplicates found — nothing to do
    }

    // 4. Flatten: re-point any existing retries to the winner directly (no chains)
    retryRepository.pointPreviousTriesToLatest(uniqueId, parentId, lastestTryId);

    // 5. Mark the winner as having retries
    retryRepository.markAsHavingRetries(lastestTryId);

    if (!launch.isHasRetries()) {
      launch.setHasRetries(true);
    }

    eventPublisher.publishEvent(
        ItemRetryEvent.of(launch.getProjectId(), launch.getId(), newTry.getItemId()));
  }

  @Override
  public void finishRetries(Long itemId, JStatusEnum status, Instant endTime) {
//    testItemRepository.updateStatusAndEndTimeByRetryOfId(
//        itemId, JStatusEnum.IN_PROGRESS, JStatusEnum.valueOf(status.name()), endTime);
  }
}
