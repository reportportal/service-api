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

import com.epam.reportportal.model.activity.NotificationRuleActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a project notification rule is created.
 */
@Getter
@NoArgsConstructor
public class NotificationRuleCreatedEvent extends AbstractEvent<Void> {

  private NotificationRuleActivityResource notificationRuleActivityResource;

  /**
   * Constructs a NotificationRuleCreatedEvent.
   *
   * @param notificationRuleActivityResource The notification rule activity resource
   * @param userId The ID of the user who created the rule
   * @param userLogin The login of the user who created the rule
   * @param orgId The organization ID
   */
  public NotificationRuleCreatedEvent(
      NotificationRuleActivityResource notificationRuleActivityResource,
      Long userId, String userLogin, Long orgId) {
    super(userId, userLogin);
    this.notificationRuleActivityResource = notificationRuleActivityResource;
    this.organizationId = orgId;
  }
}
