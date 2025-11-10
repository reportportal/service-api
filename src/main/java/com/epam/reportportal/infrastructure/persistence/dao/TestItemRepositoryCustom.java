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

import com.epam.reportportal.infrastructure.model.analyzer.IndexTestItem;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.NestedStep;
import com.epam.reportportal.infrastructure.persistence.entity.item.PathName;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.infrastructure.persistence.entity.item.history.TestItemHistory;
import com.epam.reportportal.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.statistics.Statistics;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;

/**
 * @author Pavel Bortnik
 */
public interface TestItemRepositoryCustom extends FilterableRepository<TestItem> {

  /**
   * Gets accumulated statistics of items queried by provided filter
   *
   * @param filter {@link Queryable} with
   *               {@link
   *               com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#TEST_ITEM_TARGET}
   * @return Set of accumulated statistics;
   */
  Set<Statistics> accumulateStatisticsByFilter(Queryable filter);

  Set<Statistics> accumulateStatisticsByFilterNotFromBaseline(Queryable targetFilter,
      Queryable baselineFilter);

  Optional<Long> findIdByFilter(Queryable filter, Sort sort);

  /**
   * Executes query built for given filters and maps result for given page
   *
   * @param isLatest         Flag for retrieving only latest launches
   * @param launchFilter     {@link Queryable} with
   *                         {@link
   *                         com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#LAUNCH_TARGET}
   * @param testItemFilter   {@link Queryable} with
   *                         {@link
   *                         com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#TEST_ITEM_TARGET}
   * @param launchPageable   {@link Pageable} for
   *                         {@link com.epam.reportportal.infrastructure.persistence.entity.launch.Launch} query
   * @param testItemPageable {@link Pageable} for {@link TestItem} query
   * @return List of mapped entries found
   */
  Page<TestItem> findByFilter(boolean isLatest, Queryable launchFilter, Queryable testItemFilter,
      Pageable launchPageable,
      Pageable testItemPageable);

  Page<TestItem> findAllNotFromBaseline(Queryable targetFilter, Queryable baselineFilter,
      Pageable pageable);

  /**
   * Loads items {@link TestItemHistory} - {@link TestItem} executions from the whole
   * {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} grouped by
   * {@link TestItem#getTestCaseHash()} and ordered by {@link TestItem#getStartTime()} `DESCENDING` within group. Max
   * group size equals to the provided `historyDepth` value.
   *
   * @param filter       {@link Queryable}
   * @param pageable     {@link Pageable}
   * @param projectId    {@link Project#getId()}
   * @param historyDepth max {@link TestItemHistory} group size
   * @return {@link Page} with {@link TestItemHistory} as content
   */
  Page<TestItemHistory> loadItemsHistoryPage(Queryable filter, Pageable pageable, Long projectId,
      int historyDepth, boolean usingHash);

  /**
   * Loads items {@link TestItemHistory} - {@link TestItem} executions from the
   * {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} with provided `projectId` and
   * {@link Launch#getName()} equal to the provided `launchName` value. Result is grouped by
   * {@link TestItem#getTestCaseHash()} and is ordered by {@link TestItem#getStartTime()} `DESCENDING` within group. Max
   * group size equals to the provided `historyDepth` value.
   *
   * @param filter       {@link Queryable}
   * @param pageable     {@link Pageable}
   * @param projectId    {@link Project#getId()}
   * @param launchName   Name of the {@link Launch} which {@link TestItem} should be retrieved
   * @param historyDepth Max {@link TestItemHistory} group size
   * @return {@link Page} with {@link TestItemHistory} as content
   */
  Page<TestItemHistory> loadItemsHistoryPage(Queryable filter, Pageable pageable, Long projectId,
      String launchName, int historyDepth,
      boolean usingHash);

  /**
   * Loads items {@link TestItemHistory} - {@link TestItem} executions from the
   * {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} with provided `projectId` and
   * {@link Launch} which IDs are in provided `launchIds`. Result is grouped by {@link TestItem#getTestCaseHash()} and
   * is ordered by {@link TestItem#getStartTime()} `DESCENDING` within group. Max group size equals to the provided
   * `historyDepth` value.
   *
   * @param filter       {@link Queryable}
   * @param pageable     {@link Pageable}
   * @param projectId    {@link Project#getId()}
   * @param launchIds    IDs of the {@link Launch}es which {@link TestItem} should be retrieved
   * @param historyDepth Max {@link TestItemHistory} group size
   * @return {@link Page} with {@link TestItemHistory} as content
   */
  Page<TestItemHistory> loadItemsHistoryPage(Queryable filter, Pageable pageable, Long projectId,
      List<Long> launchIds, int historyDepth,
      boolean usingHash);

