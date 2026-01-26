/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.core.user;

import com.epam.reportportal.infrastructure.persistence.entity.user.User;

/**
 * Service that provides user-related operations.
 * <p>
 * Implementations are responsible for retrieving and managing {@link User} entities used across the application.
 * </p>
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
public interface UserService {

  /**
   * Find a user by its identifier.
   *
   * @param userId identifier of the user; must not be {@code null}
   * @return the {@link User} with the given id, or {@code null} if not found
   */
  User findById(Long userId);
}
