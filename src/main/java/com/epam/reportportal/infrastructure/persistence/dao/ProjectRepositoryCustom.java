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

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.entity.attribute.Attribute;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Pavel Bortnik
 */
public interface ProjectRepositoryCustom extends FilterableRepository<Project> {

  /**
   * Find project entity without fetching related entities
   *
   * @param name Project name to search
   * @return {@link Optional} with {@link Project}
   */
  Optional<Project> findRawByName(String name);

  /**
   * Find projects info by filter
   *
   * @param filter Filter
   * @return List of project info objects
   */
  List<ProjectInfo> findProjectInfoByFilter(Queryable filter);

  /**
   * Find projects info by filter with paging
   *
   * @param filter   Filter
   * @param pageable Paging
   * @return Page of project info objects
   */
  Page<ProjectInfo> findProjectInfoByFilter(Queryable filter, Pageable pageable);

  /**
   * Find all project names
   *
   * @return List of project names
   */
  List<String> findAllProjectNames();

  /**
   * Find all project names, which contain provided term
   *
   * @return List of project names
   */
  List<String> findAllProjectNamesByTerm(String term);

  List<Project> findAllByUserLogin(String login);

  /**
   * Get {@link Page} of {@link Project#getId()} with attributes
   *
   * @param pageable {@link Pageable}
   * @return {@link Page} of {@link Project}s that contain only {@link Project#getId()}, {@link Attribute#getName()} and
   * {@link ProjectAttribute#getValue()}
   */
  Page<Project> findAllIdsAndProjectAttributes(Pageable pageable);

}
