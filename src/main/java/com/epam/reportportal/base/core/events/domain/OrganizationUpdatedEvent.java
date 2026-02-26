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

import com.epam.reportportal.base.model.activity.OrganizationAttributesActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event publish when organization is updated.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Getter
@NoArgsConstructor
public class OrganizationUpdatedEvent extends
    AbstractEvent<OrganizationAttributesActivityResource> {

  private String organizationName;

  /**
   * Constructs an OrganizationUpdatedEvent.
   *
   * @param userId           The ID of the user who performed the update.
   * @param userLogin        The login of the user who performed the update.
   * @param organizationId   The ID of the organization that was updated.
   * @param organizationName The name of the organization that was updated.
   * @param before           The state of the organization before the update.
   * @param after            The state of the organization after the update.
   */
  public OrganizationUpdatedEvent(
      Long userId,
      String userLogin,
      Long organizationId,
      String organizationName,
      OrganizationAttributesActivityResource before,
      OrganizationAttributesActivityResource after
  ) {
    super(userId, userLogin, before, after);
    this.organizationId = organizationId;
    this.organizationName = organizationName;
  }
}