  /**
   * Loads items {@link TestItemHistory} - {@link TestItem} executions from the whole
   * {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} grouped by
   * {@link TestItem#getTestCaseHash()} ordered by {@link TestItem#getStartTime()} `DESCENDING` within group. Max group
   * size equals to the provided `historyDepth` value. Items result query is built from filters with
   * {@link com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#LAUNCH_TARGET} and
   * {@link com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#TEST_ITEM_TARGET}
   *
   * @param isLatest         Flag for retrieving only latest launches
   * @param launchFilter     {@link Queryable} for {@link Launch} query
   * @param testItemFilter   {@link Queryable} for {@link TestItem} query
   * @param launchPageable   {@link Pageable} for {@link Launch} query
   * @param testItemPageable {@link Pageable} for {@link TestItem} query
   * @param projectId        {@link Project#getId()}
   * @param historyDepth     Max {@link TestItemHistory} group size
   * @return {@link Page} with {@link TestItemHistory} as content
   */
  Page<TestItemHistory> loadItemsHistoryPage(boolean isLatest, Queryable launchFilter,
      Queryable testItemFilter, Pageable launchPageable,
      Pageable testItemPageable, Long projectId, int historyDepth, boolean usingHash);

  /**
   * Loads items {@link TestItemHistory} - {@link TestItem} executions from the
   * {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} with provided `projectId` and
   * {@link Launch#getName()} equal to the provided `launchName` value. Result is grouped by
   * {@link TestItem#getTestCaseHash()} and is ordered by {@link TestItem#getStartTime()} `DESCENDING` within group. Max
   * group size equals to the provided `historyDepth` value. Items result query is built from filters with
   * {@link com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#LAUNCH_TARGET} and
   * {@link com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#TEST_ITEM_TARGET}
   *
   * @param isLatest         Flag for retrieving only latest launches
   * @param launchFilter     {@link Queryable} for {@link Launch} query
   * @param testItemFilter   {@link Queryable} for {@link TestItem} query
   * @param launchPageable   {@link Pageable} for {@link Launch} query
   * @param testItemPageable {@link Pageable} for {@link TestItem} query
   * @param projectId        {@link Project#getId()}
   * @param launchName       Name of the {@link Launch} which {@link TestItem} should be retrieved
   * @param historyDepth     Max {@link TestItemHistory} group size
   * @return {@link Page} with {@link TestItemHistory} as content
   */
  Page<TestItemHistory> loadItemsHistoryPage(boolean isLatest, Queryable launchFilter,
      Queryable testItemFilter, Pageable launchPageable,
      Pageable testItemPageable, Long projectId, String launchName, int historyDepth,
      boolean usingHash);

  /**
   * Selects all descendants of TestItem with provided id.
   *
   * @param itemId TestItem id
   * @return List of all descendants
   */
  List<TestItem> selectAllDescendants(Long itemId);

  /**
   * Selects all descendants of TestItem with provided id, which has at least one child.
   *
   * @param itemId TestItem id
   * @return List of descendants
   */
  List<TestItem> selectAllDescendantsWithChildren(Long itemId);

  List<Long> findTestItemIdsByLaunchId(@Param("launchId") Long launchId, Pageable pageable);

  /**
   * Select common items object that have provided status for specified launch.
   *
   * @param launchId Launch
   * @param statuses Statuses
   * @return List of items
   */
  List<TestItem> selectItemsInStatusByLaunch(Long launchId, StatusEnum... statuses);

  /**
   * Select common items object that have provided status for specified parent item.
   *
   * @param parentId Parent item
   * @param statuses Statuses
   * @return List of items
   */
  List<TestItem> selectItemsInStatusByParent(Long parentId, StatusEnum... statuses);

  /**
   * True if the provided launch contains any items with a specified status.
   *
   * @param launchId Checking launch id
   * @param statuses Checking statuses
   * @return True if contains, false if not
   */
  Boolean hasItemsInStatusByLaunch(Long launchId, StatusEnum... statuses);

  /**
   * Select items that has different issue from provided for specified launch.
   *
   * @param launchId Launch
   * @param locator  Issue type locator
   * @return List of items
   */
  List<TestItem> findAllNotInIssueByLaunch(Long launchId, String locator);

  /**
   * Select items that has different issue from provided for specified launch.
   *
   * @param launchId Launch
   * @param locator  Issue type locator
   * @return List of items
   */
  List<Long> selectIdsNotInIssueByLaunch(Long launchId, String locator);

  List<TestItem> findAllNotInIssueGroupByLaunch(Long launchId, TestItemIssueGroup issueGroup);

  List<Long> selectIdsNotInIssueGroupByLaunch(Long launchId, TestItemIssueGroup issueGroup);

