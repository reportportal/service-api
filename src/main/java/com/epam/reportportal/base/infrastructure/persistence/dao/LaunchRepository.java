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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

/**
 * {@link Launch} access, retention, streams, and bulk updates.
 *
 * @author Pavel Bortnik
 */
public interface LaunchRepository extends ReportPortalRepository<Launch, Long>,
    LaunchRepositoryCustom {

  /**
   * Updates the launches table setting the retention_policy column according to the provided retention policy. Only
   * updates records where the current retention_policy differs from the provided one.
   *
   * @param policy the retention policy to set
   * @return the number of rows updated
   */
  @Modifying
  @Query(value = """
      UPDATE Launch l
      SET l.retentionPolicy = :policy
      WHERE l.retentionPolicy <> :policy
      """)
  int updateLaunchesRetentionPolicy(RetentionPolicyEnum policy);

  /**
   * Finds launch by {@link Launch#id} and sets a lock on the found launch row in the database. Required for fetching
   * launch from the concurrent environment to provide synchronization between dependant entities
   *
   * @param id {@link Launch#id}
   * @return {@link Optional} with {@link Launch} object
   */
  @Query(value = "SELECT l FROM Launch l WHERE l.id = :id")
  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  Optional<Launch> findByIdForUpdate(@Param("id") Long id);

  void deleteByProjectId(Long projectId);

  List<Launch> findAllByName(String name);

  Optional<Launch> findByUuid(String uuid);

  /**
   * Finds launch by {@link Launch#getUuid()} and sets a lock on the found launch row in the database. Required for
   * fetching launch from the concurrent environment to provide synchronization between dependant entities
   *
   * @param uuid {@link Launch#getUuid()}
   * @return {@link Optional} with {@link Launch} object
   */
  @Query(value = "SELECT l FROM Launch l WHERE l.uuid = :uuid")
  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  Optional<Launch> findByUuidForUpdate(@Param("uuid") String uuid);

  List<Launch> findByProjectIdAndStartTimeGreaterThanAndMode(Long projectId, Instant after,
      LaunchModeEnum mode);

  @Query(value = "SELECT l.id FROM Launch l WHERE l.project_id = :projectId AND l.start_time < :before ORDER BY l.id LIMIT :size", nativeQuery = true)
  List<Long> findIdsByProjectIdAndStartTimeBefore(@Param("projectId") Long projectId,
      @Param("before") Instant before, @Param("size") int limit);

  @Query(value = "SELECT l.id FROM Launch l WHERE l.project_id = :projectId AND l.start_time < :before ORDER BY l.id LIMIT :pageSize OFFSET :pageOffset", nativeQuery = true)
  List<Long> findIdsByProjectIdAndStartTimeBefore(@Param("projectId") Long projectId,
      @Param("before") Instant before, @Param("pageSize") int limit,
      @Param("pageOffset") long offset);

  int deleteAllByIdIn(Collection<Long> ids);

  @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "1"))
  @Query(value = "SELECT l.id FROM Launch l WHERE l.projectId = :projectId AND l.startTime < :before")
  Stream<Long> streamIdsByStartTimeBefore(@Param("projectId") Long projectId,
      @Param("before") Instant before);

  @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "1"))
  @Query(value = "SELECT l.id FROM Launch l WHERE l.status = :status AND l.projectId = :projectId AND l.startTime < :before")
  Stream<Long> streamIdsWithStatusAndStartTimeBefore(@Param("projectId") Long projectId,
      @Param("status") StatusEnum status,
      @Param("before") Instant before);

  @Query(value = "SELECT * FROM launch l WHERE l.id <= :startingLaunchId AND l.name = :launchName "
      + "AND l.project_id = :projectId AND l.mode <> 'DEBUG' ORDER BY start_time DESC, number DESC LIMIT :historyDepth", nativeQuery = true)
  List<Launch> findLaunchesHistory(@Param("historyDepth") int historyDepth,
      @Param("startingLaunchId") Long startingLaunchId,
      @Param("launchName") String launchName, @Param("projectId") Long projectId);

  @Query(value = "SELECT merge_launch(?1)", nativeQuery = true)
  void mergeLaunchTestItems(Long launchId);

  /**
   * Checks if a {@link Launch} has items with retries.
   *
   * @param launchId Current {@link Launch#id}
   * @return True if has
   */
  @Query(value =
      "SELECT exists(SELECT 1 FROM launch JOIN test_item ON launch.id = test_item.launch_id "
          + "WHERE launch.id = :launchId AND test_item.has_retries LIMIT 1)", nativeQuery = true)
  boolean hasRetries(@Param("launchId") Long launchId);

  /**
   * @param launchId {@link Launch#getId()}
   * @param statuses {@link TestItemResults#getStatus()}
   * @return `true` if {@link TestItem#getLaunchId()} equal to provided `launchId`, {@link TestItem#getParentId()} equal
   * to `NULL` and {@link TestItemResults#getStatus()} is not equal to provided `status`, otherwise return `false`
   */
  @Query(value =
      "SELECT exists(SELECT 1 FROM test_item ti JOIN test_item_results tir ON ti.item_id = tir.result_id "
          + " WHERE ti.launch_id = :launchId AND ti.parent_id IS NULL AND ti.has_stats = TRUE "
          + " AND CAST(tir.status AS VARCHAR) NOT IN (:statuses))", nativeQuery = true)
  boolean hasRootItemsWithStatusNotEqual(@Param("launchId") Long launchId,
      @Param("statuses") String... statuses);

  @Query(value =
      "SELECT exists(SELECT 1 FROM test_item ti JOIN test_item_results tir ON ti.item_id = tir.result_id "
          + " WHERE ti.launch_id = :launchId AND ti.has_stats = TRUE AND tir.status = cast(:#{#status.name()} AS STATUS_ENUM) LIMIT 1)", nativeQuery = true)
  boolean hasItemsWithStatusEqual(@Param("launchId") Long launchId,
      @Param("status") StatusEnum status);

  @Query(value = "SELECT exists(SELECT 1 FROM test_item ti WHERE ti.launch_id = :launchId LIMIT 1)", nativeQuery = true)
  boolean hasItems(@Param("launchId") Long launchId);

  /**
   * Finds the latest(that has max {@link Launch#number} {@link Launch} with specified {@code name} and
   * {@code projectId}
   *
   * @param name      Name of {@link Launch}
   * @param projectId Id of {@link Project}
   * @return {@link Optional} if exists, {@link Optional#empty()} if not
   */
  @Query(value = "SELECT * FROM launch l WHERE l.name =:name AND l.project_id=:projectId ORDER BY l.number DESC LIMIT 1", nativeQuery = true)
  Optional<Launch> findLatestByNameAndProjectId(@Param("name") String name,
      @Param("projectId") Long projectId);

  Optional<Launch> findLaunchByProjectIdAndNameAndNumberAndIdNotAndModeNot(Long projectId,
      String name, Long number, Long launchId, LaunchModeEnum mode);

  Optional<Launch> findByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);

  @Query(value = "SELECT l FROM Launch l WHERE l.id = :id AND l.projectId = :projectId AND l.launchType = 'MANUAL'")
  Optional<Launch> findManualLaunchByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);

  /**
   * Checks if launch exists by ID and project ID.
   *
   * @param launchId  launch ID
   * @param projectId project ID
   * @return true if launch exists in a project, false otherwise
   */
  boolean existsByIdAndProjectId(Long launchId, Long projectId);

  /**
   * Checks if launch by type exists by ID and project ID.
   *
   * @param launchId  launch ID
   * @param projectId project ID
   * @return true if launch by type exists in a project, false otherwise
   */
  boolean existsByIdAndProjectIdAndLaunchType(Long launchId, Long projectId, LaunchTypeEnum launchType);

  /**
   * Finds test plan ID by launch ID.
   *
   * @param launchId the launch ID
   * @return optional test plan ID
   */
  @Query("SELECT l.testPlanId FROM Launch l WHERE l.id = :launchId")
  Optional<Long> findTestPlanIdById(@Param("launchId") Long launchId);
}
