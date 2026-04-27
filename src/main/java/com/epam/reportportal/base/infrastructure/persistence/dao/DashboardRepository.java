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
   * @param id        {@link Dashboard#id}
   * @param projectId Id of the {@link Project} whose dashboard will be extracted
   * @return {@link Dashboard} wrapped in the {@link Optional}
   */
  Optional<Dashboard> findByIdAndProjectId(Long id, Long projectId);

  List<Dashboard> findAllByProjectId(Long projectId);

  /**
   * Checks the existence of the {@link Dashboard} with specified name for a user on a project
   *
   * @param name      {@link Dashboard#name}
   * @param owner     {@link Dashboard#owner}
   * @param projectId Id of the {@link Project} on which dashboard existence will be checked
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndOwnerAndProjectId(String name, String owner, Long projectId);

  /**
   * Checks the existence of the {@link Dashboard} with specified name on a project
   *
   * @param name      {@link Dashboard#name}
   * @param projectId {@link Project#id}
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndProjectId(String name, Long projectId);

}
