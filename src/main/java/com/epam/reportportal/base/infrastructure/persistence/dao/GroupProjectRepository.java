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
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

/**
 * Repository for {@link GroupProject} and related entities.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 * @see GroupProject
 * @see ProjectRole
 */
public interface GroupProjectRepository extends
    ReportPortalRepository<GroupProject, GroupProjectId> {

  /**
   * Finds all projects of the Group.
   *
   * @param groupId group id
   * @return {@link List} of {@link GroupProject}
   */
  List<GroupProject> findAllByGroupId(Long groupId);

  /**
   * Finds all projects of the Group with pagination.
   *
   * @param groupId  group id
   * @param pageable {@link Pageable}
   * @return {@link Page} of {@link GroupProject}
   */
  Page<GroupProject> findAllByGroupId(Long groupId, Pageable pageable);


  /**
   * Finds all groups of the Project.
   *
   * @param projectId project id
   * @return {@link List} of {@link GroupProject}
   */
  List<GroupProject> findAllByProjectId(Long projectId);

  /**
   * Finds all groups of the Project with pagination.
   *
   * @param projectId project id
   * @return {@link List} of {@link GroupProject}
   */
  Page<GroupProject> findAllByProjectId(Long projectId, Pageable pageable);

  /**
   * Finds all groups of the Project by project name.
   *
   * @param projectName project name
   * @return {@link List} of {@link GroupProject}
   */
  @EntityGraph(attributePaths = {"group", "group.users"})
  List<GroupProject> findAllByProjectName(String projectName);

  /**
   * Finds all groups of the Project by project name with pagination.
   *
   * @param projectName project name
   * @return {@link List} of {@link GroupProject}
   */
  @EntityGraph(attributePaths = {"group", "group.users"})
  Page<GroupProject> findAllByProjectName(String projectName, Pageable pageable);


  /**
   * Finds group of the Project by project name and group id.
   *
   * @param groupId     group id
   * @param projectName project name
   * @return {@link Optional} of {@link GroupProject}
   */
  @EntityGraph(attributePaths = {"group", "group.users"})
  Optional<GroupProject> findByGroupIdAndProjectName(Long groupId, String projectName);
}
