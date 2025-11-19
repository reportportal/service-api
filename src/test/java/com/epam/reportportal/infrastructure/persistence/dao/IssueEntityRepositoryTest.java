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

package com.epam.reportportal.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.infrastructure.persistence.entity.item.issue.IssueEntityPojo;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/fill/item/items-fill.sql")
class IssueEntityRepositoryTest extends BaseMvcTest {

  @Autowired
  private IssueEntityRepository repository;

  @Test
  void findAllByIssueId() {
    final Long automationBugTypeId = 2L;
    final int expectedSize = 11;

    final List<IssueEntity> issueEntities = repository.findAllByIssueTypeId(automationBugTypeId);
    assertEquals(expectedSize, issueEntities.size(), "Incorrect size of issue entities");
    issueEntities.forEach(it -> assertEquals(TestItemIssueGroup.AUTOMATION_BUG,
        it.getIssueType().getIssueGroup().getTestItemIssueGroup(),
        "Issue entities should be from 'automation bug' group"
    ));

  }

  @Test
  void insertByItemIdAndIssueTypeId() {
    int result = repository.saveMultiple(Lists.newArrayList(
        new IssueEntityPojo(1L, 1L, "description", false, false),
        new IssueEntityPojo(2L, 1L, "description", false, false)
    ));

    Assertions.assertEquals(2, result);
  }
}
