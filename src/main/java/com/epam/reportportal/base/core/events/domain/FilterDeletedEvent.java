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

import com.epam.reportportal.base.model.activity.UserFilterActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a user filter is deleted.
 *
 * @author pavel_bortnik
 */
@Getter
@NoArgsConstructor
public class FilterDeletedEvent extends AbstractEvent<UserFilterActivityResource> {

  /**
   * Constructs a FilterDeletedEvent.
   *
   * @param before    The filter state before deletion
   * @param userId    The ID of the user who deleted the filter
   * @param userLogin The login of the user who deleted the filter
   * @param orgId     The organization ID
   */
  public FilterDeletedEvent(UserFilterActivityResource before, Long userId, String userLogin,
      Long orgId) {
    super(userId, userLogin, before, null);
    this.organizationId = orgId;
  }
}