  List<TestItem> findAllInIssueGroupByLaunch(Long launchId, TestItemIssueGroup issueGroup);

  List<TestItem> findItemsForAnalyze(Long launchId);

  List<TestItem> selectTestItemsProjection(Long launchId);

  /**
   * Select all {@link TestItem#getItemId()} of {@link TestItem} with attached {@link Issue} and
   * {@link TestItem#getLaunchId()} equal to provided `launchId`
   *
   * @param launchId {@link TestItem#getLaunchId()}
   * @return {@link List} of {@link TestItem#getItemId()}
   */
  List<Long> selectIdsWithIssueByLaunch(Long launchId);

  /**
   * True if the {@link TestItem} with matching 'status' and 'launchId' was started within the provided 'period'
   *
   * @param period   {@link Duration}
   * @param launchId {@link com.epam.reportportal.infrastructure.persistence.entity.launch.Launch#id}
   * @param statuses {@link StatusEnum}
   * @return true if items(the item) exist(exists)
   */
  Boolean hasItemsInStatusAddedLately(Long launchId, Duration period, StatusEnum... statuses);

  /**
   * True if {@link TestItem} wasn't modified before the provided 'period' and has logs
   *
   * @param launchId {@link com.epam.reportportal.infrastructure.persistence.entity.launch.Launch#id}
   * @param period   {@link Duration}
   * @param statuses {@link StatusEnum}
   * @return true if {@link TestItem} wasn't modified before the provided 'period' and has logs
   */
  Boolean hasLogs(Long launchId, Duration period, StatusEnum... statuses);

  /**
   * Select test items that has issue with provided issue type for specified launch.
   *
   * @param launchId  Launch id
   * @param issueType Issue type
   * @return List of items
   */
  List<TestItem> selectItemsInIssueByLaunch(Long launchId, String issueType);

  List<TestItem> selectRetries(List<Long> retryOfIds);

  //TODO move to project repo
  List<IssueType> selectIssueLocatorsByProject(Long projectId);

  /**
   * Selects issue type object by provided locator for specified project.
   *
   * @param projectId Project id
   * @param locator   Issue type locator
   * @return Issue type
   */
  Optional<IssueType> selectIssueTypeByLocator(Long projectId, String locator);

  /**
   * Select id and path for item by uuid
   *
   * @param uuid {@link TestItem#getUuid()} ()}
   * @return id from collection -> {@link PathName}
   */
  Optional<Pair<Long, String>> selectPath(String uuid);

  /**
   * Select {@link PathName} containing ids and names of all items in a tree till current and launch name and number for
   * each item id from the provided collection
   *
   * @param testItems {@link Collection} of {@link TestItem}
   * @return testItemId from collection -> {@link PathName}
   */
  Map<Long, PathName> selectPathNames(Collection<TestItem> testItems);

  /**
   * Select item IDs by analyzed status and {@link TestItem#getLaunchId()} with {@link Log} having
   * {@link Log#getLogLevel()} greater than or equal to
   * {@link com.epam.reportportal.infrastructure.persistence.entity.enums.LogLevel#ERROR_INT} excluding {@link TestItem}
   * that has {@link IssueEntity} with {@link IssueEntity#getIssueType()} from excluded types
   *
   * @param autoAnalyzed       {@link Issue#getAutoAnalyzed()}
   * @param launchId           {@link TestItem#getLaunchId()}
   * @param logLevel           {@link Log#getLogLevel()}
   * @param excludedIssueTypes {@link Collection} of {@link IssueType#getId()} to exclude {@link TestItem} with provided
   *                           type id
   * @return The {@link List} of the {@link TestItem#getItemId()}
   */
  List<Long> selectIdsByAnalyzedWithLevelGteExcludingIssueTypes(boolean autoAnalyzed,
      boolean ignoreAnalyzer, Long launchId, int logLevel,
      Collection<IssueType> excludedIssueTypes);

  /**
   * @param itemId  {@link TestItem#itemId}
   * @param status  New status
   * @param endTime {@link com.epam.reportportal.infrastructure.persistence.entity.item.TestItemResults#endTime}
   * @return 1 if updated, otherwise 0
   */
  int updateStatusAndEndTimeById(Long itemId, JStatusEnum status, Instant endTime);

  /**
   * @param retryOfId {@link TestItem#getRetryOf()}
   * @param from      Previous item {@link TestItemResults#getStatus()} criteria to update
   * @param to        New {@link TestItemResults#getStatus()}
   * @param endTime   New {@link TestItemResults#getEndTime()}
   * @return amount of updated items
   */
  int updateStatusAndEndTimeByRetryOfId(Long retryOfId, JStatusEnum from, JStatusEnum to,
      Instant endTime);

