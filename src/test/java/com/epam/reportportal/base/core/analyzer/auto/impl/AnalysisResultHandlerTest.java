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

package com.epam.reportportal.base.core.analyzer.auto.impl;

import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.reportportal.base.core.analytics.DefectUpdateStatisticsService;
import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.events.domain.ItemIssueTypeDefinedEvent;
import com.epam.reportportal.base.core.item.impl.IssueTypeHandler;
import com.epam.reportportal.base.core.project.ProjectService;
import com.epam.reportportal.base.core.statistics.TestItemStatisticsService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.model.analyzer.AnalyzedItemRs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author Pavel Bortnik
 */
class AnalysisResultHandlerTest {

  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private final LaunchRepository launchRepository = mock(LaunchRepository.class);

  private final ProjectService projectService = mock(ProjectService.class);

  private final IssueTypeHandler issueTypeHandler = mock(IssueTypeHandler.class);

  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

  private final TestItemStatisticsService testItemStatisticsService =
      mock(TestItemStatisticsService.class);

  private final DefectUpdateStatisticsService defectUpdateStatisticsService =
      mock(DefectUpdateStatisticsService.class);

  private final LogIndexer logIndexer = mock(LogIndexer.class);

  private final AnalysisResultHandler analysisResultHandler =
      new AnalysisResultHandler(testItemRepository, launchRepository, projectService,
          issueTypeHandler, eventPublisher, testItemStatisticsService,
          defectUpdateStatisticsService, logIndexer);

  @Test
  void processResultsOverwritesIssuesWritesStatsAndIndexes() {
    int itemsCount = 2;
    Long launchId = 1L;
    Long projectId = 1L;

    List<TestItem> items = testItemsTI(itemsCount, launchId);
    List<AnalyzedItemRs> analyzed = analyzedItems(itemsCount);

    when(testItemRepository.findAllById(any())).thenReturn(items);
    when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch(launchId, projectId)));
    when(projectService.findProjectById(projectId)).thenReturn(project(projectId));
    when(issueTypeHandler.defineIssueType(eq(projectId), eq(PRODUCT_BUG.getLocator())))
        .thenReturn(productBug().getIssueType());

    analysisResultHandler.processResults(analyzed, "test-analyzer");

    verify(testItemRepository, times(itemsCount)).save(any());
    verify(issueTypeHandler, times(itemsCount)).defineIssueType(eq(projectId),
        eq(PRODUCT_BUG.getLocator()));
    verify(defectUpdateStatisticsService, times(1))
        .saveAutoAnalyzedDefectStatistics(itemsCount, itemsCount, 0, projectId);
    verify(logIndexer, times(1)).indexItemsLogs(eq(projectId), eq(launchId), anyList(), any());
    verify(eventPublisher, times(itemsCount))
        .publishEvent(any(ItemIssueTypeDefinedEvent.class));
  }

  @Test
  void processResultsIgnoresEmptyBatch() {
    analysisResultHandler.processResults(Collections.emptyList(), "test-analyzer");

    verify(testItemRepository, never()).save(any());
    verify(logIndexer, never()).indexItemsLogs(any(), any(), anyList(), any());
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
