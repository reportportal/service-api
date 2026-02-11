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

import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends ReportPortalRepository<Project, Long>,
    ProjectRepositoryCustom {

  Optional<Project> findByName(String name);

  Optional<Project> findByKey(String key);

  boolean existsByName(String name);

  Optional<Project> findByNameAndOrganizationId(String name, Long organizationId);

  Optional<Project> findBySlugAndOrganizationId(String slug, Long organizationId);

  boolean existsBySlugAndOrganizationId(String slug, Long organizationId);

  Optional<Project> findByIdAndOrganizationId(Long projectId, Long organizationId);

  List<Project> findAllByOrganizationId(Long organizationId);

  boolean existsByIdAndOrganizationId(Long projectId, Long organizationId);

  @Query(value = "SELECT p.* FROM project p JOIN project_user pu on p.id = pu.project_id JOIN users u on pu.user_id = u.id WHERE u.login = :login", nativeQuery = true)
  List<Project> findUserProjects(@Param("login") String login);

  @Query(value = "SELECT p.* FROM project p JOIN project_user pu on p.id = pu.project_id JOIN users u on pu.user_id = u.id WHERE u.login = :login AND p.project_type = :projectType", nativeQuery = true)
  List<Project> findUserProjects(@Param("login") String login,
      @Param("projectType") String projectType);

  @Modifying
  @Query(value = "UPDATE project SET name = :projectName WHERE id = :projectId", nativeQuery = true)
  void updateProjectName(@Param("projectName") String projectName, @Param("projectId") Long projectId);

  @Modifying
  @Query(value = "UPDATE project SET slug = :projectSlug WHERE id = :projectId", nativeQuery = true)
  void updateProjectSlug(@Param("projectSlug") String projectSlug, @Param("projectId") Long projectId);

  @Modifying
  @Query(value = """
      UPDATE project_attribute
      SET value = :newValue
      WHERE attribute_id = (
          SELECT id FROM attribute WHERE name = :attributeName
      )
        AND project_id IN (
          SELECT id FROM project WHERE organization_id = :organizationId
      )
        AND CAST(value AS bigint) > :newValue
      """, nativeQuery = true)
  int updateProjectAttributeValueIfGreater(
      @Param("newValue") Long newValue,
      @Param("attributeName") String attributeName,
      @Param("organizationId") Long organizationId
  );
}
