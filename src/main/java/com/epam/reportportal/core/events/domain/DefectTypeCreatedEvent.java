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
import lombok.Setter;

/**
 * Event published when a defect type is created.
 *
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class DefectTypeCreatedEvent extends AbstractEvent<Void> {

  private IssueTypeActivityResource issueTypeActivityResource;

  /**
   * Constructs a DefectTypeCreatedEvent.
   *
   * @param issueTypeActivityResource The issue type activity resource
   * @param userId The ID of the user who created the defect type
   * @param userLogin The login of the user who created the defect type
   * @param projectId The project ID
   * @param orgId The organization ID
   */
  public DefectTypeCreatedEvent(IssueTypeActivityResource issueTypeActivityResource, Long userId,
      String userLogin, Long projectId, Long orgId) {
    super(userId, userLogin);
    this.issueTypeActivityResource = issueTypeActivityResource;
    this.projectId = projectId;
    this.organizationId = orgId;
  }
}
