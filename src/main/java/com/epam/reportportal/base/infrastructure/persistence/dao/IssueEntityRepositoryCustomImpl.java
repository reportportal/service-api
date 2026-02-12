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

import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JIssue.ISSUE;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntityPojo;
import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.records.JIssueRecord;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep5;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Repository
@RequiredArgsConstructor
public class IssueEntityRepositoryCustomImpl implements IssueEntityRepositoryCustom {

  private final DSLContext dslContext;

  @Override
  public int saveMultiple(List<IssueEntityPojo> issueEntities) {

    InsertValuesStep5<JIssueRecord, Long, Long, String, Boolean, Boolean> columns = dslContext.insertInto(
            ISSUE)
        .columns(ISSUE.ISSUE_ID, ISSUE.ISSUE_TYPE, ISSUE.ISSUE_DESCRIPTION, ISSUE.AUTO_ANALYZED,
            ISSUE.IGNORE_ANALYZER);

    issueEntities.forEach(issue -> columns.values(issue.getItemId(),
        issue.getIssueTypeId(),
        issue.getDescription(),
        issue.isAutoAnalyzed(),
        issue.isIgnoreAnalyzer()
    ));

    return columns.execute();

  }
}
