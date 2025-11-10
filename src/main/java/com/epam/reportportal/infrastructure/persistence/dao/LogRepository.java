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

package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.model.analyzer.IndexLog;
import com.epam.reportportal.infrastructure.persistence.entity.log.Log;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Pavel Bortnik
 */
public interface LogRepository extends ReportPortalRepository<Log, Long>, LogRepositoryCustom {

  Optional<Log> findByUuid(String uuid);

  List<Log> findLogsByLogTime(Timestamp timestamp);

  @Query(value = """
      WITH ParentPath AS (
          SELECT path
          FROM test_item ti2
          WHERE ti2.item_id = (
              SELECT parent_id
              FROM test_item ti3
              WHERE ti3.item_id = :itemId
          )
      ),
      TargetPath AS (
          SELECT cast(concat(pp.path, '.', :itemId) as ltree) AS path
          FROM ParentPath pp
      ),
      FilteredItems AS (
          SELECT ti.item_id
          FROM test_item ti
          WHERE ti.path <@ (SELECT path FROM TargetPath)
      )
      SELECT
          log.id AS logId,
          log.log_level AS logLevel,
          log.log_time AS logTime,
          log.log_message AS message,
          clusters.index_id AS clusterId
      FROM log
      LEFT JOIN clusters ON log.cluster_id = clusters.id
      WHERE log.item_id IN (SELECT item_id FROM FilteredItems)
        AND log.log_level >= :logLevel;
      """, nativeQuery = true)
  List<IndexLog> findNestedLogsOfRetryItem(@Param("itemId") Long itemId,
      @Param("logLevel") int logLevel);

  @Modifying
  @Query(value = "UPDATE log SET launch_id = :newLaunchId WHERE launch_id = :currentLaunchId", nativeQuery = true)
  void updateLaunchIdByLaunchId(@Param("currentLaunchId") Long currentLaunchId,
      @Param("newLaunchId") Long newLaunchId);

  @Modifying
  @Query(value = "UPDATE log SET cluster_id = :clusterId WHERE id IN (:ids)", nativeQuery = true)
  int updateClusterIdByIdIn(@Param("clusterId") Long clusterId, @Param("ids") Collection<Long> ids);

  @Modifying
  @Query(value = "UPDATE log SET cluster_id = NULL WHERE cluster_id IN (SELECT id FROM clusters WHERE clusters.launch_id = :launchId)", nativeQuery = true)
  int updateClusterIdSetNullByLaunchId(@Param("launchId") Long launchId);

  @Modifying
  @Query(value = "UPDATE log SET cluster_id = NULL WHERE cluster_id IS NOT NULL AND item_id IN (:itemIds)", nativeQuery = true)
  int updateClusterIdSetNullByItemIds(@Param("itemIds") Collection<Long> itemIds);
}
