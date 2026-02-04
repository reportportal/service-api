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

import java.util.Collection;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when multiple projects are deleted.
 *
 * @author Ryhor_Kukharenka
 */
@Getter
@NoArgsConstructor
public class ProjectBulkDeletedEvent extends AbstractEvent<Void> {

  private Collection<String> projectNames;

  /**
   * Constructs a ProjectBulkDeletedEvent.
   *
   * @param userId       The ID of the user who deleted the projects
   * @param userLogin    The login of the user who deleted the projects
   * @param projectNames The names of the deleted projects
   */
  public ProjectBulkDeletedEvent(Long userId, String userLogin, Collection<String> projectNames) {
    super(userId, userLogin);
    this.projectNames = projectNames;
  }
}
