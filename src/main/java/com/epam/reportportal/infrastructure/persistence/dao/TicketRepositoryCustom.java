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

import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import java.time.Instant;
import java.util.List;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface TicketRepositoryCustom {

  /**
   * Find tickets that contains a term as a part inside for specified launch
   *
   * @param launchId Launch id
   * @param term     A part of ticket id
   * @return List of ticket ids
   */
  List<String> findByLaunchIdAndTerm(Long launchId, String term);

  /**
   * Find tickets that contains a term as a part inside for specified project
   *
   * @param projectId {@link Project#getId()}
   * @param term      A part of ticket id
   * @return List of ticket ids
   */
  List<String> findByProjectIdAndTerm(Long projectId, String term);

  /**
   * Returns number of unique tickets on specified project posted before {@code from} parameter
   *
   * @param projectId {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project#id} Id of project
   * @param from      Date threshold
   * @return Number of unique tickets
   */
  Integer findUniqueCountByProjectBefore(Long projectId, Instant from);

}
