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

import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a user's type/role is changed.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@NoArgsConstructor
public class ChangeUserTypeEvent extends AbstractEvent<Void> {

  private Long targetUserId;
  private String targetUserName;
  private UserRole oldType;
  private UserRole newType;

  /**
   * Constructs a ChangeUserTypeEvent.
   *
   * @param targetUserId   The ID of the user whose type is being changed
   * @param targetUserName The username of the user whose type is being changed
   * @param oldRole        The previous user role
   * @param newRole        The new user role
   * @param editorId       The ID of the user making the change
   * @param editorUsername The username of the user making the change
   */
  public ChangeUserTypeEvent(Long targetUserId, String targetUserName, UserRole oldRole,
      UserRole newRole, Long editorId, String editorUsername) {
    super(editorId, editorUsername);
    this.targetUserId = targetUserId;
    this.targetUserName = targetUserName;
    this.oldType = oldRole;
    this.newType = newRole;
  }
}
