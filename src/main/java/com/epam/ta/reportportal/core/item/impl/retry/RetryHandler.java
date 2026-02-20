package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import java.time.Instant;

/**
 * Handles retry logic for test items.
 *
 * <p>Terminology:
 * <ul>
 *   <li><b>previousTry</b> — the item already in the database from an earlier execution</li>
 *   <li><b>lastTry</b> — the newly arrived item that triggers retry handling</li>
 * </ul>
 *
 * <p>The implementation compares {@code start_time} of both items to decide which one becomes the
 * "main" (keeps path/launch_id/statistics) and which one becomes the "retry"
 * (path=NULL, launch_id=NULL, retry_of=main).
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface RetryHandler {

  /**
   * Links {@code lastTry} and {@code previousTry} through the retry mechanism. Compares
   * {@code start_time} to decide which item becomes the "main" (keeps path/launch_id/statistics)
   * and which becomes the "retry" (path=NULL, launch_id=NULL, retry_of=main). Deletes the loser's
   * statistics and issue, then updates the rows accordingly.
   *
   * @param launch  the launch both items belong to
   * @param lastTry the newly arrived test item (already persisted)
   * @param retryOf item_id of the previous execution found in the database
   */
  void handleRetry(Launch launch, TestItem lastTry, String retryOf);

  /**
   * Finishes all in-progress retry items that point to the given main item
   * ({@code retry_of = itemId}). Called when the main item itself finishes.
   *
   * @param itemId  item_id of the main item that has retries
   * @param status  the final status to assign to dangling retries
   * @param endTime the end time to assign to dangling retries
   */
  void finishRetries(Long itemId, JStatusEnum status, Instant endTime);
}
