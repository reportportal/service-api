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

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repository for integration plugin instances.
 *
 * @author Ivan Budayeu
 * @author Andrei Varabyeu
 */
public interface IntegrationRepository extends ReportPortalRepository<Integration, Long>,
    IntegrationRepositoryCustom {

  /**
   * Check whether a global integration (no project, no organization) with the given name and type already exists.
   *
   * @param name   {@code Integration#getName()}
   * @param typeId {@code IntegrationType#getId()}
   * @return {@code true} if a matching global integration exists
   */
  boolean existsByNameIgnoreCaseAndTypeIdAndProjectIdIsNullAndOrganizationIdIsNull(String name, Long typeId);

  boolean existsByNameIgnoreCaseAndTypeIdAndProjectId(String name, Long typeId, Long projectId);

  boolean existsByTypeIdAndProjectIdIsNullAndOrganizationIdIsNull(Long typeId);

  boolean existsByTypeIdAndOrganizationId(Long typeId, Long organizationId);

  boolean existsByTypeIdAndProjectId(Long typeId, Long projectId);

  /**
   * Retrieve integration by ID and project ID
   *
   * @param id        ID of integrations
   * @param projectId ID of project
   * @return Optional of integration
   */
  Optional<Integration> findByIdAndProjectId(Long id, Long projectId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT i FROM Integration i WHERE i.id = :id AND i.project.id = :projectId")
  Optional<Integration> findByIdAndProjectIdForUpdate(@Param("id") Long id, @Param("projectId") Long projectId);

  /**
   * @param name              {@code Integration#getName()}
   * @param integrationTypeId {@code Integration#getType()}#{@code IntegrationType#getId()}
   * @return {@link Optional} with {@link Integration}
   */
  Optional<Integration> findByNameAndTypeIdAndProjectIdIsNullAndOrganizationIdIsNull(String name,
      Long integrationTypeId);

  /**
   * @param id                {@code Integration#getId()}
   * @param integrationTypeId {@code Integration#getType()}#{@code IntegrationType#getId()}
   * @return {@link Optional} with {@link Integration}
   */
  Optional<Integration> findByIdAndTypeIdAndProjectIdIsNullAndOrganizationIdIsNull(Long id, Long integrationTypeId);

  /**
   * Retrieve given project's integrations
   *
   * @param projectId ID of project
   * @return Found integrations
   */
  List<Integration> findAllByProjectIdOrderByCreationDateDesc(Long projectId);

  /**
   * Retrieve all {@link Integration} by project ID and integration type
   *
   * @param projectId       {@code Project#getId()}
   * @param integrationType {@link IntegrationType}
   * @return The {@link List} of the {@link Integration}
   */
  List<Integration> findAllByProjectIdAndTypeOrderByCreationDateDesc(Long projectId,
      IntegrationType integrationType);

  /**
   * Delete all global {@link Integration} (no project, no organization) by integration type ID
   *
   * @param typeId {@code IntegrationType#getId()}
   */
  @Modifying
  @Query(value = "DELETE FROM integration WHERE project_id IS NULL AND organization_id IS NULL AND type = :typeId",
      nativeQuery = true)
  void deleteAllGlobalByIntegrationTypeId(@Param("typeId") Long typeId);

  /**
   * Delete all {@link Integration} by projectID and integration type ID
   *
   * @param typeId {@code IntegrationType#getId()}
   */
  @Modifying
  @Query(value = "DELETE FROM integration WHERE project_id = :projectId AND type = :typeId", nativeQuery = true)
  void deleteAllByProjectIdAndIntegrationTypeId(@Param("projectId") Long projectId,
      @Param("typeId") Long typeId);

  /**
   * Retrieve all {@link Integration} with {@code Integration#getProject()} == null by integration type
   *
   * @param integrationType {@code Integration#getType()}
   * @return @return The {@link List} of the {@link Integration}
   */
  @Query(value = "SELECT i FROM Integration i WHERE i.project IS NULL AND i.organizationId IS NULL AND i.type = :integrationType order by i.creationDate desc")
  List<Integration> findAllGlobalByType(@Param("integrationType") IntegrationType integrationType);

  /**
   * Retrieve all {@link Integration} with {@code Integration#getProject()} by integration group
   *
   * @param integrationGroup {@code IntegrationType#getIntegrationGroup()}
   * @return @return The {@link List} of the {@link Integration}
   */
  @Query(value = "SELECT i FROM Integration i JOIN i.type t WHERE i.project = :project AND t.integrationGroup = :integrationGroup order by i.creationDate desc")
  List<Integration> findAllProjectByGroup(@Param("project") Project project,
      @Param("integrationGroup") IntegrationGroupEnum integrationGroup);

  /**
   * Retrieve all {@link Integration} with {@code Integration#getProject()} == null by integration group
   *
   * @param integrationGroup {@code IntegrationType#getIntegrationGroup()}
   * @return @return The {@link List} of the {@link Integration}
   */
  @Query(value = "SELECT i FROM Integration i JOIN i.type t WHERE i.project IS NULL AND i.organizationId IS NULL AND t.integrationGroup = :integrationGroup order by i.creationDate desc")
  List<Integration> findAllGlobalByGroup(
      @Param("integrationGroup") IntegrationGroupEnum integrationGroup);

  /**
   * Retrieve all {@link Integration} with {@code Integration#getProject()} == null
   *
   * @return @return The {@link List} of the global {@link Integration}
   */
  @Query(value = "SELECT i FROM Integration i WHERE i.project IS NULL AND i.organizationId IS NULL order by i.creationDate desc")
  List<Integration> findAllGlobal();

  /**
   * Find BTS integration by BTS url, BTS project name and Report Portal project id
   *
   * @param url        Bug Tracking System url
   * @param btsProject Bug Tracking System project name
   * @param projectId  {@code Project#getId()}
   * @return The {@link Integration} wrapped in the {@link Optional}
   */
  @Query(value =
      "SELECT i.id, i.name, i.enabled,  i.organization_id, i.project_id, i.creator, i.creation_date, i.params, i.type, 0 AS clazz_ FROM integration i"
          + " WHERE (params->'params'->>'url' = :url AND params->'params'->>'project' = :btsProject"
          + " AND i.project_id = :projectId) LIMIT 1", nativeQuery = true)
  Optional<Integration> findProjectBtsByUrlAndLinkedProject(@Param("url") String url,
      @Param("btsProject") String btsProject,
      @Param("projectId") Long projectId);

  /**
   * Find BTS integration by BTS url, BTS project name and {@code Integration#getProject()} == null
   *
   * @param url        Bug Tracking System url
   * @param btsProject Bug Tracking System project name
   * @return The {@link Integration} wrapped in the {@link Optional}
   */
  @Query(value =
      "SELECT i.id, i.name, i.enabled, i.project_id, i.organization_id, i.creator, i.creation_date, i.params, i.type, 0 AS clazz_ FROM integration i "
          + " WHERE params->'params'->>'url' = :url AND i.params->'params'->>'project' = :btsProject AND i.project_id IS NULL AND i.organization_id IS NULL LIMIT 1", nativeQuery = true)
  Optional<Integration> findGlobalBtsByUrlAndLinkedProject(@Param("url") String url,
      @Param("btsProject") String btsProject);

  /**
   * Update {@code Integration#isEnabled()} by integration ID
   *
   * @param enabled       Enabled state flag
   * @param integrationId {@code Integration#getId()}
   */
  @Modifying
  @Query(value = "UPDATE integration SET enabled = :enabled WHERE id = :integrationId", nativeQuery = true)
  void updateEnabledStateById(@Param("enabled") boolean enabled,
      @Param("integrationId") Long integrationId);

  /**
   * Update {@code Integration#isEnabled()} of all integrations by integration type id
   *
   * @param enabled           Enabled state flag
   * @param integrationTypeId {@code IntegrationType#getId()}
   */
  @Modifying
  @Query(value = "UPDATE integration SET enabled = :enabled WHERE type = :integrationTypeId", nativeQuery = true)
  void updateEnabledStateByIntegrationTypeId(@Param("enabled") boolean enabled,
      @Param("integrationTypeId") Long integrationTypeId);

  @Query(value = "SELECT i.* FROM integration i LEFT OUTER JOIN integration_type it ON i.type = it.id WHERE it.name IN (:types) order by i.creation_date desc", nativeQuery = true)
  List<Integration> findAllByTypeIn(@Param("types") String... types);

  @Query("""
      SELECT i
      FROM Integration i
      JOIN i.type t
      WHERE i.name = :name
        AND t.integrationGroup = :group
        AND t.authFlow = :authFlow
        AND i.project IS NULL
        AND i.organizationId IS NULL
      """)
  Optional<Integration> findGlobalByNameAndAuthFlowAndGroup(
      @Param("name") String name,
      @Param("group") IntegrationGroupEnum group,
      @Param("authFlow") IntegrationAuthFlowEnum authFlow);

  @Query("""
      SELECT i
      FROM Integration i
      JOIN i.type t
      WHERE t.integrationGroup = :group
        AND t.authFlow = :authFlow
        AND i.project IS NULL
        AND i.organizationId IS NULL
      """)
  List<Integration> findAllByAuthFlowAndGroup(
      @Param("group") IntegrationGroupEnum group,
      @Param("authFlow") IntegrationAuthFlowEnum authFlow);

  @Query("""
      SELECT CASE WHEN COUNT(i) > 0 THEN TRUE
            ELSE FALSE END
      FROM Integration i
      WHERE LOWER(i.name) = LOWER(:name)
        AND i.type.id = :typeId
        AND i.organizationId = :organizationId
      """)
  boolean existsByNameIgnoreCaseAndTypeIdAndOrganizationId(@Param("name") String name,
      @Param("typeId") Long typeId, @Param("organizationId") Long organizationId);

  /**
   * Retrieve integrations for the given organization with pagination and sorting.
   *
   * @param orgId    Organization ID
   * @param pageable Pagination and sort parameters
   * @return Page of integrations
   */
  @Query("SELECT i FROM Integration i WHERE i.organizationId = :orgId")
  Page<Integration> findAllByOrganizationId(@Param("orgId") Long orgId, Pageable pageable);

  /**
   * Retrieve an integration by ID scoped to the given organization.
   *
   * @param id    Integration ID
   * @param orgId Organization ID
   * @return Optional integration
   */
  @Query("SELECT i FROM Integration i WHERE i.id = :id AND i.organizationId = :orgId")
  Optional<Integration> findByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);

  /**
   * Finds all integrations belonging to the specified organization and matching the given integration type.
   *
   * @param orgId  the ID of the organization
   * @param typeId the ID of the integration type
   * @return a list of {@link Integration} entities matching both the organization and type; empty if none found
   */
  @Query("SELECT i FROM Integration i WHERE i.organizationId = :orgId AND i.type.id = :typeId")
  List<Integration> findAllByOrganizationIdAndTypeId(@Param("orgId") Long orgId, @Param("typeId") Long typeId);

  /**
   * Returns the newest enabled project-scoped integration whose type is in {@code typeIds}.
   *
   * @param projectId project identifier
   * @param typeIds   integration type identifiers belonging to the requested group
   * @return the integration, or empty if none match
   */
  @Query("""
      SELECT i FROM Integration i
      WHERE i.project.id = :projectId
        AND i.type.id IN :typeIds
        AND i.enabled = true
        AND i.type.enabled = true
      ORDER BY i.creationDate DESC
      LIMIT 1
      """)
  Optional<Integration> findFirstEnabledByProjectIdAndTypeIdIn(
      @Param("projectId") Long projectId, @Param("typeIds") List<Long> typeIds);

  /**
   * Returns the newest enabled organization-scoped integration whose type is in {@code typeIds}.
   *
   * @param orgId   organization identifier
   * @param typeIds integration type identifiers belonging to the requested group
   * @return the integration, or empty if none match
   */
  @Query("""
      SELECT i FROM Integration i
      WHERE i.organizationId = :orgId
        AND i.type.id IN :typeIds
        AND i.enabled = true
        AND i.type.enabled = true
      ORDER BY i.creationDate DESC
      LIMIT 1
      """)
  Optional<Integration> findFirstEnabledByOrganizationIdAndTypeIdIn(
      @Param("orgId") Long orgId, @Param("typeIds") List<Long> typeIds);

  /**
   * Returns the newest enabled global integration whose type is in {@code typeIds}.
   *
   * @param typeIds integration type identifiers belonging to the requested group
   * @return the integration, or empty if none match
   */
  @Query("""
      SELECT i FROM Integration i
      WHERE i.project IS NULL
        AND i.organizationId IS NULL
        AND i.type.id IN :typeIds
        AND i.enabled = true
        AND i.type.enabled = true
      ORDER BY i.creationDate DESC
      LIMIT 1
      """)
  Optional<Integration> findFirstEnabledGlobalByTypeIdIn(@Param("typeIds") List<Long> typeIds);

}
