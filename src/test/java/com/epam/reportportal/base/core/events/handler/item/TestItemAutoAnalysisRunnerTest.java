/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.events.handler.item;

import static com.epam.reportportal.base.core.events.handler.item.TestItemAutoAnalysisRunner.IMMEDIATE_AUTO_ANALYSIS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.analyzer.auto.AnalyzerService;
import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.events.domain.item.TestItemFinishedEvent;
import com.epam.reportportal.base.core.launch.GetLaunchHandler;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestItemAutoAnalysisRunnerTest {

  private final AnalyzerService analyzerService = mock(AnalyzerService.class);
  private final LogIndexer logIndexer = mock(LogIndexer.class);
  private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
  private final TestItemAutoAnalysisRunner runner =
      new TestItemAutoAnalysisRunner(analyzerService, logIndexer, getLaunchHandler);

  @Test
  void applicableToInvestigateItemWithSystemTrueAttribute() {
    TestItem item = item(TestItemIssueGroup.TO_INVESTIGATE, false,
        new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", true));

    assertTrue(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
    verifyNoInteractions(analyzerService, logIndexer, getLaunchHandler);
  }

  @Test
  void notApplicableWithoutAttribute() {
    TestItem item = item(TestItemIssueGroup.TO_INVESTIGATE, false,
        new ItemAttribute("os", "linux", false));

    assertFalse(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
  }

  @Test
  void notApplicableWithFalseAttribute() {
    TestItem item = item(TestItemIssueGroup.TO_INVESTIGATE, false,
        new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "false", true));

    assertFalse(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
  }

  @Test
  void notApplicableWithNonSystemAttribute() {
    TestItem item = item(TestItemIssueGroup.TO_INVESTIGATE, false,
        new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", false));

    assertFalse(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
  }

  @Test
  void notApplicableWhenIssueIgnoresAnalyzer() {
    TestItem item = item(TestItemIssueGroup.TO_INVESTIGATE, true,
        new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", true));

    assertFalse(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
  }

  @Test
  void notApplicableToItemOutsideToInvestigate() {
    TestItem item = item(TestItemIssueGroup.PRODUCT_BUG, false,
        new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", true));

    assertFalse(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
  }

  @Test
  void notApplicableToItemWithoutIssue() {
    TestItem item = new TestItem();
    item.setItemResults(new TestItemResults());
    item.getAttributes().add(new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", true));

    assertFalse(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
  }

  @Test
  void notApplicableToItemWithoutResults() {
    TestItem item = new TestItem();
    item.getAttributes().add(new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", true));

    assertFalse(runner.isApplicable(new TestItemFinishedEvent(item, 1L)));
  }

  @Test
  void handleRunsAnalyzersWhenAnalyzerIsAvailable() {
    TestItem item = item(TestItemIssueGroup.TO_INVESTIGATE, false,
        new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", true));
    Launch launch = new Launch();
    when(analyzerService.hasAnalyzers()).thenReturn(true);
    when(getLaunchHandler.get(2L)).thenReturn(launch);

    runner.handle(new TestItemFinishedEvent(item, 1L), Map.of());

    verify(analyzerService).runAnalyzers(eq(launch), eq(List.of(3L)), any());
    verify(logIndexer, times(2)).indexItemsLogs(eq(1L), eq(2L), eq(List.of(3L)), any());
  }

  @Test
  void handleDoesNothingWithoutAnalyzers() {
    TestItem item = item(TestItemIssueGroup.TO_INVESTIGATE, false,
        new ItemAttribute(IMMEDIATE_AUTO_ANALYSIS, "true", true));
    when(analyzerService.hasAnalyzers()).thenReturn(false);

    runner.handle(new TestItemFinishedEvent(item, 1L), Map.of());

    verify(analyzerService, never()).runAnalyzers(any(), any(), any());
    verifyNoInteractions(logIndexer, getLaunchHandler);
  }

  private static TestItem item(TestItemIssueGroup group, boolean ignoreAnalyzer,
      ItemAttribute attribute) {
    IssueType issueType = new IssueType();
    issueType.setIssueGroup(new IssueGroup(group));
    IssueEntity issue = new IssueEntity();
    issue.setIssueType(issueType);
    issue.setIgnoreAnalyzer(ignoreAnalyzer);
    TestItemResults results = new TestItemResults();
    results.setIssue(issue);
    TestItem item = new TestItem();
    item.setItemId(3L);
    item.setLaunchId(2L);
    item.setItemResults(results);
    item.getAttributes().add(attribute);
    return item;
  }
}
