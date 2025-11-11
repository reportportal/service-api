/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.log.ProjectLogType;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogTypeRepository extends ReportPortalRepository<ProjectLogType, Long> {

  @Cacheable(
      value = "projectLogTypeCache",
      key = "#projectId",
      cacheManager = "caffeineCacheManager"
  )
  List<ProjectLogType> findByProjectId(Long projectId);

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM log_type lt
          WHERE lt.project_id = :projectId AND (LOWER(lt.name) = LOWER(:name) OR lt.level = :level)
      )
      """, nativeQuery = true)
  boolean existsByProjectIdAndNameOrLevelIgnoreCase(@Param("projectId") Long projectId,
      @Param("name") String name, @Param("level") Integer level);

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM log_type lt
          WHERE lt.project_id = :projectId
            AND lt.id != :excludeId
            AND (LOWER(lt.name) = LOWER(:name) OR lt.level = :level)
      )
      """, nativeQuery = true)
  boolean existsByProjectIdAndNameOrLevelIgnoreCaseExcludingId(@Param("projectId") Long projectId,
      @Param("name") String name, @Param("level") Integer level, @Param("excludeId") Long excludeId);

  @Query("""
      SELECT COUNT(log.id)
      FROM ProjectLogType log
      WHERE log.projectId = :projectId AND log.filterable = true
      """)
  long countFilterableLogTypes(@Param("projectId") Long projectId);

  @Cacheable(
      value = "projectLogTypeWithLevelNameCache",
      key = "#projectId + '_' + #name",
      cacheManager = "caffeineCacheManager"
  )
  @Query("""
      SELECT lt.level
      FROM ProjectLogType lt
      WHERE lt.projectId = :projectId AND LOWER(lt.name) = LOWER(:name)
      """)
  Optional<Integer> findLevelByProjectIdAndNameIgnoreCase(@Param("projectId") Long projectId,
      @Param("name") String name);

  @Cacheable(
      value = "projectLogTypeWithLevelCache",
      key = "#projectId + '_' + #level",
      cacheManager = "caffeineCacheManager"
  )
  @Query("""
      SELECT lt.name
      FROM ProjectLogType lt
      WHERE lt.projectId = :projectId AND lt.level = :level
      """)
  String findNameByProjectIdAndLevel(@Param("projectId") Long projectId,
      @Param("level") Integer level);
}
