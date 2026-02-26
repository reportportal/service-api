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

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.ISSUE_TYPE_RECORD_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_GROUP;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_TYPE;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Pavel Bortnik
 */
@Repository
public class IssueTypeRepositoryCustomImpl implements IssueTypeRepositoryCustom {

  private DSLContext dsl;

  @Autowired
  public void setDsl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public List<IssueType> getDefaultIssueTypes() {
    return dsl.select()
        .from(ISSUE_TYPE)
        .join(ISSUE_GROUP)
        .on(ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
        .where(ISSUE_TYPE.LOCATOR.in(Arrays.stream(TestItemIssueGroup.values())
            .map(TestItemIssueGroup::getLocator)
            .toArray(String[]::new)))
        .fetch(ISSUE_TYPE_RECORD_MAPPER);
  }

  @Override
  public List<Long> getIssueTypeIdsByLocators(Collection<String> locators) {
    return dsl.select(ISSUE_TYPE.ID).from(ISSUE_TYPE)
        .where(ISSUE_TYPE.LOCATOR.in(locators))
        .fetch(ISSUE_TYPE.ID);
  }
}