  /**
   * @param itemId {@link TestItem#itemId}
   * @return {@link TestItemTypeEnum}
   */
  TestItemTypeEnum getTypeByItemId(Long itemId);

  /**
   * @param launchId {@link TestItem#getLaunchId()}
   * @param filter   {@link Queryable} for additional dynamic filtering
   * @param limit    query limit
   * @param offset   query offset
   * @return {@link List} of {@link TestItem#getItemId()}
   */
  List<Long> selectIdsByFilter(Long launchId, Queryable filter, int limit, int offset);

  /**
   * Select ids of items that has descendants
   *
   * @param itemIds {@link Collection} of {@link TestItem#getItemId()} that should be filtered by having descendants
   * @return {@link List} of {@link TestItem#getItemId()}
   */
  List<Long> selectIdsByHasDescendants(Collection<Long> itemIds);

  /**
   * Select item IDs which log's level is greater than or equal to provided and log's message match to the STRING
   * pattern
   *
   * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
   * @param logLevel {@link Log#getLogLevel()}
   * @param pattern  CASE SENSITIVE STRING pattern for log message search
   * @return The {@link List} of the {@link TestItem#getItemId()}
   */
  List<Long> selectIdsByStringLogMessage(Collection<Long> itemIds, Integer logLevel,
      String pattern);

  /**
   * Select item IDs which log's level is greater than or equal to provided and log's message match to the REGEX
   * pattern
   *
   * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
   * @param logLevel {@link Log#getLogLevel()}
   * @param pattern  REGEX pattern for log message search
   * @return The {@link List} of the {@link TestItem#getItemId()}
   */
  List<Long> selectIdsByRegexLogMessage(Collection<Long> itemIds, Integer logLevel, String pattern);

  /**
   * Select Log IDs which log's level is greater than or equal to provided.
   *
   * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
   * @param logLevel {@link Log#getLogLevel()}
   * @return The {@link List} of the {@link TestItem#getItemId()}
   */
  List<Long> selectLogIdsWithLogLevelCondition(Collection<Long> itemIds, Integer logLevel);

  /**
   * Select item IDs which descendants' log's level is greater than or equal to provided and log's message match to the
   * REGEX pattern
   *
   * @param launchId {@link TestItem#getLaunchId()}
   * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
   * @param logLevel {@link Log#getLogLevel()}
   * @param pattern  REGEX pattern for log message search
   * @return The {@link List} of the {@link TestItem#getItemId()}
   */
  List<Long> selectIdsUnderByStringLogMessage(Long launchId, Collection<Long> itemIds,
      Integer logLevel, String pattern);

  /**
   * Select Log IDs which descendants' log's level is greater than or equal to provided.
   *
   * @param launchId {@link TestItem#getLaunchId()}
   * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
   * @param logLevel {@link Log#getLogLevel()}
   * @return The {@link List} of the {@link TestItem#getItemId()}
   */
  List<Long> selectLogIdsUnderWithLogLevelCondition(Long launchId, Collection<Long> itemIds,
      Integer logLevel);

  /**
   * Select item IDs which descendants' log's level is greater than or equal to provided and log's message match to the
   * REGEX pattern
   *
   * @param launchId {@link TestItem#getLaunchId()}
   * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
   * @param logLevel {@link Log#getLogLevel()}
   * @param pattern  REGEX pattern for log message search
   * @return The {@link List} of the {@link TestItem#getItemId()}
   */
  List<Long> selectIdsUnderByRegexLogMessage(Long launchId, Collection<Long> itemIds,
      Integer logLevel, String pattern);

  /**
   * Select {@link NestedStep} entities by provided 'IDs' with {@link NestedStep#attachmentsCount} of the
   * {@link com.epam.reportportal.infrastructure.persistence.entity.log.Log} entities of all descendants for each
   * {@link NestedStep} and {@link NestedStep#hasContent} flag to check whether entity is a last one in the descendants
   * tree or there are {@link com.epam.reportportal.infrastructure.persistence.entity.log.Log} or {@link NestedStep}
   * entities exist under it
   *
   * @param ids       {@link Collection} of the {@link TestItem#itemId}
   * @param logFilter {@link Queryable} with {@link com.epam.reportportal.infrastructure.persistence.entity.log.Log}
   *                  target, to evaluate 'hasContent' flag and   attachments count
   * @return {@link List} of the {@link NestedStep}
   */

  List<NestedStep> findAllNestedStepsByIds(Collection<Long> ids, Queryable logFilter,
      boolean excludePassedLogs);

  List<IndexTestItem> findIndexTestItemByLaunchId(Long launchId,
      Collection<JTestItemTypeEnum> itemTypes);
}
