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

import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@NoArgsConstructor
public class ChangeUserTypeEvent extends AbstractEvent<Void> {

  private Long userId;
  private String userName;
  private UserRole oldType;
  private UserRole newType;

  public ChangeUserTypeEvent(Long userId, String userName, UserRole oldRole, UserRole newRole,
      Long editorId, String editorUsername) {
    super(editorId, editorUsername);
    this.userId = userId;
    this.userName = userName;
    this.oldType = oldRole;
    this.newType = newRole;
  }
}
