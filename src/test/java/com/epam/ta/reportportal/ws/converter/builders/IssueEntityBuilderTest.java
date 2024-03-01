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

package com.epam.ta.reportportal.ws.converter.builders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IssueEntityBuilderTest {

  @Test
  void issueEntityBuilder() {
    final boolean autoAnalyzed = false;
    final boolean ignoreAnalyzer = true;
    final IssueType issueType = new IssueType(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG),
        "locator",
        "longName",
        "shortName",
        "color"
    );
    final String description = "description";

    final IssueEntity issueEntity = new IssueEntityBuilder().addAutoAnalyzedFlag(autoAnalyzed)
        .addIgnoreFlag(ignoreAnalyzer)
        .addIssueType(issueType)
        .addDescription(description)
        .get();

    assertEquals(autoAnalyzed, issueEntity.getAutoAnalyzed());
    assertEquals(ignoreAnalyzer, issueEntity.getIgnoreAnalyzer());
    assertThat(issueEntity.getIssueType()).isEqualToComparingFieldByField(issueType);
    assertEquals(description, issueEntity.getIssueDescription());
  }
}