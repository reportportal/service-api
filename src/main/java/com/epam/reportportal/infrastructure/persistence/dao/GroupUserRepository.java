/*
 * Copyright 2025 EPAM Systems
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

import com.epam.reportportal.infrastructure.persistence.entity.group.GroupUser;
import com.epam.reportportal.infrastructure.persistence.entity.group.GroupUserId;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository for {@link GroupUser}.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 * @see GroupUser
 */
public interface GroupUserRepository extends ReportPortalRepository<GroupUser, GroupUserId> {

  /**
   * Finds all users by group id.
   *
   * @param groupId group id
   * @return {@link List} of {@link GroupUser}
   */
  List<GroupUser> findAllByGroupId(Long groupId);

  /**
   * Finds all users by group id with pagination.
   *
   * @param groupId  group id
   * @param pageable {@link Pageable}
   * @return {@link Page} of {@link GroupUser}
   */
  Page<GroupUser> findAllByGroupId(Long groupId, Pageable pageable);
}
