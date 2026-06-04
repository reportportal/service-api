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

import com.epam.reportportal.base.infrastructure.persistence.entity.preference.UserPreference;
import java.util.List;
import java.util.Optional;

/**
 * User preference repository
 *
 * @author Pavel Bortnik
 */
public interface UserPreferenceRepository extends ReportPortalRepository<UserPreference, Long> {

  /**
   * Find user preferences by project and user
   *
   * @param projectId Project id
   * @param userId    User id
   * @return List of user preferences
   */
  List<UserPreference> findByProjectIdAndUserId(Long projectId, Long userId);

  /**
   * Find unique user preference
   *
   * @param projectId Project id
   * @param userId    User id
   * @param filterId  Filter id
   * @return Optional of {@link UserPreference}
   */
  Optional<UserPreference> findByProjectIdAndUserIdAndFilterId(Long projectId, Long userId,
      Long filterId);

  /**
   * Remove user preferences by project and user
   *
   * @param projectId Project id
   * @param userId    User id
   */
  void removeByProjectIdAndUserId(Long projectId, Long userId);

}
