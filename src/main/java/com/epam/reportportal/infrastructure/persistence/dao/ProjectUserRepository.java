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

import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Pavel Bortnik
 */
public interface ProjectUserRepository extends ReportPortalRepository<ProjectUser, ProjectUserId>,
    ProjectUserRepositoryCustom {

  @Query(value = "SELECT pu.project_id FROM project_user pu WHERE pu.user_id = :userId", nativeQuery = true)
  List<Long> findProjectIdsByUserId(@Param("userId") Long userId);

  Optional<ProjectUser> findProjectUserByUserIdAndProjectId(Long userId, Long projectId);


  /**
   * Deletes project user records for all projects belonging to the specified organization.
   *
   * @param orgId  The organization ID
   * @param userId The user ID
   */
  @Modifying
  @Query(value = """
      DELETE FROM project_user pu WHERE pu.user_id = :userId AND pu.project_id IN (SELECT id FROM project WHERE organization_id = :orgId)
      """,
      nativeQuery = true)
  void deleteProjectUserByProjectOrganizationId(@Param("orgId") Long orgId, @Param("userId") Long userId);

  /**
   * Deletes project user records for all projects ids from the list.
   *
   * @param userId     The user ID
   * @param projectIds List of Project ids
   */
  @Modifying
  @Query(value = """
      DELETE FROM project_user WHERE user_id = :userId AND project_id IN :projectIds
      """,
      nativeQuery = true)
  void deleteByUserIdAndProjectIds(@Param("userId") Long userId, @Param("projectIds") List<Long> projectIds);

  @Modifying
  @Query(value = "DELETE FROM project_user WHERE project_id = :projectId AND user_id NOT IN :userIds", nativeQuery = true)
  void deleteByProjectIdAndUserIdNotIn(@Param("projectId") Long projectId, @Param("userIds") List<Long> userIds);


  /**
   * Deletes all entries from the project_user table for the specified project ID.
   *
   * @param projectId The ID of the project whose user associations should be deleted.
   */
  @Modifying
  @Query(value = "DELETE FROM project_user WHERE project_id = :projectId", nativeQuery = true)
  void deleteAllByProjectId(@Param(value = "projectId") Long projectId);
}
