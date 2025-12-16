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
 * Event published when a user's role is changed.
 */
@Getter
@NoArgsConstructor
public class ChangeRoleEvent extends AbstractEvent<Void> {

  private UserActivityResource userActivityResource;
  private String oldRole;
  private String newRole;

  /**
   * Constructs a ChangeRoleEvent.
   *
   * @param userActivityResource The user activity resource
   * @param oldRole The previous role
   * @param newRole The new role
   * @param userId The ID of the user making the change
   * @param userLogin The login of the user making the change
   * @param orgId The organization ID
   */
  public ChangeRoleEvent(UserActivityResource userActivityResource, String oldRole, String newRole,
      Long userId, String userLogin, Long orgId) {
    super(userId, userLogin);
    this.userActivityResource = userActivityResource;
    this.oldRole = oldRole;
    this.newRole = newRole;
    this.organizationId = orgId;
  }

}
