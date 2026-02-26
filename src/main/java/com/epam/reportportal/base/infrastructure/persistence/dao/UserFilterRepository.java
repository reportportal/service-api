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
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Pavel Bortnik
 */
public interface UserFilterRepository extends ReportPortalRepository<UserFilter, Long>,
    UserFilterRepositoryCustom {

  /**
   * Finds filter by 'id' and 'project id'
   *
   * @param id        {@link UserFilter#id}
   * @param projectId Id of the {@link Project} whose filter will be extracted
   * @return {@link UserFilter} wrapped in the {@link Optional}
   */
  Optional<UserFilter> findByIdAndProjectId(Long id, Long projectId);

  /**
   * @param ids       {@link Iterable} of the filter Ids
   * @param projectId Id of the {@link Project} whose filters will be extracted
   * @return The {@link List} of the {@link UserFilter}
   */
  List<UserFilter> findAllByIdInAndProjectId(Collection<Long> ids, Long projectId);

  /**
   * @param projectId Id of the {@link Project} whose filters will be extracted
   * @return The {@link List} of the {@link UserFilter}
   */
  List<UserFilter> findAllByProjectId(Long projectId);

  /**
   * Checks the existence of the {@link UserFilter} with specified name for a user on a project
   *
   * @param name      {@link UserFilter#name}
   * @param owner     {@link UserFilter#owner}
   * @param projectId Id of the {@link Project} on which filter existence will be checked
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndOwnerAndProjectId(String name, String owner, Long projectId);

  /**
   * Checks the existence of the {@link UserFilter} with specified name on a project
   *
   * @param name      {@link UserFilter#name}
   * @param projectId Id of the {@link Project} on which filter existence will be checked
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndProjectId(String name, Long projectId);

}
