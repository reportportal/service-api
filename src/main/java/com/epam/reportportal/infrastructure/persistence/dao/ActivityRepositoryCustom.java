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
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import java.time.Duration;
import java.util.List;
import org.springframework.data.domain.Sort;

/**
 * @author Ihar Kahadouski
 */
public interface ActivityRepositoryCustom extends FilterableRepository<Activity> {

  /**
   * Delete outdated activities
   *
   * @param projectId ID of project
   * @param period    Time period
   */
  void deleteModifiedLaterAgo(Long projectId, Duration period);

  /**
   * Find limiting count of results
   *
   * @param filter Filter
   * @param sort   Sorting details
   * @param limit  Maximum number of returning items
   * @return Found activities
   */
  List<Activity> findByFilterWithSortingAndLimit(Queryable filter, Sort sort, int limit);
}
