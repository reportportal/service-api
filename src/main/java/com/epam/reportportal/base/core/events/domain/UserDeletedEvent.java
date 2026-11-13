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

package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.model.activity.UserActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Fired when a user account is deleted.
 *
 * @author Andrei Varabyeu
 */
@Getter
@NoArgsConstructor
public class UserDeletedEvent extends AbstractEvent<UserActivityResource> {

  /**
   * Constructs a UserDeletedEvent.
   *
   * @param userActivityResource The user activity resource
   * @param userId               The ID of the user who deleted the user
   * @param username             The username of the user who deleted the user
   */
  public UserDeletedEvent(UserActivityResource userActivityResource, Long userId, String username) {
    super(userId, username);
    this.before = userActivityResource;
  }
}
