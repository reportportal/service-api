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

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event publish when organization is created.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Getter
@NoArgsConstructor
public class OrganizationCreatedEvent extends AbstractEvent<Void> {

  private String organizationName;

  /**
   * Constructs a new OrganizationCreatedEvent.
   *
   * @param userId           The ID of the user who created the organization.
   * @param userLogin        The login of the user who created the organization.
   * @param organizationId   The ID of the created organization.
   * @param organizationName The name of the created organization.
   */
  public OrganizationCreatedEvent(Long userId, String userLogin, Long organizationId,
      String organizationName) {
    super(userId, userLogin);
    this.organizationId = organizationId;
    this.organizationName = organizationName;
  }

  /**
   * Constructs a new OrganizationCreatedEvent (without userId, userLogin - system event).
   *
   * @param organizationId   The ID of the created organization.
   * @param organizationName The name of the created organization.
   */
  public OrganizationCreatedEvent(Long organizationId, String organizationName) {
    super();
    this.organizationId = organizationId;
    this.organizationName = organizationName;
  }

}
