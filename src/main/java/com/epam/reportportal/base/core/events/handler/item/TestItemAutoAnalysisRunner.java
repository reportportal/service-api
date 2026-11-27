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

package com.epam.reportportal.base.core.events.handler.item;

import com.epam.reportportal.base.core.analyzer.auto.AnalyzerService;
import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.reportportal.base.core.events.domain.item.TestItemFinishedEvent;
import com.epam.reportportal.base.core.events.handler.ConfigurableEventHandler;
import com.epam.reportportal.base.core.launch.GetLaunchHandler;
import com.epam.reportportal.base.infrastructure.model.project.AnalyzerConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Run auto analyzer for finished test item with immediateAutoAnalysis attribute.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Component
public class TestItemAutoAnalysisRunner implements
    ConfigurableEventHandler<TestItemFinishedEvent, Map<String, String>> {

  protected static final String IMMEDIATE_AUTO_ANALYSIS = "immediateAutoAnalysis";

  private final AnalyzerService analyzerService;

  private final LogIndexer logIndexer;

  private final GetLaunchHandler getLaunchHandler;

  public TestItemAutoAnalysisRunner(AnalyzerService analyzerService, LogIndexer logIndexer,
      GetLaunchHandler getLaunchHandler) {
    this.analyzerService = analyzerService;
    this.logIndexer = logIndexer;
    this.getLaunchHandler = getLaunchHandler;
  }

  /**
   * Applies to an item that has the system attribute {@code immediateAutoAnalysis=true} and a {@code To Investigate}
   * issue that is not excluded from analysis. Reads only the item already in the event.
   */
  @Override
  public boolean isApplicable(TestItemFinishedEvent event) {
    final TestItem testItem = event.getTestItem();
    final boolean requested = testItem.getAttributes().stream()
        .anyMatch(at -> IMMEDIATE_AUTO_ANALYSIS.equals(at.getKey())
            && Boolean.parseBoolean(at.getValue()) && at.isSystem());
    if (!requested || testItem.getItemResults() == null) {
      return false;
    }
    final IssueEntity issue = testItem.getItemResults().getIssue();
    return issue != null && !Boolean.TRUE.equals(issue.getIgnoreAnalyzer())
        && TestItemIssueGroup.TO_INVESTIGATE.equals(
        issue.getIssueType().getIssueGroup().getTestItemIssueGroup());
  }

  @Override
  public void handle(TestItemFinishedEvent testItemFinishedEvent,
      Map<String, String> projectConfig) {
    if (analyzerService.hasAnalyzers()) {
      final AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(projectConfig);
      TestItem testItem = testItemFinishedEvent.getTestItem();
      logIndex(testItem, testItemFinishedEvent.getProjectId(), analyzerConfig);
      Launch launch = getLaunchHandler.get(testItem.getLaunchId());
      analyzerService.runAnalyzers(launch, List.of(testItem.getItemId()), analyzerConfig);
      logIndex(testItem, testItemFinishedEvent.getProjectId(), analyzerConfig);
    }
  }

  private void logIndex(TestItem testItem, Long projectId, AnalyzerConfig config) {
    logIndexer.indexItemsLogs(projectId, testItem.getLaunchId(), List.of(testItem.getItemId()),
        config);
  }
}
