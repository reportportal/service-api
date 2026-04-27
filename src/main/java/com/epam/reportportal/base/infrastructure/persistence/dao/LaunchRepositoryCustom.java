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

import com.epam.reportportal.base.infrastructure.model.analyzer.IndexLaunch;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JLaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * jOOQ-based launch search, name lookup, and analyzer index helpers.
 *
 * @author Pavel Bortnik
 */
public interface LaunchRepositoryCustom extends FilterableRepository<Launch> {

  boolean hasItemsInStatuses(Long launchId, List<JStatusEnum> statuses);

  /**
   * Retrieves names of the launches by provided 'projectId', 'mode', 'value' as a part of the name and statuses that
   * are not equal to the provided 'status'
   *
   * @param projectId {@link Launch#projectId}
   * @param value     A part of the {@link Launch#name}
   * @param mode      {@link Launch#mode}
   * @param status    {@link Launch#status}
   * @return The {@link List} of the {@link Launch#name}
   */
  List<String> getLaunchNamesByModeExcludedByStatus(Long projectId, String value,
      LaunchModeEnum mode, StatusEnum status);

  List<String> getOwnerNames(Long projectId, String value, String mode);

  Map<String, String> getStatuses(Long projectId, Long[] ids);

  Optional<Launch> findLatestByFilter(Filter filter);

  Page<Launch> findAllLatestByFilter(Queryable filter, Pageable pageable);

  /**
   * Finds launch ids of project with provided id
   *
   * @param projectId - Project id
   * @return List of ids
   */
  List<Long> findLaunchIdsByProjectId(Long projectId);

  /**
   * Finds the last valid launch in project
   *
   * @param projectId Project id
   * @param mode      Launch mode
   * @return {@link Optional} of {@link Launch}
   */
  Optional<Launch> findLastRun(Long projectId, String mode);

  /**
   * Counts launches with mode for specified project from provided date
   *
   * @param projectId Project id
   * @param mode      Launch mode
   * @param fromDate  From Date to count
   * @return Launches count
   */
  Integer countLaunches(Long projectId, String mode, Instant fromDate);

  /**
   * Counts launches with mode for specified project
   *
   * @param projectId {@link Launch#projectId}
   * @param mode      {@link Launch#mode}
   * @return Launches count
   */
  Integer countLaunches(Long projectId, String mode);

  /**
   * Counts quantity of launches with mode per user for specified project.
   *
   * @param projectId Project id
   * @param mode      Launch mode
   * @param from      From Date to count
   * @return Map of username and launches count
   */
  Map<String, Integer> countLaunchesGroupedByOwner(Long projectId, String mode, Instant from);

  List<Long> findIdsByProjectIdAndModeAndStatusNotEq(Long projectId, JLaunchModeEnum mode,
      JStatusEnum status, int limit);

  List<Long> findIdsByProjectIdAndModeAndStatusNotEqAfterId(Long projectId, JLaunchModeEnum mode,
      JStatusEnum status, Long launchId,
      int limit);

  boolean hasItemsWithLogsWithLogLevel(Long launchId, Collection<JTestItemTypeEnum> itemTypes,
      Integer logLevel);

  List<IndexLaunch> findIndexLaunchByIds(List<Long> ids);

  Optional<Launch> findPreviousLaunchByProjectIdAndNameAndAttributesForLaunchIdAndModeNot(
      Long projectId, String name, String[] attributes, Long launchId, JLaunchModeEnum mode
  );

  Optional<Long> findPreviousLaunchId(Launch launch);
}
