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
 * Event published when an import process finishes.
 *
 * @author Pavel Bortnik
 */
@Getter
@NoArgsConstructor
public class ImportFinishedEvent extends AbstractEvent<Void> {

  private String fileName;

  /**
   * Constructs an ImportFinishedEvent.
   *
   * @param userId The ID of the user who finished the import
   * @param userLogin The login of the user who finished the import
   * @param projectId The project ID
   * @param fileName The name of the imported file
   * @param orgId The organization ID
   */
  public ImportFinishedEvent(Long userId, String userLogin, Long projectId, String fileName,
      Long orgId) {
    super(userId, userLogin);
    this.projectId = projectId;
    this.fileName = fileName;
    this.organizationId = orgId;
  }

}
