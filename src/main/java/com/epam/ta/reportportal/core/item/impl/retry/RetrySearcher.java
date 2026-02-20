package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Optional;

/**
 * Searches for a previous execution of the same test case within a launch.
 *
 * <p>The returned item_id (if any) represents the <b>previousTry</b> — the latest matching
 * item that is still a "main" item ({@code retry_of IS NULL, has_stats = true}).
 */
public interface RetrySearcher {

  /**
   * Finds the most recent previous execution matching the given retry item.
   *
   * @param launch the launch to search within
   * @param retry  the newly arrived item that triggered retry handling
   * @return item_id of the previous try, or empty if no match found
   */
  Optional<Long> findPreviousTry(Launch launch, TestItem retry);

}
