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

import com.epam.reportportal.base.model.activity.ProjectAttributesActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event publish when project is created.
 *
 * @author Ryhor_Kukharenka
 */
@Getter
@NoArgsConstructor
public class ProjectRenamedEvent extends AbstractEvent<ProjectAttributesActivityResource> {

  private String newProjectName;
  private String oldProjectName;

  /**
   * Constructs a ProjectCreatedEvent.
   *
   * @param userId    The ID of the user who created the project
   * @param userLogin The login of the user who created the project
   * @param projectId The ID of the created project
   * @param orgId     The organization ID
   */
  public ProjectRenamedEvent(Long userId, String userLogin, Long projectId, String oldProjectName,
      String newProjectName,
      Long orgId) {
    super(userId, userLogin);
    this.projectId = projectId;
    this.newProjectName = newProjectName;
    this.oldProjectName = oldProjectName;
    this.organizationId = orgId;
  }

}
