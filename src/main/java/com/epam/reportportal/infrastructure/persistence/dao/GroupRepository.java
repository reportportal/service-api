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

package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.group.Group;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link Group}.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 * @see Group
 */
public interface GroupRepository extends ReportPortalRepository<Group, Long> {

  /**
   * Retrieves a group by its slug.
   *
   * @param slug group slug
   * @return {@link Optional} of {@link Group}
   */
  Optional<Group> findBySlug(String slug);

  /**
   * Retrieves a group by its UUID.
   *
   * @param uuid group UUID
   * @return {@link Optional} of {@link Group}
   */
  Optional<Group> findByUuid(UUID uuid);

  /**
   * Retrieves all groups with their users and projects with pagination.
   *
   * @param pageable {@link Pageable} object
   * @return {@link Page} of {@link Group}
   */
  @EntityGraph(attributePaths = {"users", "projects"})
  @Query("SELECT g FROM Group g")
  Page<Group> findAllWithUsersAndProjects(Pageable pageable);

  /**
   * Retrieves all groups with their users and projects with pagination.
   *
   * @param pageable {@link Pageable} object
   * @param orgId    Organization identifier
   * @return {@link Page} of {@link Group}
   */
  @EntityGraph(attributePaths = {"users", "projects"})
  @Query("SELECT g FROM Group g WHERE g.organizationId = :orgId")
  Page<Group> findAllWithUsersAndProjects(@Param("orgId") Long orgId, Pageable pageable);

  /**
   * Retrieves a group by its ID with users and projects.
   *
   * @param id {@link Long} group ID
   * @return {@link List} of {@link Group}
   */
  @EntityGraph(attributePaths = {"users", "projects"})
  @Query("SELECT g FROM Group g WHERE g.id = :id")
  Optional<Group> findByIdWithUsersAndProjects(Long id);
}
