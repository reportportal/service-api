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

package com.epam.reportportal.base.core.events.domain.tms;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestCaseDeletedEvent extends AbstractEvent<TestCaseActivityResource> {

  public TestCaseDeletedEvent(TestCaseActivityResource before, Long userId, String userLogin,
      Long organizationId) {
    super(userId, userLogin, before, null);
    this.organizationId = organizationId;
  }

  public TestCaseDeletedEvent(Long testCaseId, Long userId, String username, MembershipDetails membershipDetails) {
    this(
        TestCaseActivityResource
            .builder()
            .id(testCaseId)
            .projectId(membershipDetails.getProjectId())
            .build(),
        userId,
        username,
        membershipDetails.getOrgId()
    );
  }
}
