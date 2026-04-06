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

import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing ProjectUser entities.
 *
 * @author Pavel Bortnik
 */
public interface ProjectUserRepository
    extends ReportPortalRepository<ProjectUser, ProjectUserId>, ProjectUserRepositoryCustom {

  /**
   * Finds project IDs associated with the specified user ID.
   *
   * @param userId The user ID
   * @return List of project IDs
   */
  @Query(value = "SELECT pu.project_id FROM project_user pu WHERE pu.user_id = :userId", nativeQuery = true)
  List<Long> findProjectIdsByUserId(@Param("userId") Long userId);

  /**
   * Finds a ProjectUser by user ID and project ID.
   *
   * @param userId    The user ID
   * @param projectId The project ID
   * @return An Optional containing the ProjectUser if found, or empty if not found
   */
  Optional<ProjectUser> findProjectUserByUserIdAndProjectId(Long userId, Long projectId);


  /**
   * Deletes project user records for all projects belonging to the specified organization and returns the deleted
   * project IDs.
   *
   * @param orgId  The organization ID
   * @param userId The user ID
   * @return List of project IDs that were removed
   */
  @Modifying
  @Query(value =
      """
            DELETE FROM project_user
            WHERE user_id = :userId AND project_id IN (SELECT id FROM project WHERE organization_id = :orgId)
            RETURNING project_id
          """, nativeQuery = true)
  List<Long> deleteProjectUserByProjectOrganizationId(@Param("orgId") Long orgId, @Param("userId") Long userId);

  /**
   * Deletes project user records for all projects ids from the list.
   *
   * @param userId     The user ID
   * @param projectIds List of Project ids
   */
  @Modifying
  @Query(value =
      """
            DELETE FROM project_user WHERE user_id = :userId AND project_id IN :projectIds
          """, nativeQuery = true)
  void deleteByUserIdAndProjectIds(@Param("userId") Long userId, @Param("projectIds") List<Long> projectIds);

  /**
   * Deletes all entries from the project_user table for the specified project ID with user IDs in the provided list.
   *
   * @param projectId The ID of the project whose user associations should be deleted.
   * @param userIds   The list of user IDs to delete.
   */
  void deleteByProject_IdAndUser_IdIn(Long projectId, List<Long> userIds);


  /**
   * Deletes all entries from the project_user table for the specified project ID.
   *
   * @param projectId The ID of the project whose user associations should be deleted.
   */
  @Modifying
  @Query(value = "DELETE FROM project_user WHERE project_id = :projectId", nativeQuery = true)
  void deleteAllByProjectId(@Param(value = "projectId") Long projectId);

  /**
   * Finds all ProjectUser entries for the specified project ID.
   *
   * @param projectId The project ID
   * @return List of ProjectUser entries
   */
  List<ProjectUser> findAllByProject_Id(Long projectId);

  /**
   * Finds all ProjectUser entries for the specified project ID where the user ID is not in the provided list.
   *
   * @param projectId The project ID
   * @param userIds   The list of user IDs to exclude
   * @return List of ProjectUser entries
   */
  List<ProjectUser> findAllByProject_IdAndUser_IdNotIn(Long projectId, List<Long> userIds);
}
