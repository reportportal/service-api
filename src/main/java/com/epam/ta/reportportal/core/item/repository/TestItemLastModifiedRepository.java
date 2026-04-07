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
import java.util.List;
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
   * Returns the next batch of leaf item IDs using cursor-style pagination. Only rows with
   * {@code item_id > afterItemId} are considered, ordered ascending.
   */
  @Query(value = """
      SELECT ti.item_id
      FROM   test_item ti
      WHERE  ti.launch_id = :launchId
        AND  ti.has_children = FALSE
        AND  ti.has_stats = TRUE
        AND  ti.type NOT IN ('SUITE', 'TEST')
        AND  ti.item_id > :afterItemId
      ORDER BY ti.item_id
      LIMIT :batchSize
      """, nativeQuery = true)
  List<Long> findNextLeafItemBatch(@Param("launchId") Long launchId,
      @Param("afterItemId") Long afterItemId,
      @Param("batchSize") int batchSize);

  /**
   * Updates {@code last_modified} for items with the given IDs. Uses {@code clock_timestamp()} so
   * that each row gets a distinct wall-clock value, preventing Logstash's {@code LIMIT}-based
   * polling from losing rows that share a timestamp.
   */
  @Modifying
  @Query(value = """
      UPDATE test_item
      SET last_modified = clock_timestamp()
      WHERE item_id IN (:itemIds)
      """, nativeQuery = true)
  void updateLastModifiedByItemIds(@Param("itemIds") List<Long> itemIds);
}
