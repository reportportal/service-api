/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.statistics.repository;

import com.epam.ta.reportportal.core.item.repository.TestItemPathContext;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Pavel Bortnik
 */
public interface StatisticsRepository extends ReportPortalRepository<Statistics, Long> {


  /**
   * Deletes all statistics by item ID.
   *
   * @param itemId item ID
   */
  void deleteByItemId(Long itemId);


  /**
   * Acquires a transaction-scoped PostgreSQL advisory lock on the given key. Used to serialize all
   * statistics operations within a single launch.
   *
   * @param lockId typically the launch ID
   */
  @Query(value = "SELECT pg_advisory_xact_lock(:lockId)", nativeQuery = true)
  void performAdvisoryLock(@Param("lockId") Long lockId);

  /**
   * Subtracts item's non-zero counters from all ancestors (excluding self). Uses PostgreSQL array
   * operations for efficient bulk updates.
   *
   * @param itemId  item ID whose statistics should be subtracted
   * @param pathIds array of item IDs (ancestors path)
   */
  @Modifying(flushAutomatically = true)
  @Query(value = """
      WITH item_stats AS (
        SELECT statistics_field_id, s_counter
        FROM statistics WHERE item_id = :itemId AND s_counter <> 0
      )
      UPDATE statistics s
      SET s_counter = GREATEST(0, s.s_counter - ist.s_counter)
      FROM item_stats ist
      WHERE s.statistics_field_id = ist.statistics_field_id
        AND s.item_id = ANY(CAST(:pathIds AS bigint[]))
        AND s.item_id <> :itemId
      """, nativeQuery = true)
  void subtractItemStatsFromAncestors(@Param("itemId") Long itemId,
      @Param("pathIds") Long[] pathIds);

  /**
   * Subtracts item's non-zero counters from launch statistics.
   *
   * @param itemId   item ID whose statistics should be subtracted
   * @param launchId launch ID
   */
  @Modifying(flushAutomatically = true)
  @Query(value = """
      WITH item_stats AS (
        SELECT statistics_field_id, s_counter
        FROM statistics WHERE item_id = :itemId AND s_counter <> 0
      )
      UPDATE statistics s
      SET s_counter = GREATEST(0, s.s_counter - ist.s_counter)
      FROM item_stats ist
      WHERE s.statistics_field_id = ist.statistics_field_id
        AND s.launch_id = :launchId
      """, nativeQuery = true)
  void subtractItemStatsFromLaunch(@Param("itemId") Long itemId,
      @Param("launchId") Long launchId);

  /**
   * Increments statistics fields by 1 for all items in the path (ancestors + self). Uses UPSERT
   * (INSERT ... ON CONFLICT DO UPDATE) for efficient bulk operations.
   *
   * @param pathIds  array of item IDs (ancestors path including self)
   * @param fieldIds array of statistics field IDs to increment
   */
  @Modifying(flushAutomatically = true)
  @Query(value = """
      WITH path_items AS (SELECT unnest(CAST(:pathIds AS bigint[])) AS item_id),
           fields AS (SELECT unnest(CAST(:fieldIds AS bigint[])) AS fid)
      INSERT INTO statistics (s_counter, statistics_field_id, item_id)
      SELECT 1, f.fid, p.item_id
      FROM fields f CROSS JOIN path_items p
      ORDER BY f.fid, p.item_id
      ON CONFLICT (statistics_field_id, item_id)
          DO UPDATE SET s_counter = statistics.s_counter + 1
      """, nativeQuery = true)
  void incrementForAncestors(@Param("pathIds") Long[] pathIds,
      @Param("fieldIds") Long[] fieldIds);

  /**
   * Increments statistics fields by 1 for the launch. Uses UPSERT (INSERT ... ON CONFLICT DO
   * UPDATE) for efficient operations.
   *
   * @param launchId launch ID
   * @param fieldIds array of statistics field IDs to increment
   */
  @Modifying(flushAutomatically = true)
  @Query(value = """
      WITH fields AS (SELECT unnest(CAST(:fieldIds AS bigint[])) AS fid)
      INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
      SELECT 1, f.fid, :launchId
      FROM fields f
      ON CONFLICT (statistics_field_id, launch_id)
          DO UPDATE SET s_counter = statistics.s_counter + 1
      """, nativeQuery = true)
  void incrementForLaunch(@Param("launchId") Long launchId,
      @Param("fieldIds") Long[] fieldIds);

  /**
   * Decrements statistics fields by the given amount for all items in the path. Uses GREATEST(0,
   * ...) to prevent negative values.
   *
   * @param pathIds  array of item IDs (ancestors path including self)
   * @param fieldIds array of statistics field IDs to decrement
   * @param amount   amount to decrement
   */
  @Modifying(flushAutomatically = true)
  @Query(value = """
      UPDATE statistics
      SET s_counter = GREATEST(0, s_counter - :amount)
      WHERE statistics_field_id = ANY(CAST(:fieldIds AS bigint[]))
        AND item_id = ANY(CAST(:pathIds AS bigint[]))
      """, nativeQuery = true)
  void decrementForAncestors(@Param("pathIds") Long[] pathIds,
      @Param("fieldIds") Long[] fieldIds,
      @Param("amount") int amount);

  /**
   * Decrements statistics fields by the given amount for the launch. Uses GREATEST(0, ...) to
   * prevent negative values.
   *
   * @param launchId launch ID
   * @param fieldIds array of statistics field IDs to decrement
   * @param amount   amount to decrement
   */
  @Modifying(flushAutomatically = true)
  @Query(value = """
      UPDATE statistics
      SET s_counter = GREATEST(0, s_counter - :amount)
      WHERE statistics_field_id = ANY(CAST(:fieldIds AS bigint[]))
        AND launch_id = :launchId
      """, nativeQuery = true)
  void decrementForLaunch(@Param("launchId") Long launchId,
      @Param("fieldIds") Long[] fieldIds,
      @Param("amount") int amount);


  @Query(value = """
      SELECT EXISTS(SELECT 1
                    FROM   test_item
                    WHERE  item_id = :itemId
                           AND type = 'STEP'
                           AND has_children = false
                           AND has_stats = true
                           AND retry_of IS NULL)
      """, nativeQuery = true)
  boolean canHaveExecutionStats(Long itemId);

  @Query(value = """
      SELECT EXISTS(SELECT 1
                    FROM   test_item
                    WHERE  item_id = :itemId
                           AND has_children = false
                           AND has_stats = true
                           AND retry_of IS NULL)
      """, nativeQuery = true)
  boolean canHaveIssueStats(Long itemId);


  @Query(value = """
      SELECT item_id,
             launch_id,
             path::varchar
      FROM   test_item ti
      JOIN   test_item_results tir
      ON     ti.item_id = tir.result_id
      WHERE  ti.launch_id = :launchId
      AND    ti.has_children = false
      AND    ti.has_stats = true
      AND    tir.status = 'INTERRUPTED'
      """, nativeQuery = true)
  List<TestItemPathContext> selectInterruptedItems(Long launchId);

  @Query(value = """
      SELECT EXISTS(SELECT 1
                    FROM   statistics
                    WHERE  item_id = :itemId)
      """, nativeQuery = true)
  boolean hasStatistics(Long itemId);
}
