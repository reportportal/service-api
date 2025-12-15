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
 * Event publish when project is deleted.
 *
 * @author Ryhor_Kukharenka
 */
@Getter
@NoArgsConstructor
public class ProjectDeletedEvent extends AbstractEvent<Void> {

  private String projectName;

  /**
   * Constructs a ProjectDeletedEvent.
   *
   * @param userId The ID of the user who deleted the project
   * @param userLogin The login of the user who deleted the project
   * @param projectId The ID of the deleted project
   * @param projectName The name of the deleted project
   * @param organizationId The organization ID
   */
  public ProjectDeletedEvent(Long userId, String userLogin, Long projectId, String projectName,
      Long organizationId) {
    super(userId, userLogin);
    this.projectId = projectId;
    this.projectName = projectName;
    this.organizationId = organizationId;
  }

}
