/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events.handler.item;

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  @Override
  public void handle(TestItemFinishedEvent testItemFinishedEvent,
      Map<String, String> projectConfig) {
    if (analyzerService.hasAnalyzers() && isNeedToRunAA(testItemFinishedEvent.getTestItem())) {
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

  private boolean isNeedToRunAA(TestItem testItem) {
    if (Objects.nonNull(testItem.getItemResults().getIssue()) && testItem.getItemResults()
        .getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()
        .equals(TestItemIssueGroup.TO_INVESTIGATE)) {
      return testItem.getAttributes().stream()
          .filter(at -> !at.getTestItem().getItemResults().getIssue().getIgnoreAnalyzer())
          .anyMatch(at -> IMMEDIATE_AUTO_ANALYSIS.equals(at.getKey()) && Boolean.parseBoolean(
              at.getValue()) && at.isSystem());
    }
    return false;
  }
}
