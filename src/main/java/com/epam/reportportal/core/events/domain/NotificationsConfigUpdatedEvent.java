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

import com.epam.reportportal.model.project.ProjectResource;
import com.epam.reportportal.model.project.email.ProjectNotificationConfigDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when project notification configuration is updated.
 *
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class NotificationsConfigUpdatedEvent extends AbstractEvent<ProjectResource> {

  private ProjectNotificationConfigDTO updateProjectNotificationConfigRq;

  /**
   * Constructs a NotificationsConfigUpdatedEvent.
   *
   * @param before                            The project resource state before the update
   * @param updateProjectNotificationConfigRq The updated notification configuration
   * @param userId                            The ID of the user who updated the configuration
   * @param userLogin                         The login of the user who updated the configuration
   * @param orgId                             The organization ID
   */
  public NotificationsConfigUpdatedEvent(ProjectResource before,
      ProjectNotificationConfigDTO updateProjectNotificationConfigRq,
      Long userId, String userLogin, Long orgId) {
    super(userId, userLogin);
    this.before = before;
    this.updateProjectNotificationConfigRq = updateProjectNotificationConfigRq;
    this.organizationId = orgId;
  }

}
