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

import com.epam.reportportal.model.activity.IntegrationActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when an integration is updated.
 *
 * @author Andrei Varabyeu
 */
@Getter
@NoArgsConstructor
public class IntegrationUpdatedEvent extends AbstractEvent<IntegrationActivityResource> {

  /**
   * Constructs an IntegrationUpdatedEvent.
   *
   * @param userId The ID of the user who updated the integration
   * @param userLogin The login of the user who updated the integration
   * @param before The integration state before the update
   * @param after The integration state after the update
   */
  public IntegrationUpdatedEvent(Long userId, String userLogin, IntegrationActivityResource before,
      IntegrationActivityResource after) {
    super(userId, userLogin, before, after);
  }

  /**
   * Constructs an IntegrationUpdatedEvent.
   *
   * @param userId The ID of the user who updated the integration
   * @param userLogin The login of the user who updated the integration
   * @param before The integration state before the update
   * @param after The integration state after the update
   * @param orgId The organization ID
   */
  public IntegrationUpdatedEvent(Long userId, String userLogin, IntegrationActivityResource before,
      IntegrationActivityResource after, Long orgId) {
    super(userId, userLogin, before, after);
    this.organizationId = orgId;
  }
}
