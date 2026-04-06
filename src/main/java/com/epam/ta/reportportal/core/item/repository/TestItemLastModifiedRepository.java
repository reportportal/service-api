/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.core.item.repository;

import com.epam.ta.reportportal.entity.item.TestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence operations for {@link TestItem#getLastModified()} field updates.
 */
public interface TestItemLastModifiedRepository extends JpaRepository<TestItem, Long> {

  int BATCH_SIZE = 500;

  /**
   * Updates {@code last_modified} for a single batch of items belonging to the given launch.
   *
   * @param launchId  the launch whose items should be touched
   * @param batchSize maximum number of rows to update in one statement
   * @param offset    row offset within the ordered result set
   * @return the number of rows actually updated
   */
  @Modifying
  @Query(value = """
      UPDATE test_item
      SET last_modified = CURRENT_TIMESTAMP
      WHERE item_id IN (
        SELECT item_id FROM test_item
        WHERE launch_id = :launchId
        ORDER BY item_id
        LIMIT :batchSize OFFSET :offset
      )
      """, nativeQuery = true)
  int updateLastModifiedBatch(@Param("launchId") Long launchId,
      @Param("batchSize") int batchSize,
      @Param("offset") int offset);

  /**
   * Sets {@code last_modified = CURRENT_TIMESTAMP} on every test item that belongs to the given
   * launch, processing rows in batches of {@link #BATCH_SIZE}.
   *
   * @param launchId the launch whose items should be touched
   */
  default void updateLastModifiedByLaunchId(Long launchId) {
    int offset = 0;
    int updated;
    do {
      updated = updateLastModifiedBatch(launchId, BATCH_SIZE, offset);
      offset += BATCH_SIZE;
    } while (updated == BATCH_SIZE);
  }
}
