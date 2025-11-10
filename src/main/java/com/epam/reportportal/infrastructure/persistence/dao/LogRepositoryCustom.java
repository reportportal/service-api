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
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.NestedItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.NestedItemPage;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.log.Log;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Pavel Bortnik
 */
public interface LogRepositoryCustom extends FilterableRepository<Log> {

  /**
   * Checks if the test item has any logs.
   *
   * @param itemId Item id
   * @return true if logs were found
   */
  boolean hasLogs(Long itemId);

  /**
   * Load specified number of last logs for specified test item. binaryData field will be loaded if it specified in
   * appropriate input parameter, all other fields will be fully loaded.
   *
   * @param limit  Max count of logs to be loaded
   * @param itemId Test Item log belongs to
   * @return Found logs
   */
  List<Log> findByTestItemId(Long itemId, int limit);

  /**
   * Load specified number of last logs for specified test item. binaryData field will be loaded if it specified in
   * appropriate input parameter, all other fields will be fully loaded.
   *
   * @param itemId Test Item log belongs to
   * @return Found logs
   */
  List<Log> findByTestItemId(Long itemId);

  /**
   * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
   * @param itemIds  {@link List} of the {@link Log#getTestItem()} IDs
   * @param logLevel {@link Log#getLogLevel()}
   * @return {@link List} of {@link Log}
   */
  List<Log> findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId,
      List<Long> itemIds, int logLevel);

  /**
   * Find logs as {@link IndexLog} under {@link TestItem} and group by {@link Log#getTestItem()} ID
   *
   * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
   * @param itemIds  {@link List} of the {@link Log#getTestItem()} IDs
   * @param logLevel {@link Log#getLogLevel()}
   * @return {@link List} of {@link Log}
   */
  Map<Long, List<IndexLog>> findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(
      Long launchId, List<Long> itemIds,
      int logLevel);

  /**
   * Find n latest logs for item
   *
   * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
   * @param itemId   {@link List} of the {@link Log#getTestItem()} IDs
   * @param logLevel {@link Log#getLogLevel()}
   * @param limit    Number of logs to be fetch
   * @return {@link List} of {@link Log}
   */
  List<Log> findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId,
      Long itemId, int logLevel, int limit);

  /**
   * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
   * @param itemIds  {@link List} of the {@link Log#getTestItem()} IDs
   * @param limit    Max count of {@link Log} to be loaded
   * @return {@link List} of {@link Log}
   */
  List<Log> findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(Long launchId, List<Long> itemIds,
      int limit);

  List<Long> findIdsByFilter(Queryable filter);

  List<Long> findIdsByTestItemId(Long testItemId);

  /**
   * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
   * @param itemIds  {@link List} of the {@link Log#getTestItem()} IDs
   * @param logLevel {@link Log#getLogLevel()}
   * @return {@link List} of {@link Log#getId()}
   */
  List<Long> findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId,
      List<Long> itemIds, int logLevel);

  List<Long> findItemLogIdsByLaunchIdAndLogLevelGte(Long launchId, int logLevel);

  List<Long> findItemLogIdsByLaunchIdsAndLogLevelGte(List<Long> launchIds, int logLevel);

  List<Long> findIdsByTestItemIdsAndLogLevelGte(List<Long> itemIds, int logLevel);

  /**
   * Get the specified log's page number
   *
   * @param id       ID of log page should be found of
   * @param filter   Filter
   * @param pageable Page details
   * @return Page number log found using specified filter
   */
  Integer getPageNumber(Long id, Filter filter, Pageable pageable);

  /**
   * True if the {@link TestItem} with matching 'status' and 'launchId' has {@link Log}'s with {@link Log#lastModified}
   * up to the current point of time minus provided 'period'
   *
   * @param period   {@link Duration}
   * @param launchId {@link com.epam.reportportal.infrastructure.persistence.entity.launch.Launch#id}
   * @param statuses {@link StatusEnum}
   * @return true if logs(the log) exist(exists)
   */
  boolean hasLogsAddedLately(Duration period, Long launchId, StatusEnum... statuses);

  /**
   * @param period      {@link Duration}
   * @param testItemIds Collection of the {@link TestItem#itemId} referenced from {@link Log#testItem}
   * @return Count of removed logs
   */
  int deleteByPeriodAndTestItemIds(Duration period, Collection<Long> testItemIds);

  /**
   * @param period    {@link Duration}
   * @param launchIds Collection of the {@link Launch#getId()} referenced from {@link Log#getLaunch()}
   * @return Count of removed logs
   */
  int deleteByPeriodAndLaunchIds(Duration period, Collection<Long> launchIds);

  /**
   * Retrieve {@link Log} and {@link TestItem} entities' ids, differentiated by entity type
   * <p>
   * {@link Log} and {@link TestItem} entities filtered and sorted on the DB level and returned as UNION parsed into the
   * {@link NestedItem} entity
   *
   * @param parentId          {@link TestItem#itemId} of the parent item
   * @param filter            {@link Queryable}
   * @param excludeEmptySteps Exclude steps without content (logs and child items)
   * @param excludeLogs       Exclude logs selection
   * @param pageable          {@link Pageable}
   * @return {@link Page} with {@link NestedItem} as content
   */
  Page<NestedItem> findNestedItems(Long parentId, boolean excludeEmptySteps, boolean excludeLogs,
      Queryable filter, Pageable pageable);

  /**
   * Retrieve {@link Log} and {@link TestItem} entities' ids, differentiated by entity type
   * <p>
   * {@link Log} and {@link TestItem} entities filtered and sorted on the DB level and returned as UNION parsed into the
   * {@link NestedItemPage} entity with page where item is located
   *
   * @param parentId          {@link TestItem#itemId} of the parent item
   * @param filter            {@link Queryable}
   * @param excludeEmptySteps Exclude steps without content (logs and child items)
   * @param excludeLogs       Exclude logs selection
   * @param pageable          {@link Pageable}
   * @return {@link Page} with {@link NestedItemPage} as content
   */
  List<NestedItemPage> findNestedItemsWithPage(Long parentId, boolean excludeEmptySteps,
      boolean excludeLogs,
      Queryable filter, Pageable pageable);

  /**
   * Retrieves log message of specified test item with log level greater or equals than {@code level}
   *
   * @param launchId @link TestItem#getLaunchId()}
   * @param itemId   ID of {@link Log#getTestItem()}
   * @param path     {@link TestItem#getPath()}
   * @param level    log level
   * @return {@link List} of {@link String} of log messages
   */
  List<String> findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(Long launchId, Long itemId,
      String path, Integer level);

  /**
   * Retrieves log message id of specified test item with log level greater or equals than {@code level}
   *
   * @param launchId @link TestItem#getLaunchId()}
   * @param itemId   ID of {@link Log#getTestItem()}
   * @param path     {@link TestItem#getPath()}
   * @param level    log level
   * @return {@link List} of {@link String} of log messages
   */
  List<Long> findIdsByLaunchIdAndItemIdAndPathAndLevelGte(Long launchId, Long itemId, String path,
      Integer level);

  int deleteByProjectId(Long projectId);
}
