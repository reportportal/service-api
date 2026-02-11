/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueGroup;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IssueGroupRepositoryTest extends BaseMvcTest {

  @Autowired
  private IssueGroupRepository repository;

  @Test
  void findByTestItemIssueGroup() {
    Arrays.stream(TestItemIssueGroup.values())
        .filter(it -> !it.equals(TestItemIssueGroup.NOT_ISSUE_FLAG)).forEach(it -> {
          final IssueGroup issueGroup = repository.findByTestItemIssueGroup(it);
          assertEquals(it, issueGroup.getTestItemIssueGroup(), "Incorrect issue group");
          assertNotNull(issueGroup.getId(), "Issue group should have id");
        });
  }
}
