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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.fromTestItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.model.analyzer.RelevantItemInfo;
import com.epam.ta.reportportal.model.project.UniqueErrorConfig;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bortnik
 */
class AnalyzerUtilsTest {

  @Test
  void testConverting() {
    TestItem testItem = createTest();
    testItem.getItemResults().setIssue(createIssue(false));
    IndexTestItem indexTestItem = fromTestItem(testItem);
    indexTestItem.setLogs(createSameLogs(5));
    assertEquals(testItem.getItemId(), indexTestItem.getTestItemId());
    assertEquals(testItem.getUniqueId(), indexTestItem.getUniqueId());
    assertEquals(testItem.getStartTime(), indexTestItem.getStartTime());
    assertEquals(testItem.getItemResults().getIssue().getIssueType().getLocator(),
        indexTestItem.getIssueTypeLocator()
    );
    assertEquals(1, indexTestItem.getLogs().size());
    assertFalse(indexTestItem.isAutoAnalyzed());
  }

  @Test
  void testConvertingAnalyzed() {
    TestItem test = createTest();
    test.getItemResults().setIssue(createIssue(true));
    IndexTestItem indexTestItem = fromTestItem(test);
    indexTestItem.setLogs(createSameLogs(1));
    assertTrue(indexTestItem.isAutoAnalyzed());
  }

  @Test
  void testAnalyzerConfig() {
    AnalyzerConfig config = AnalyzerUtils.getAnalyzerConfig(project());
    assertEquals(String.valueOf(config.getIsAutoAnalyzerEnabled()),
        ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getDefaultValue()
    );
    assertEquals(String.valueOf(config.getNumberOfLogLines()),
        ProjectAttributeEnum.NUMBER_OF_LOG_LINES.getDefaultValue()
    );
    assertEquals(config.getAnalyzerMode(),
        ProjectAttributeEnum.AUTO_ANALYZER_MODE.getDefaultValue()
    );
    assertEquals(String.valueOf(config.getMinShouldMatch()),
        ProjectAttributeEnum.MIN_SHOULD_MATCH.getDefaultValue()
    );
    assertEquals(String.valueOf(config.getSearchLogsMinShouldMatch()),
        ProjectAttributeEnum.SEARCH_LOGS_MIN_SHOULD_MATCH.getDefaultValue()
    );
    assertEquals(String.valueOf(config.isIndexingRunning()),
        ProjectAttributeEnum.INDEXING_RUNNING.getDefaultValue()
    );
    assertEquals(String.valueOf(config.isAllMessagesShouldMatch()),
        ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH.getDefaultValue()
    );
  }

  @Test
  void testUniqueErrorConfig() {
    final Map<String, String> configParameters =
        ProjectUtils.getConfigParameters(project().getProjectAttributes());
    final UniqueErrorConfig config = AnalyzerUtils.getUniqueErrorConfig(configParameters);
    assertEquals(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getDefaultValue(),
        String.valueOf(config.isEnabled())
    );
    assertEquals(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getDefaultValue(),
        String.valueOf(config.isRemoveNumbers())
    );
  }

  @Test
  void testFromLogs() {
    final LogFull logFull = new LogFull();
    logFull.setId(1L);
    logFull.setLogMessage("Log message");
    logFull.setLogLevel(40000);
    logFull.setClusterId(2L);

    final Set<IndexLog> indexLogs = AnalyzerUtils.fromLogs(List.of(logFull));
    final IndexLog indexLog = indexLogs.stream().findFirst().get();
    assertEquals(logFull.getId(), indexLog.getLogId());
    assertEquals(logFull.getLogMessage(), indexLog.getMessage());
    assertEquals(logFull.getLogLevel().intValue(), indexLog.getLogLevel());
    assertEquals(logFull.getClusterId(), indexLog.getClusterId());
  }

  @Test
  void testToRelevantItemInfo() {
    final TestItem testItem = new TestItem();
    testItem.setItemId(1L);
    testItem.setLaunchId(2L);
    testItem.setPath("1");

    final RelevantItemInfo itemInfo = AnalyzerUtils.TO_RELEVANT_ITEM_INFO.apply(testItem);
    assertEquals(String.valueOf(testItem.getItemId()), itemInfo.getItemId());
    assertEquals(String.valueOf(testItem.getLaunchId()), itemInfo.getLaunchId());
    assertEquals(testItem.getPath(), itemInfo.getPath());
  }

  private TestItem createTest() {
    TestItem testItem = new TestItem();
    testItem.setItemId(1L);
    testItem.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
    testItem.setUniqueId("uniqueId");
    testItem.setItemResults(new TestItemResults());
    return testItem;
  }

  private IssueEntity createIssue(boolean isAutoAnalyzed) {
    IssueType issueType = new IssueType();
    issueType.setId(1L);
    issueType.setLocator("locator");
    IssueEntity issue = new IssueEntity();
    issue.setAutoAnalyzed(isAutoAnalyzed);
    issue.setIssueType(issueType);
    return issue;
  }

  private Set<IndexLog> createSameLogs(int count) {
    Set<IndexLog> logs = new HashSet<>();
    for (int i = 0; i < count; i++) {
      IndexLog log = new IndexLog();
      log.setLogLevel(LogLevel.ERROR.toInt());
      log.setMessage("Current message of the log");
      logs.add(log);
    }
    return logs;
  }

  public static Project project() {
    Project project = new Project();
    project.setProjectAttributes(ProjectUtils.defaultProjectAttributes(project,
        Arrays.stream(ProjectAttributeEnum.values()).map(it -> {
          Attribute attribute = new Attribute();
          attribute.setName(it.getAttribute());
          return attribute;
        }).collect(Collectors.toSet())
    ));
    return project;
  }

}