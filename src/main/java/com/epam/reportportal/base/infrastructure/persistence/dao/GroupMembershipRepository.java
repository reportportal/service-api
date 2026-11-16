/*
 * Copyright 2025 EPAM Systems
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

import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupProject;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupProjectId;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.dto.GroupMembershipDetailsDto;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link GroupProject} and related entities.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 * @see GroupProject
 * @see MembershipDetails
 * @see ProjectRole
 */
public interface GroupMembershipRepository extends
    ReportPortalRepository<GroupProject, GroupProjectId> {

  /**
   * Finds all roles of the user in the project via group membership.
   *
   * @param userId    user id
   * @param projectId project id
   * @return {@link List} of {@link ProjectRole}
   */
  @Query(value = """
      SELECT DISTINCT gp.project_role
      FROM groups_projects gp
      JOIN groups_users gu
      ON gp.group_id = gu.group_id
        AND gu.user_id = :userId
      WHERE gp.project_id = :projectId
      """,
      nativeQuery = true
  )
  @Cacheable(
      value = "groupUserProjectRolesCache",
      key = "#userId + '_' + #projectId",
      cacheManager = "caffeineCacheManager"
  )
  List<ProjectRole> findUserProjectRoles(
      @Param("userId") Long userId,
      @Param("projectId") Long projectId
  );

  /**
   * Finds a raw project details of the user in the project via group membership.
   *
   * @param userId     user id
   * @param projectKey project key
   * @return {@link Optional} of {@link GroupMembershipDetailsDto}
   */
  @Query(value = """
      SELECT p.id AS projectId,
        p.key AS projectKey,
        array_agg(gp.project_role) AS projectRoles
      FROM groups_projects gp
      JOIN groups_users gu
        ON gp.group_id = gu.group_id
          AND gu.user_id = :userId
      JOIN Project p
        ON gp.project_id = p.id
          AND p.name = :projectKey group by p.id
      """,
      nativeQuery = true
  )
  Optional<GroupMembershipDetailsDto> findMembershipRaw(
      @Param("userId") Long userId,
      @Param("projectKey") String projectKey
  );

  /**
   * Finds a raw project details of the user in the project via group membership.
   *
   * @param userId    user id
   * @param projectId project id
   * @return {@link Optional} of {@link GroupMembershipDetailsDto}
   */
  @Query(value = """
      SELECT gp.project_id AS projectId,
        null as projectKey,
        array_agg(gp.project_role) AS projectRoles
      FROM groups_projects gp
      JOIN groups_users gu
        ON gp.group_id = gu.group_id
          AND gu.user_id = :userId
      WHERE gp.project_id = :projectId
      group by gp.project_id
      """,
      nativeQuery = true
  )
  Optional<GroupMembershipDetailsDto> findMembershipRaw(
      @Param("userId") Long userId,
      @Param("projectId") Long projectId
  );

  /**
   * Finds project details of the user in the project via group membership.
   *
   * @param userId     user id
   * @param projectKey project key
   * @return {@link Optional} of {@link MembershipDetails}
   */
  @Cacheable(
      value = "groupProjectDetailsCache",
      key = "#userId + '_' + #projectKey",
      cacheManager = "caffeineCacheManager"
  )
  default Optional<MembershipDetails> findMembershipDetails(
      Long userId,
      String projectKey
  ) {
    return findMembershipRaw(userId, projectKey)
        .map(record -> MembershipDetails.builder()
            .withProjectId(record.projectId())
            .withProjectKey(record.projectKey())
            .withProjectHighestRole(record.projectRoles())
            .build()
        );
  }

  /**
   * Finds project details of the user in the project via group membership.
   *
   * @param userId    user id
   * @param projectId project id
   * @return {@link Optional} of {@link MembershipDetails}
   */
  @Cacheable(
      value = "groupProjectDetailsCache",
      key = "#userId + '_' + #projectId",
      cacheManager = "caffeineCacheManager"
  )
  default Optional<MembershipDetails> findMembershipDetails(
      Long userId,
      Long projectId
  ) {
    return findMembershipRaw(userId, projectId)
        .map(record -> MembershipDetails.builder()
            .withProjectId(record.projectId())
            .withProjectKey(Optional.ofNullable(record.projectKey()).orElse(""))
            .withProjectHighestRole(record.projectRoles())
            .build()
        );
  }

  /**
   * Finds all projects of the user via group membership.
   *
   * @param userId user id
   * @return {@link List} of {@link GroupProject}
   */
  @Query(value = """
      SELECT gp.project_id, gp.group_id, gp.project_role, gp.created_at, gp.updated_at
      FROM groups_projects gp
      JOIN groups_users gu
        ON gp.group_id = gu.group_id
      WHERE gu.user_id = :user_id
      ORDER BY gp.project_id
      """,
      nativeQuery = true
  )
  List<GroupProject> findAllUserProjects(@Param("user_id") Long userId);

  /**
   * Finds all projects of the user via group membership in the organization.
   *
   * @param userId user id
   * @param orgId  organization id
   * @return {@link List} of {@link GroupProject}
   */
  @Query(value = """
      SELECT gp.project_id, gp.group_id, gp.project_role, gp.created_at, gp.updated_at
      FROM groups_projects gp
      JOIN groups_users gu
        ON gp.group_id = gu.group_id
      JOIN groups g
        ON g.id = gp.group_id
      WHERE gu.user_id = :userId
        AND g.org_id = :orgId
      ORDER BY gp.project_id
      """,
      nativeQuery = true
  )
  List<GroupProject> findAllUserProjectsInOrganization(
      @Param("userId") Long userId,
      @Param("orgId") Long orgId
  );
}
