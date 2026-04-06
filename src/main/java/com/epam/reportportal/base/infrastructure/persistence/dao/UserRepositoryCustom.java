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

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Pavel Bortnik
 */
public interface UserRepositoryCustom extends FilterableRepository<User> {

  Optional<User> findRawById(Long id);

  /**
   * Finds entities list according provided filter
   *
   * @param filter   Filter - Query representation
   * @param pageable Page Representation
   * @param exclude  Fields to exclude from query
   * @return Found Paged objects
   */
  Page<User> findByFilterExcluding(Queryable filter, Pageable pageable, String... exclude);

  Page<User> findProjectUsersByFilterExcluding(String projectKey, Queryable filter,
      Pageable pageable, String... exclude);

  Map<String, ProjectRole> findUsernamesWithProjectRolesByProjectId(Long projectId);

  /**
   * Finds details about user and his project by login.
   *
   * @param login Login to find
   * @return User details
   */
  Optional<ReportPortalUser> findUserDetails(String login);

  Optional<ReportPortalUser> findReportPortalUser(String login);

  Optional<ReportPortalUser> findReportPortalUser(Long userId);
}
