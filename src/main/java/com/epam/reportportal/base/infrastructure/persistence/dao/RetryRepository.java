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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemPathContext;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for retry-specific bulk mutations on {@link TestItem}.
 * <p>
 * All write methods use native SQL to bypass Hibernate dirty-checking and avoid loading entire
 * entity graphs for bulk operations.
 */
public interface RetryRepository extends JpaRepository<TestItem, Long> {

  /**
   * Marks an item as having retries.
   *
   * @param itemId item_id of the main item
   */
  @Modifying
  @Query(value = """
      UPDATE test_item
      SET    has_retries   = TRUE,
             last_modified = CURRENT_TIMESTAMP
      WHERE  item_id = :itemId
      """, nativeQuery = true)
  void markAsHavingRetries(@Param("itemId") Long itemId);

  /**
   * Acquires a transaction-scoped PostgreSQL advisory lock on the given key. Used to serialize all
   * retry and statistics operations within a single launch.
   *
   * @param lockId typically the launch ID
   */
  @Query(value = "SELECT pg_advisory_xact_lock(:lockId)", nativeQuery = true)
  void advisoryXactLock(@Param("lockId") Long lockId);

  /**
   * Finds the "winner" — the active item with the latest {@code start_time} (ties broken by highest
   * {@code item_id}) among items sharing the same {@code uniqueId} and {@code parentId} that are
   * still in the launch tree.
   *
   * @param uniqueId unique identifier of the test case
   * @param parentId parent item id (scopes the search to one launch branch)
   * @return item_id of the winner, or empty if no active items exist
   */
  @Query(value = """
      SELECT item_id FROM test_item
      WHERE  unique_id = :uniqueId
        AND  parent_id = :parentId
        AND  path IS NOT NULL
        AND  retry_of IS NULL
      ORDER BY start_time DESC, item_id DESC
      LIMIT 1
      """, nativeQuery = true)
  Optional<Long> findLatestTryByUniqueIdAndParentId(@Param("uniqueId") String uniqueId,
      @Param("parentId") Long parentId);

  /**
   * Demotes all active items (path ≠ NULL, retry_of IS NULL) with the given {@code uniqueId} and
   * {@code parentId}, except the winner, into retries. Sets {@code retry_of = winnerId}, clears
   * {@code launch_id} and {@code path}.
   *
   * @param winnerId item_id of the winner that should remain active
   */
  @Modifying
  @Query(value = """
      UPDATE test_item
      SET    retry_of      = :winnerId,
             launch_id     = NULL,
             has_retries   = FALSE,
             last_modified = CURRENT_TIMESTAMP,
             path          = NULL
      WHERE  item_id in (:itemIds)
      """, nativeQuery = true)
  void changeActiveTyPreviousTry(@Param("itemIds") List<Long> itemIds,
      @Param("winnerId") Long winnerId);

  @Query(value = """
      SELECT item_id, launch_id, path::VARCHAR
      FROM test_item
      WHERE  unique_id = :uniqueId
        AND  parent_id = :parentId
        AND  path IS NOT NULL
        AND  retry_of IS NULL
        AND  item_id != :winnerId
      """, nativeQuery = true)
  List<TestItemPathContext> getPreviousTries(@Param("uniqueId") String uniqueId,
      @Param("parentId") Long parentId, @Param("winnerId") Long winnerId);

  /**
   * Flattens existing retry chains: any item with the same {@code uniqueId} and {@code parentId}
   * that already has {@code retry_of} set but pointing to a different item is re-pointed to the
   * current winner. This ensures every retry references the main item directly (no chains).
   *
   * @param itemIds  item ids
   * @param winnerId item_id of the current winner (main item)
   */
  @Modifying
  @Query(value = """
      UPDATE test_item
      SET    retry_of      = :winnerId,
             last_modified = CURRENT_TIMESTAMP
      WHERE  retry_of in (:itemIds)
      """, nativeQuery = true)
  void pointPreviousTriesToLatest(@Param("itemIds") List<Long> itemIds,
      @Param("winnerId") Long winnerId);

}
