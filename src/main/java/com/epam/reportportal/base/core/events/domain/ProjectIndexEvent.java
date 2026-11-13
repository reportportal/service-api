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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a project indexing operation starts or finishes.
 *
 * @author Pavel Bortnik
 */
@Setter
@Getter
@NoArgsConstructor
public class ProjectIndexEvent extends AbstractEvent<Void> {

  private String projectName;
  private boolean indexing;

  /**
   * Constructs a ProjectIndexEvent.
   *
   * @param userId      The ID of the user who triggered the indexing
   * @param userLogin   The login of the user who triggered the indexing
   * @param projectId   The project ID
   * @param projectName The project name
   * @param indexing    Whether indexing is starting (true) or finishing (false)
   * @param orgId       The organization ID
   */
  public ProjectIndexEvent(Long userId, String userLogin, Long projectId, String projectName,
      boolean indexing, Long orgId) {
    super(userId, userLogin);
    this.projectId = projectId;
    this.projectName = projectName;
    this.indexing = indexing;
    this.organizationId = orgId;
  }
}
