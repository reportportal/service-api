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

import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repository for project dashboards.
 *
 * @author Pavel Bortnik
 */
public interface DashboardRepository extends ReportPortalRepository<Dashboard, Long>,
    DashboardRepositoryCustom {

  /**
   * Finds dashboard by 'id' and 'project id'
   *
   * @param id        {@link Dashboard#getId()}
   * @param projectId Id of the {@link Project} whose dashboard will be extracted
   * @return {@link Dashboard} wrapped in the {@link Optional}
   */
  Optional<Dashboard> findByIdAndProjectId(Long id, Long projectId);

  List<Dashboard> findAllByProjectId(Long projectId);

  /**
   * Checks the existence of the {@link Dashboard} with specified name for a user on a project
   *
   * @param name      {@link Dashboard#getName()}
   * @param owner     {@link Dashboard#getOwner()}
   * @param projectId Id of the {@link Project} on which dashboard existence will be checked
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndOwnerAndProjectId(String name, String owner, Long projectId);

  /**
   * Checks the existence of the {@link Dashboard} with specified name on a project
   *
   * @param name      {@link Dashboard#getName()}
   * @param projectId {@link Project#getId()}
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndProjectId(String name, Long projectId);


  /**
   * Toggles the lock flag for the specified dashboard and all related widgets.
   *
   * <p>Performs native update statements:
   * <ul>
   *   <li>Updates the dashboard owned_entity row.</li>
   *   <li>Updates owned_entity rows for widgets linked to the dashboard.</li>
   * </ul>
   *
   * @param dashboardId id of the dashboard to toggle lock for
   */
  @Modifying
  @Query(value = """
           WITH widget_ids AS (SELECT widget_id FROM dashboard_widget WHERE dashboard_id = :dashboardId)
           UPDATE owned_entity SET locked = true
           WHERE id = :dashboardId
           OR id IN (SELECT widget_id FROM widget_ids);
      """, nativeQuery = true)
  void lockDashboard(@Param("dashboardId") Long dashboardId);


  @Modifying
  @Query(value = """
           WITH widget_ids AS (SELECT widget_id FROM dashboard_widget WHERE dashboard_id = :dashboardId)
           UPDATE owned_entity SET locked = false
           WHERE id = :dashboardId
           OR id IN (SELECT widget_id FROM widget_ids);
      """, nativeQuery = true)
  void unlockDashboard(@Param("dashboardId") Long dashboardId);

}
