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
 * Event triggered when a user is unassigned from a project. Uses {@code after} field to hold the
 * unassigned user resource (before=null for CREATE-like events).
 */
@Getter
@NoArgsConstructor
public class UnassignUserEvent extends AbstractEvent<UserActivityResource> {

  /**
   * Constructs an UnassignUserEvent.
   *
   * @param userActivityResource The user activity resource
   * @param userId The ID of the user who unassigned the user
   * @param userLogin The login of the user who unassigned the user
   * @param orgId The organization ID
   */
  public UnassignUserEvent(UserActivityResource userActivityResource, Long userId,
      String userLogin, Long orgId) {
    super(userId, userLogin);
    this.after = userActivityResource;
    this.organizationId = orgId;
  }

  /**
   * Constructs an UnassignUserEvent.
   *
   * @param userActivityResource The user activity resource
   */
  public UnassignUserEvent(UserActivityResource userActivityResource) {
    super();
    this.after = userActivityResource;
  }

  public UnassignUserEvent(UserActivityResource userActivityResource, Long orgId) {
    super();
    this.after = userActivityResource;
    this.organizationId = orgId;
  }

  /**
   * Convenience method to get the unassigned user resource. Equivalent to {@code getAfter()}.
   */
  public UserActivityResource getUserActivityResource() {
    return getAfter();
  }

}
