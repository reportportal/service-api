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

package com.epam.reportportal.core.events.domain;

import com.epam.reportportal.model.activity.UserActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Andrei Varabyeu
 */
@Getter
@NoArgsConstructor
public class UsersDeletedEvent extends AbstractEvent<UserActivityResource> {

  /**
   * Constructs a UsersDeletedEvent.
   *
   * @param userActivityResource The user activity resource
   * @param userId The ID of the user who deleted the users
   * @param userName The name of the user who deleted the users
   */
  public UsersDeletedEvent(UserActivityResource userActivityResource, Long userId,
      String userName) {
    super(userId, userName);
    this.before = userActivityResource;
  }
}
