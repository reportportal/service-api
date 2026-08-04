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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.reportportal.model.project.AnalyzerConfig;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bortnik
 */
class AnalyzerServiceServiceTest {

  private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private final LaunchRepository launchRepository = mock(LaunchRepository.class);

  private final LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);

  private final AnalyzerStatusCache analyzerStatusCache = mock(AnalyzerStatusCache.class);

  private final AnalyzerServiceImpl issuesAnalyzer =
      new AnalyzerServiceImpl(100, analyzerStatusCache, launchPreparerService,
          analyzerServiceClient,
          testItemRepository, launchRepository);

  @Test
  void hasAnalyzers() {
    when(analyzerServiceClient.hasClients()).thenReturn(true);
    assertTrue(issuesAnalyzer.hasAnalyzers());
  }

  @Test
  void runAnalyzersDispatchesToClient() {
    int itemsCount = 2;

    Launch launch = launch();
    List<TestItem> items = testItemsTI(itemsCount);
    items.forEach(item -> item.setLaunchId(launch.getId()));

    AnalyzerConfig analyzerConfig = analyzerConfig();

    IndexLaunch indexLaunch = new IndexLaunch();
    indexLaunch.setLaunchId(launch.getId());
    indexLaunch.setAnalyzerConfig(analyzerConfig);

    when(testItemRepository.findAllById(anyList())).thenReturn(items);
    when(launchPreparerService.prepare(any(Launch.class), anyList(), any(AnalyzerConfig.class)))
        .thenReturn(Optional.of(indexLaunch));

    issuesAnalyzer.runAnalyzers(launch,
        items.stream().map(TestItem::getItemId).collect(Collectors.toList()), analyzerConfig);

    verify(analyzerServiceClient, times(1)).analyze(any());
  }

  private AnalyzerConfig analyzerConfig() {
    AnalyzerConfig analyzerConfig = new AnalyzerConfig();
    analyzerConfig.setAnalyzerMode(ALL_LAUNCHES.getValue());
    return analyzerConfig;
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
}
