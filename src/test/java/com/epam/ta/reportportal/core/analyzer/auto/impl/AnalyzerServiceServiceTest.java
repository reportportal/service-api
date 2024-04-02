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

import static com.epam.ta.reportportal.entity.AnalyzeMode.ALL_LAUNCHES;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.model.analyzer.AnalyzedItemRs;
import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.reportportal.model.analyzer.IndexLog;
import com.epam.reportportal.model.analyzer.IndexTestItem;
import com.epam.reportportal.model.project.AnalyzerConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bortnik
 */
class AnalyzerServiceServiceTest {

  private AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

  private IssueTypeHandler issueTypeHandler = mock(IssueTypeHandler.class);

  private TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private LaunchRepository launchRepository = mock(LaunchRepository.class);

  private MessageBus messageBus = mock(MessageBus.class);

  private LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);

  private AnalyzerStatusCache analyzerStatusCache = mock(AnalyzerStatusCache.class);

  private AnalyzerServiceImpl issuesAnalyzer =
      new AnalyzerServiceImpl(100, analyzerStatusCache, launchPreparerService,
          analyzerServiceClient, issueTypeHandler, testItemRepository, messageBus, launchRepository
      );

  @Test
  void hasAnalyzers() {
    when(analyzerServiceClient.hasClients()).thenReturn(true);
    assertTrue(issuesAnalyzer.hasAnalyzers());
  }

  @Test
  void analyze() {
    int itemsCount = 2;

    Launch launch = launch();

    List<TestItem> items = testItemsTI(itemsCount);
    items.forEach(item -> item.setLaunchId(launch.getId()));

    AnalyzerConfig analyzerConfig = analyzerConfig();

    final IndexLaunch indexLaunch = new IndexLaunch();
    indexLaunch.setLaunchId(launch.getId());
    indexLaunch.setAnalyzerConfig(analyzerConfig);

    final List<IndexTestItem> indexTestItems =
        items.stream().map(AnalyzerUtils::fromTestItem).peek(item -> item.setLogs(errorLogs(2)))
            .collect(Collectors.toList());
    indexLaunch.setTestItems(indexTestItems);

    when(testItemRepository.findAllById(anyList())).thenReturn(items);

    when(launchPreparerService.prepare(any(Launch.class), anyList(),
        any(AnalyzerConfig.class)
    )).thenReturn(Optional.of(indexLaunch));

    when(analyzerServiceClient.analyze(any())).thenReturn(analyzedItems(itemsCount));

    when(issueTypeHandler.defineIssueType(anyLong(), eq("pb001"))).thenReturn(
        issueProductBug().getIssueType());

    issuesAnalyzer.runAnalyzers(launch,
        items.stream().map(TestItem::getItemId).collect(Collectors.toList()), analyzerConfig
    );

    verify(analyzerServiceClient, times(1)).analyze(any());
    verify(testItemRepository, times(itemsCount)).save(any());
    verify(messageBus, times(4)).publishActivity(any());
  }

  private AnalyzerConfig analyzerConfig() {
    AnalyzerConfig analyzerConfig = new AnalyzerConfig();
    analyzerConfig.setAnalyzerMode(ALL_LAUNCHES.getValue());
    return analyzerConfig;
  }

  private Project project() {
    Project project = new Project();
    project.setId(1L);
    return project;
  }

  private Launch launch() {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setName("launch");
    launch.setProjectId(1L);
    return launch;
  }

  private List<TestItem> testItemsTI(int count) {
    List<TestItem> list = new ArrayList<>(count);
    for (int i = 1; i <= count; i++) {
      TestItem test = new TestItem();
      test.setItemId((long) i);
      test.setName("test" + i);
      test.setUniqueId("unique" + i);
      test.setItemResults(new TestItemResults());
      test.getItemResults().setIssue(issueToInvestigate());
      test.getItemResults().setStatus(StatusEnum.FAILED);
      list.add(test);
    }
    return list;
  }

  private IssueEntity issueToInvestigate() {
    IssueType issueType = new IssueType();
    issueType.setLocator("ti001");
    IssueEntity issueEntity = new IssueEntity();
    issueEntity.setIssueType(issueType);
    return issueEntity;
  }

  private IssueEntity issueProductBug() {
    IssueType issueType = new IssueType();
    issueType.setLocator("pb001");
    IssueEntity issueEntity = new IssueEntity();
    issueEntity.setIssueType(issueType);
    return issueEntity;
  }

  private Set<IndexLog> errorLogs(int count) {
    Set<IndexLog> logs = new HashSet<>(count);
    for (int i = 1; i <= count; i++) {
      IndexLog log = new IndexLog();
      log.setMessage("Error message " + i);
      log.setLogLevel(LogLevel.ERROR.toInt());
      logs.add(log);
    }
    return logs;
  }

  private Map<String, List<AnalyzedItemRs>> analyzedItems(int itemsCount) {
    Map<String, List<AnalyzedItemRs>> res = new HashMap<>();
    List<AnalyzedItemRs> list = new ArrayList<>();
    for (int i = 1; i <= itemsCount; i++) {
      AnalyzedItemRs testItem = new AnalyzedItemRs();
      testItem.setItemId((long) i);
      testItem.setLocator(PRODUCT_BUG.getLocator());
      list.add(testItem);
    }
    res.put("test", list);
    return res;
  }
}