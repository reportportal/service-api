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

import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

/**
 * @author Pavel Bortnik
 */
public interface TestItemRepository extends ReportPortalRepository<TestItem, Long>,
    TestItemRepositoryCustom {

  /**
   * Among the provided parent test item and its retries, finds the parent item whose direct child steps have the
   * longest continuous sequence of non-failed nested steps from the start until the first failure occurs.
   *
   * @param itemId {@link TestItem#getItemId()} of a parent item (or any of its retries) whose child steps are analyzed
   * @return {@link Long} parent item id with the maximum number of steps before the first failed step; {@code null} if
   * no matching steps are found
   */
  @Query(value = """
      WITH parent_items AS (
          SELECT DISTINCT item_id AS parent_id, start_time
          FROM test_item
          WHERE item_id = :itemId OR retry_of = :itemId
      ),
      nested_steps AS (
          SELECT par.parent_id, par.start_time,
                 SUM(CASE WHEN tir.status = 'FAILED' or ti.item_id is null THEN 1 ELSE 0 end)
                      OVER (PARTITION BY ti.parent_id ORDER BY ti.start_time
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS fail_count
        FROM parent_items par
        LEFT JOIN test_item ti ON ti.parent_id = par.parent_id
        LEFT JOIN test_item_results tir ON tir.result_id = ti.item_id
      )
      SELECT parent_id, start_time
      FROM nested_steps
      WHERE fail_count = 0
      GROUP BY parent_id, start_time
      ORDER BY COUNT(*) desc, start_time desc
      LIMIT 1;
      """, nativeQuery = true)
  Long findIdWithMaxStepsBeforeFailed(@Param("itemId") Long itemId);

  @Query(value = "SELECT * FROM test_item WHERE item_id = (SELECT parent_id FROM test_item WHERE item_id = :childId)", nativeQuery = true)
  Optional<TestItem> findParentByChildId(@Param("childId") Long childId);

  @Query(value = """
      SELECT ti.* FROM test_item ti
      INNER JOIN launch l ON ti.launch_id = l.id
      WHERE ti.has_children = false
        AND ti.has_stats = true
        AND ti.retry_of IS NULL
        AND ti.type = 'STEP'
        AND l.project_id = :projectId
        AND ti.name ILIKE %:name%
      """, nativeQuery = true)
  @QueryHints(@QueryHint(name = "javax.persistence.query.timeout", value = "10000"))
  Slice<TestItem> findTestItemsContainsName(@Param("name") String nameTerm,
      @Param("projectId") Long projectId,
      Pageable pageable);

  @Query(value = """
      SELECT ti.* FROM test_item ti
      JOIN test_item_results tir on ti.item_id = tir.result_id
      JOIN launch l ON ti.launch_id = l.id
      WHERE ti.has_children = false
        AND ti.has_stats = true
        AND ti.retry_of IS NULL
        AND ti.type = 'STEP'
        AND l.project_id = :projectId
        AND ti.name ILIKE %:name%
        AND CAST(tir.status AS VARCHAR) in (:statuses)
      """, nativeQuery = true)
  @QueryHints(@QueryHint(name = "javax.persistence.query.timeout", value = "10000"))
  Slice<TestItem> findTestItemsContainsNameAndStatuses(@Param("name") String nameTerm,
      @Param("projectId") Long projectId, @Param("statuses") List<String> statuses,
      Pageable pageable);

  @Query(value = """
      SELECT ti.* FROM test_item ti
      JOIN launch l ON ti.launch_id = l.id
      LEFT JOIN item_attribute ia ON ti.item_id = ia.item_id
      WHERE ti.has_children = false
        AND ti.has_stats = true
        AND ti.retry_of IS NULL
        AND ti.type = 'STEP'
        AND l.project_id = :projectId
        AND ia.key = :key AND ia.value = :value and ia.system = false
      """, nativeQuery = true)
  @QueryHints(@QueryHint(name = "javax.persistence.query.timeout", value = "10000"))
  Slice<TestItem> findTestItemsByAttribute(@Param("projectId") Long projectId,
      @Param("key") String attributeKey,
      @Param("value") String attributeValue,
      Pageable pageable);

  @Query(value = """
      SELECT ti.* FROM test_item ti
      JOIN test_item_results tir on ti.item_id = tir.result_id
      JOIN launch l ON ti.launch_id = l.id
      LEFT JOIN item_attribute ia ON ti.item_id = ia.item_id
      WHERE ti.has_children = false
        AND ti.has_stats = true
        AND ti.retry_of IS NULL
        AND ti.type = 'STEP'
        AND l.project_id = :projectId
        AND ia.key = :key AND ia.value = :value and ia.system = false
        AND CAST(tir.status AS VARCHAR) in (:statuses)
      """, nativeQuery = true)
  @QueryHints(@QueryHint(name = "javax.persistence.query.timeout", value = "10000"))
  Slice<TestItem> findTestItemsByAttributeAndStatuses(@Param("projectId") Long projectId,
      @Param("key") String attributeKey,
      @Param("value") String attributeValue,
      @Param("statuses") List<String> statuses,
      Pageable pageable);


  @Query(value = """
      SELECT ti.* FROM test_item ti
      JOIN launch l ON ti.launch_id = l.id
      LEFT JOIN item_attribute ia ON ti.item_id = ia.item_id
      WHERE ti.has_children = false
        AND ti.has_stats = true
        AND ti.retry_of IS NULL
        AND ti.type = 'STEP'
        AND l.project_id = :projectId
        AND ia.key is null AND ia.value = :value and ia.system = false
      """, nativeQuery = true)
  @QueryHints(@QueryHint(name = "javax.persistence.query.timeout", value = "10000"))
  Slice<TestItem> findTestItemsByAttribute(@Param("projectId") Long projectId,
      @Param("value") String attributeValue, Pageable pageable);

  @Query(value = """
      SELECT ti.* FROM test_item ti
      JOIN launch l ON ti.launch_id = l.id
      JOIN test_item_results tir on ti.item_id = tir.result_id
      LEFT JOIN item_attribute ia ON ti.item_id = ia.item_id
      WHERE ti.has_children = false
        AND ti.has_stats = true
        AND ti.retry_of IS NULL
        AND ti.type = 'STEP'
        AND l.project_id = :projectId
        AND ia.key is null AND ia.value = :value and ia.system = false
        AND CAST(tir.status AS VARCHAR) in (:statuses)
      """, nativeQuery = true)
  @QueryHints(@QueryHint(name = "javax.persistence.query.timeout", value = "10000"))
  Slice<TestItem> findTestItemsByAttributeAndStatuses(@Param("projectId") Long projectId,
      @Param("value") String attributeValue, @Param("statuses") List<String> statuses,
      Pageable pageable);

  /**
   * Retrieve list of test item ids for provided launch
   *
   * @param launchId Launch id
   * @return List of test item ids
   */
  @Query(value = "SELECT item_id FROM test_item WHERE launch_id = :launchId UNION "
      + "SELECT item_id FROM test_item WHERE retry_of IS NOT NULL AND retry_of IN "
      + "(SELECT item_id FROM test_item WHERE launch_id = :launchId);", nativeQuery = true)
  List<Long> findIdsByLaunchId(@Param("launchId") Long launchId);

  /**
   * Retrieve the {@link List} of the {@link TestItem#getItemId()} by launch ID, {@link StatusEnum#name()} and
   * {@link TestItem#isHasChildren()} == false
   *
   * @param launchId {@link Launch#getId()}
   * @param status   {@link StatusEnum#name()}
   * @return the {@link List} of the {@link TestItem#getItemId()}
   */
  @Query(value =
      "SELECT test_item.item_id FROM test_item JOIN test_item_results result ON test_item.item_id = result.result_id "
          + " WHERE test_item.launch_id = :launchId AND NOT test_item.has_children "
          + " AND result.status = CAST(:#{#status.name()} AS STATUS_ENUM) ORDER BY test_item.item_id LIMIT :pageSize OFFSET :pageOffset", nativeQuery = true)
  List<Long> findIdsByNotHasChildrenAndLaunchIdAndStatus(@Param("launchId") Long launchId,
      @Param("status") StatusEnum status,
      @Param("pageSize") Integer limit, @Param("pageOffset") Long offset);

  /**
   * Retrieve the {@link List} of the {@link TestItem#getItemId()} by launch ID, {@link StatusEnum#name()} and
   * {@link TestItem#isHasChildren()} == true ordered (DESCENDING) by 'nlevel' of the {@link TestItem#getPath()}
   *
   * @param launchId {@link Launch#getId()}
   * @param status   {@link StatusEnum#name()}
   * @return the {@link List} of the {@link TestItem#getItemId()}
   * @see <a
   * href="https://www.postgresql.org/docs/current/ltree.html">https://www.postgresql.org/docs/current/ltree.html</a>
   */
  @Query(value =
      "SELECT test_item.item_id FROM test_item JOIN test_item_results result ON test_item.item_id = result.result_id "
          + " WHERE test_item.launch_id = :launchId AND test_item.has_children AND result.status = CAST(:#{#status.name()} AS STATUS_ENUM)"
          + " ORDER BY nlevel(test_item.path) DESC, test_item.item_id LIMIT :pageSize OFFSET :pageOffset", nativeQuery = true)
  List<Long> findIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(
      @Param("launchId") Long launchId,
      @Param("status") StatusEnum status, @Param("pageSize") Integer limit,
      @Param("pageOffset") Long offset);

  /**
   * Retrieve the {@link Stream} of the {@link TestItem#getItemId()} under parent {@link TestItem#getPath()},
   * {@link StatusEnum#name()} and {@link TestItem#isHasChildren()} == false
   *
   * @param parentPath {@link TestItem#getPath()} of the parent item
   * @param status     {@link StatusEnum#name()}
   * @return the {@link List} of the {@link TestItem#getItemId()}
   */
  @Query(value =
      "SELECT test_item.item_id FROM test_item JOIN test_item_results result ON test_item.item_id = result.result_id "
          + " WHERE CAST(:parentPath AS LTREE) @> test_item.path AND CAST(:parentPath AS LTREE) != test_item.path "
          + " AND NOT test_item.has_children AND result.status = CAST(:#{#status.name()} AS STATUS_ENUM) ORDER BY test_item.item_id LIMIT :pageSize OFFSET :pageOffset", nativeQuery = true)
  List<Long> findIdsByNotHasChildrenAndParentPathAndStatus(@Param("parentPath") String parentPath,
      @Param("status") StatusEnum status,
      @Param("pageSize") Integer limit, @Param("pageOffset") Long offset);

  /**
   * Retrieve the {@link Stream} of the {@link TestItem#getItemId()} under parent {@link TestItem#getPath()},
   * {@link StatusEnum#name()} and {@link TestItem#isHasChildren()} == true ordered (DESCENDING) by 'nlevel' of the
   * {@link TestItem#getPath()}
   *
   * @param parentPath {@link TestItem#getPath()} of the parent item
   * @param status     {@link StatusEnum#name()}
   * @return the {@link List} of the {@link TestItem#getItemId()}
   * @see <a
   * href="https://www.postgresql.org/docs/current/ltree.html">https://www.postgresql.org/docs/current/ltree.html</a>
   */
  @Query(value =
      "SELECT test_item.item_id FROM test_item JOIN test_item_results result ON test_item.item_id = result.result_id "
          + " WHERE CAST(:parentPath AS LTREE) @> test_item.path AND CAST(:parentPath AS LTREE) != test_item.path "
          + " AND test_item.has_children AND result.status = CAST(:#{#status.name()} AS STATUS_ENUM)"
          + " ORDER BY nlevel(test_item.path) DESC, test_item.item_id LIMIT :pageSize OFFSET :pageOffset", nativeQuery = true)
  List<Long> findIdsByHasChildrenAndParentPathAndStatusOrderedByPathLevel(
      @Param("parentPath") String parentPath,
      @Param("status") StatusEnum status, @Param("pageSize") Integer limit,
      @Param("pageOffset") Long offset);

  List<TestItem> findTestItemsByUniqueId(String uniqueId);

  List<TestItem> findTestItemsByLaunchId(Long launchId);

  Optional<TestItem> findByUuid(String uuid);

  /**
   * Finds {@link TestItem#getItemId()} by {@link TestItem#getUuid()} and sets a lock on the found 'item' row in the
   * database. Required for fetching 'item' from the concurrent environment to provide synchronization between dependant
   * entities
   *
   * @param uuid {@link TestItem#getUuid()}
   * @return {@link Optional} with {@link TestItem} object
   */
  @Query(value = "SELECT ti.item_id FROM test_item ti WHERE ti.uuid = :uuid FOR UPDATE", nativeQuery = true)
  Optional<Long> findIdByUuidForUpdate(@Param("uuid") String uuid);

  /**
   * Finds all {@link TestItem} by specified launch id
   *
   * @param launchId {@link Launch#getId()}
   * @return {@link List} of {@link TestItem} ordered by {@link TestItem#getStartTime()} ascending
   */
  List<TestItem> findTestItemsByLaunchIdOrderByStartTimeAsc(Long launchId);

  /**
   * Execute sql-function that changes a structure of retries according to the MAX {@link TestItem#getStartTime()}. If
   * the new-inserted {@link TestItem} with specified {@link TestItem#getItemId()} is a retry and it has
   * {@link TestItem#getStartTime()} greater than MAX {@link TestItem#getStartTime()} of the other {@link TestItem} with
   * the same {@link TestItem#getUniqueId()} then all those test items become retries of the new-inserted one: theirs
   * {@link TestItem#isHasRetries()} flag is set to 'false' and {@link TestItem#getRetryOf()} gets the new-inserted
   * {@link TestItem#getItemId()} value. The same operation applies to the new-inserted {@link TestItem} if its
   * {@link TestItem#getStartTime()} is less than MAX {@link TestItem#getStartTime()} of the other {@link TestItem} with
   * the same {@link TestItem#getUniqueId()}
   *
   * @param itemId The new-inserted {@link TestItem#getItemId()}
   * @deprecated {@link TestItemRepository#handleRetry(Long, Long)} should be used instead
   */
  @Deprecated
  @Query(value = "SELECT handle_retries(:itemId)", nativeQuery = true)
  void handleRetries(@Param("itemId") Long itemId);

  /**
   * Execute sql-function that changes a structure of retries assigning {@link TestItem#getRetryOf()} value of the
   * previously inserted retries and previous retries' parent to the new inserted parent id
   *
   * @param itemId      Previous retries' parent {@link TestItem#getItemId()}
   * @param retryParent The new-inserted {@link TestItem#getItemId()}
   */
  @Query(value = "SELECT handle_retry(:itemId, :retryParent)", nativeQuery = true)
  void handleRetry(@Param("itemId") Long itemId, @Param("retryParent") Long retryParent);

  @Query(value = "DELETE FROM test_item WHERE test_item.item_id = :itemId", nativeQuery = true)
  void deleteTestItem(@Param(value = "itemId") Long itemId);

  /**
   * Checks does test item have children.
   *
   * @param itemPath Current item path in a tree
   * @return True if has
   */
  @Query(value = "SELECT EXISTS(SELECT 1 FROM test_item t WHERE t.path <@ CAST(:itemPath AS LTREE) AND t.item_id != :itemId LIMIT 1)", nativeQuery = true)
  boolean hasChildren(@Param("itemId") Long itemId, @Param("itemPath") String itemPath);

  /**
   * Checks does test item have children with {@link TestItem#isHasStats()} == true.
   *
   * @param itemId Parent item id
   * @return True if has
   */
  @Query(value = "SELECT EXISTS(SELECT 1 FROM test_item t WHERE t.parent_id = :itemId AND t.has_stats)", nativeQuery = true)
  boolean hasChildrenWithStats(@Param("itemId") Long itemId);

  /**
   * Checks does test item have parent with provided status.
   *
   * @param itemId   Cuttent item id
   * @param itemPath Current item path in a tree
   * @param status   {@link StatusEnum}
   * @return 'True' if has, otherwise 'false'
   */
  @Query(value =
      "SELECT EXISTS(SELECT 1 FROM test_item ti JOIN test_item_results tir ON ti.item_id = tir.result_id"
          + " WHERE ti.path @> CAST(:itemPath AS LTREE) AND ti.has_stats = TRUE AND ti.item_id != :itemId AND tir.status = CAST(:#{#status.name()} AS STATUS_ENUM) LIMIT 1)", nativeQuery = true)
  boolean hasParentWithStatus(@Param("itemId") Long itemId, @Param("itemPath") String itemPath,
      @Param("status") StatusEnum status);

  /**
   * Check for existence of descendants with statuses NOT EQUAL to provided status
   *
   * @param parentId {@link TestItem#getParent()} ID
   * @param statuses {@link StatusEnum#name()} Array
   * @return 'true' if items with statuses NOT EQUAL to provided status exist, otherwise 'false'
   */
  @Query(value =
      "SELECT EXISTS(SELECT 1 FROM test_item ti JOIN test_item_results tir ON ti.item_id = tir.result_id"
          + " WHERE ti.parent_id = :parentId AND ti.retry_of IS NULL AND CAST(tir.status AS VARCHAR) NOT IN (:statuses))", nativeQuery = true)
  boolean hasDescendantsNotInStatus(@Param("parentId") Long parentId,
      @Param("statuses") String... statuses);

  /**
   * True if the parent item has any child items with provided status.
   *
   * @param parentId   parent item {@link TestItem#getItemId()}
   * @param parentPath parent item {@link TestItem#getPath()}
   * @param statuses   child item {@link TestItemResults#getStatus()}
   * @return True if contains, false if not
   */
  @Query(value =
      "SELECT EXISTS(SELECT 1 FROM test_item ti JOIN test_item_results tir ON ti.item_id = tir.result_id"
          + " WHERE ti.path <@ CAST(:parentPath AS LTREE) AND ti.item_id != :parentId AND CAST(tir.status AS VARCHAR) IN (:statuses))", nativeQuery = true)
  boolean hasItemsInStatusByParent(@Param("parentId") Long parentId,
      @Param("parentPath") String parentPath,
      @Param("statuses") String... statuses);

  /**
   * True if the launch has any items with issue.
   *
   * @param launchId parent item {@link TestItem#getItemId()}
   * @return True if contains, false if not
   */
  @Query(value = "SELECT EXISTS(SELECT 1 FROM test_item ti JOIN issue i ON ti.item_id = i.issue_id WHERE ti.launch_id = :launchId)", nativeQuery = true)
  boolean hasItemsWithIssueByLaunch(@Param("launchId") Long launchId);

  /**
   * Interrupts all {@link com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum#IN_PROGRESS}
   * children items of the launch with provided launchId. Sets them
   * {@link com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum#INTERRUPTED} status
   *
   * @param launchId Launch id
   */
  @Modifying
  @Query(value =
      "UPDATE test_item_results SET status = 'INTERRUPTED', end_time = CURRENT_TIMESTAMP, duration = EXTRACT(EPOCH FROM CURRENT_TIMESTAMP - i.start_time)"
          + "FROM test_item i WHERE i.item_id = result_id AND i.launch_id = :launchId AND status = 'IN_PROGRESS'", nativeQuery = true)
  void interruptInProgressItems(@Param("launchId") Long launchId);

  /**
   * Checks if all children of test item with id = {@code parentId}, except item with id = {@code stepId}, has status
   * not in provided {@code statuses}
   *
   * @param parentId Id of parent test item
   * @param stepId   Id of test item that should be ignored during the checking
   * @param statuses {@link StatusEnum#name()} Array
   * @return True if has
   */
  @Query(value =
      "SELECT EXISTS(SELECT 1 FROM test_item JOIN test_item_results result ON test_item.item_id = result.result_id "
          + " WHERE test_item.parent_id = :parentId AND test_item.item_id != :stepId AND test_item.retry_of IS NULL "
          + " AND CAST(result.status AS VARCHAR) NOT IN (:statuses))", nativeQuery = true)
  boolean hasDescendantsNotInStatusExcludingById(@Param("parentId") Long parentId,
      @Param("stepId") Long stepId,
      @Param("statuses") String... statuses);

  /**
   * Finds {@link TestItem} with specified {@code path}
   *
   * @param path Path of {@link TestItem}
   * @return {@link Optional} of {@link TestItem} if it exists, {@link Optional#empty()} if not
   */
  @Query(value = "SELECT * FROM test_item t WHERE t.path = CAST(:path AS LTREE)", nativeQuery = true)
  Optional<TestItem> findByPath(@Param("path") String path);

  /**
   * Finds latest {@link TestItem#getItemId()} with specified {@code uniqueId}, {@code launchId}, {@code parentId}
   *
   * @param uniqueId {@link TestItem#getUniqueId()}
   * @param launchId {@link TestItem#getLaunchId()}
   * @param parentId {@link TestItem#getParentId()}
   * @return {@link Optional} of {@link TestItem} if exists otherwise {@link Optional#empty()}
   */
  @Query(value =
      "SELECT t.item_id FROM test_item t WHERE t.unique_id = :uniqueId AND t.launch_id = :launchId "
          + " AND t.parent_id = :parentId AND t.has_stats AND t.retry_of IS NULL"
          + " ORDER BY t.start_time DESC, t.item_id DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
  Optional<Long> findLatestIdByUniqueIdAndLaunchIdAndParentId(@Param("uniqueId") String uniqueId,
      @Param("launchId") Long launchId,
      @Param("parentId") Long parentId);

  /**
   * Finds latest {@link TestItem#getItemId()} with specified {@code uniqueId}, {@code launchId}, {@code parentId} and
   * not equal {@code itemId}
   *
   * @param uniqueId {@link TestItem#getUniqueId()}
   * @param launchId {@link TestItem#getLaunchId()}
   * @param parentId {@link TestItem#getParentId()}
   * @param itemId   {@link TestItem#getItemId()} ()}
   * @return {@link Optional} of {@link TestItem} if exists otherwise {@link Optional#empty()}
   */
  @Query(value =
      "SELECT t.item_id FROM test_item t WHERE t.unique_id = :uniqueId AND t.launch_id = :launchId "
          + " AND t.parent_id = :parentId AND t.item_id != :itemId AND t.has_stats AND t.retry_of IS NULL"
          + " ORDER BY t.start_time DESC, t.item_id DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
  Optional<Long> findLatestIdByUniqueIdAndLaunchIdAndParentIdAndItemIdNotEqual(
      @Param("uniqueId") String uniqueId,
      @Param("launchId") Long launchId, @Param("parentId") Long parentId,
      @Param("itemId") Long itemId);

  /**
   * Finds all descendants ids of {@link TestItem} with {@code path} include its own id
   *
   * @param path Path of {@link TestItem}
   * @return {@link List<Long>} of test item ids
   */
  @Query(value = "SELECT item_id FROM test_item WHERE path <@ CAST(:path AS LTREE)", nativeQuery = true)
  List<Long> selectAllDescendantsIds(@Param("path") String path);

  void deleteAllByItemIdIn(Collection<Long> ids);

  /**
   * Finds latest root(without any parent) {@link TestItem} with specified {@code testCaseHash} and {@code launchId}
   *
   * @param testCaseHash {@link TestItem#getTestCaseHash()}
   * @param launchId     {@link TestItem#getLaunchId()}
   * @return {@link Optional} of {@link TestItem#getItemId()} if exists otherwise {@link Optional#empty()}
   */
  @Query(value =
      "SELECT t.item_id FROM test_item t WHERE t.test_case_hash = :testCaseHash AND t.launch_id = :launchId AND t.parent_id IS NULL "
          + " ORDER BY t.start_time DESC, t.item_id DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
  Optional<Long> findLatestIdByTestCaseHashAndLaunchIdWithoutParents(
      @Param("testCaseHash") Integer testCaseHash,
      @Param("launchId") Long launchId);

  /**
   * Finds latest {@link TestItem#getItemId()} with specified {@code testCaseHash}, {@code launchId} and
   * {@code parentId}
   *
   * @param testCaseHash {@link TestItem#getTestCaseHash()}
   * @param launchId     {@link TestItem#getLaunchId()}
   * @param parentId     {@link TestItem#getParentId()}
   * @return {@link Optional} of {@link TestItem#getItemId()} if exists otherwise {@link Optional#empty()}
   */
  @Query(value =
      "SELECT t.item_id FROM test_item t WHERE t.test_case_hash = :testCaseHash AND t.launch_id = :launchId "
          + " AND t.parent_id = :parentId AND t.has_stats AND t.retry_of IS NULL"
          + " ORDER BY t.start_time DESC, t.item_id DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
  Optional<Long> findLatestIdByTestCaseHashAndLaunchIdAndParentId(
      @Param("testCaseHash") Integer testCaseHash,
      @Param("launchId") Long launchId, @Param("parentId") Long parentId);

  @Query(value = "SELECT t.name FROM test_item t WHERE t.item_id = :itemId", nativeQuery = true)
  Optional<String> findItemNameByItemId(Long itemId);

  /**
   * Count items by launch id
   *
   * @param launchId Launch id
   * @return Number of {@link TestItem}
   */
  long countTestItemByLaunchId(Long launchId);

  /**
   * Select items with provided parent ids
   *
   * @param parentIds Parent test items id
   * @return List of item ids
   */
  @Query(value = "SELECT t.item_id FROM test_item t WHERE t.parent_id IN (:parentIds)", nativeQuery = true)
  List<Long> findIdsByParentIds(@Param("parentIds") Long... parentIds);

  /**
   * Select item paths by provided parent ids
   *
   * @param parentIds Parent test items id
   * @return List of item paths
   */
  @Query(value = "SELECT CAST(t.path AS VARCHAR) FROM test_item t WHERE t.parent_id IN (:parentIds)", nativeQuery = true)
  List<String> findPathsByParentIds(@Param("parentIds") Long... parentIds);

  /**
   * Select items ids with provided retry of
   *
   * @param retryOf Retry of test item id
   * @return List of item ids
   */
  @Query(value = "SELECT t.item_id FROM test_item t WHERE t.retry_of = :retryOf", nativeQuery = true)
  List<Long> findIdsByRetryOf(@Param("retryOf") Long retryOf);

  /**
   * Returns IDs (from itemIds) that have nested steps.
   *
   * @param itemIds test item IDs to check
   * @return list of IDs that have nested steps
   */
  @Query(
      value = "SELECT DISTINCT parent_id FROM test_item WHERE parent_id IN (:itemIds) AND has_stats = false",
      nativeQuery = true
  )
  List<Long> findParentsWithNestedSteps(@Param("itemIds") List<Long> itemIds);

  /**
   * Deletes all test items by launch ID.
   *
   * @param launchId launch ID
   * @return number of deleted test items
   */
  @Modifying
  @Query("DELETE FROM TestItem ti WHERE ti.launchId = :launchId")
  int deleteByLaunchId(@Param("launchId") Long launchId);
}
