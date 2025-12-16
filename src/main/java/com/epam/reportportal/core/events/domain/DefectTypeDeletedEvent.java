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

import com.epam.reportportal.model.activity.IssueTypeActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a defect type is deleted.
 *
 * @author Andrei Varabyeu
 */
@Getter
@NoArgsConstructor
public class DefectTypeDeletedEvent extends AbstractEvent<IssueTypeActivityResource> {

  /**
   * Constructs a DefectTypeDeletedEvent.
   *
   * @param before The defect type state before deletion
   * @param userId The ID of the user who deleted the defect type
   * @param userLogin The login of the user who deleted the defect type
   * @param projectId The project ID
   * @param orgId The organization ID
   */
  public DefectTypeDeletedEvent(IssueTypeActivityResource before, Long userId, String userLogin,
      Long projectId, Long orgId) {
    super(userId, userLogin, before, null);
    this.projectId = projectId;
    this.organizationId = orgId;
  }
}
