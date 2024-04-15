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

package com.epam.ta.reportportal.ws.converter.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.ws.reporting.Issue;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IssueConverterTest {

  @Test
  void toModel() {
    final IssueEntity issueEntity = getIssueEntity();
    final Issue resource = IssueConverter.TO_MODEL.apply(issueEntity);

    assertEquals(resource.isAutoAnalyzed(), issueEntity.getAutoAnalyzed());
    assertEquals(resource.getComment(), issueEntity.getIssueDescription());
    assertEquals(resource.isIgnoreAnalyzer(), issueEntity.getIgnoreAnalyzer());
    assertEquals(resource.getIssueType(), issueEntity.getIssueType().getLocator());
  }

  @Test
  void toResource() {
    final Issue issue = getIssue();
    final IssueEntity issueEntity = IssueConverter.TO_ISSUE.apply(issue);

    assertEquals(issueEntity.getIgnoreAnalyzer(), issue.isIgnoreAnalyzer());
    assertEquals(issueEntity.getAutoAnalyzed(), issue.isAutoAnalyzed());
    assertEquals(issue.getComment(), issue.getComment());
  }

  private static IssueEntity getIssueEntity() {
    final IssueEntity issue = new IssueEntity();
    issue.setIssueType(
        new IssueType(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG), "locator", "long name", "SNA",
            "color"));
    issue.setIgnoreAnalyzer(false);
    issue.setAutoAnalyzed(false);
    issue.setIssueDescription("issue description");
    return issue;
  }

  private static Issue getIssue() {
    Issue issue = new Issue();
    issue.setComment("comment");
    issue.setIgnoreAnalyzer(false);
    issue.setAutoAnalyzed(false);
    return issue;
  }
}
