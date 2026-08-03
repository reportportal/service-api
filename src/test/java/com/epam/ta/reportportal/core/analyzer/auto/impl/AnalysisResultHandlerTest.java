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

import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.ta.reportportal.core.analytics.DefectUpdateStatisticsService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ItemIssueTypeDefinedEvent;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.core.statistics.TestItemStatisticsService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.model.analyzer.AnalyzedItemRs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bortnik
 */
class AnalysisResultHandlerTest {

  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private final LaunchRepository launchRepository = mock(LaunchRepository.class);

  private final ProjectRepository projectRepository = mock(ProjectRepository.class);

  private final IssueTypeHandler issueTypeHandler = mock(IssueTypeHandler.class);

  private final MessageBus messageBus = mock(MessageBus.class);

  private final TestItemStatisticsService testItemStatisticsService =
      mock(TestItemStatisticsService.class);

  private final DefectUpdateStatisticsService defectUpdateStatisticsService =
      mock(DefectUpdateStatisticsService.class);

  private final LogIndexer logIndexer = mock(LogIndexer.class);

  private final AnalysisResultHandler analysisResultHandler =
      new AnalysisResultHandler(testItemRepository, launchRepository, projectRepository,
          issueTypeHandler, messageBus, testItemStatisticsService, defectUpdateStatisticsService,
          logIndexer);

  @Test
  void processResultsOverwritesIssuesWritesStatsAndIndexes() {
    int itemsCount = 2;
    Long launchId = 1L;
    Long projectId = 1L;

    List<TestItem> items = testItemsTI(itemsCount, launchId);
    List<AnalyzedItemRs> analyzed = analyzedItems(itemsCount);

    when(testItemRepository.findAllById(any())).thenReturn(items);
    when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch(launchId, projectId)));
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project(projectId)));
    when(issueTypeHandler.defineIssueType(eq(projectId), eq(PRODUCT_BUG.getLocator())))
        .thenReturn(productBug().getIssueType());
    analysisResultHandler.processResults(analyzed, "test-analyzer");

    verify(testItemRepository, times(itemsCount)).save(any());
    verify(issueTypeHandler, times(itemsCount)).defineIssueType(eq(projectId),
        eq(PRODUCT_BUG.getLocator()));
    verify(defectUpdateStatisticsService, times(1))
        .saveAutoAnalyzedDefectStatistics(itemsCount, itemsCount, 0, projectId);
    verify(logIndexer, times(1)).indexDefectsUpdate(eq(projectId), any(), anyList());
    verify(messageBus, times(itemsCount))
        .publishActivity(any(ItemIssueTypeDefinedEvent.class));
  }

  @Test
  void processResultsResolvesRetryItemBeforeLaunchFiltering() {
    Long launchId = 1L;
    Long projectId = 1L;
    Long originalItemId = 1L;
    Long retryItemId = 99L;

    TestItem originalItem = testItemsTI(1, launchId).get(0);
    TestItem retryItem = new TestItem();
    retryItem.setItemId(retryItemId);
    retryItem.setRetryOf(originalItemId);

    AnalyzedItemRs analyzedRetryItem = new AnalyzedItemRs();
    analyzedRetryItem.setItemId(retryItemId);
    analyzedRetryItem.setLocator(PRODUCT_BUG.getLocator());

    when(testItemRepository.findAllById(any())).thenReturn(List.of(retryItem));
    when(testItemRepository.findById(originalItemId)).thenReturn(Optional.of(originalItem));
    when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch(launchId, projectId)));
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project(projectId)));
    when(issueTypeHandler.defineIssueType(eq(projectId), eq(PRODUCT_BUG.getLocator())))
        .thenReturn(productBug().getIssueType());
    analysisResultHandler.processResults(List.of(analyzedRetryItem), "test-analyzer");

    verify(testItemRepository).save(originalItem);
    verify(defectUpdateStatisticsService)
        .saveAutoAnalyzedDefectStatistics(1, 1, 0, projectId);
    verify(logIndexer).indexDefectsUpdate(eq(projectId), any(), anyList());
  }

  @Test
  void processResultsIgnoresEmptyBatch() {
    analysisResultHandler.processResults(Collections.emptyList(), "test-analyzer");

    verify(testItemRepository, never()).save(any());
    verify(logIndexer, never()).indexDefectsUpdate(any(), any(), anyList());
    verify(defectUpdateStatisticsService, never())
        .saveAutoAnalyzedDefectStatistics(anyInt(), anyInt(), anyInt(), any());
  }

  private Launch launch(Long launchId, Long projectId) {
    Launch launch = new Launch();
    launch.setId(launchId);
    launch.setName("launch");
    launch.setProjectId(projectId);
    return launch;
  }

  private Project project(Long projectId) {
    Project project = new Project();
    project.setId(projectId);
    return project;
  }

  private List<TestItem> testItemsTI(int count, Long launchId) {
    List<TestItem> list = new ArrayList<>(count);
    for (int i = 1; i <= count; i++) {
      TestItem test = new TestItem();
      test.setItemId((long) i);
      test.setName("test" + i);
      test.setLaunchId(launchId);
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
    issueEntity.setIgnoreAnalyzer(false);
    return issueEntity;
  }

  private IssueEntity productBug() {
    IssueType issueType = new IssueType();
    issueType.setLocator(PRODUCT_BUG.getLocator());
    IssueEntity issueEntity = new IssueEntity();
    issueEntity.setIssueType(issueType);
    return issueEntity;
  }

  private List<AnalyzedItemRs> analyzedItems(int itemsCount) {
    List<AnalyzedItemRs> list = new ArrayList<>();
    for (int i = 1; i <= itemsCount; i++) {
      AnalyzedItemRs testItem = new AnalyzedItemRs();
      testItem.setItemId((long) i);
      testItem.setLocator(PRODUCT_BUG.getLocator());
      list.add(testItem);
    }
    return list;
  }
}
