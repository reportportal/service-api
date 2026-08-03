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

package com.epam.reportportal.base.core.analyzer.auto.impl;

import static com.epam.reportportal.base.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.epam.reportportal.base.core.analytics.DefectUpdateStatisticsService;
import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.events.domain.ItemIssueTypeDefinedEvent;
import com.epam.reportportal.base.core.events.domain.LinkTicketEvent;
import com.epam.reportportal.base.core.item.impl.IssueTypeHandler;
import com.epam.reportportal.base.core.project.ProjectService;
import com.epam.reportportal.base.core.statistics.TestItemStatisticsService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.activity.TestItemActivityResource;
import com.epam.reportportal.base.model.analyzer.AnalyzedItemRs;
import com.epam.reportportal.base.model.analyzer.RelevantItemInfo;
import com.epam.reportportal.base.ws.converter.builders.IssueEntityBuilder;
import com.google.common.collect.Sets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Handles {@link AnalyzedItemRs} results delivered asynchronously through the analyzer reply queue. Every result is
 * treated as the actual one and overwrites the item's issue (no priority, last-write-wins). For every processed
 * message it also records auto-analysis statistics and triggers log indexing of the affected items.
 *
 * @author Pavel Bortnik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisResultHandler {

  private static final String DEFAULT_ANALYZER_NAME = "analyzer";

  private final TestItemRepository testItemRepository;

  private final LaunchRepository launchRepository;

  private final ProjectService projectService;

  private final IssueTypeHandler issueTypeHandler;

  private final ApplicationEventPublisher eventPublisher;

  private final TestItemStatisticsService testItemStatisticsService;

  private final DefectUpdateStatisticsService defectUpdateStatisticsService;

  private final LogIndexer logIndexer;

  /**
   * Applies analysis results that arrived in a single reply message. A message always belongs to a single launch and
   * project, so the launch/project context is resolved once and reused for the whole batch.
   *
   * @param analyzed         results from the reply queue
   * @param analyzerInstance analyzer instance name (from the message header), may be {@code null}
   */
  @Transactional
  public void processResults(List<AnalyzedItemRs> analyzed, String analyzerInstance) {
    if (CollectionUtils.isEmpty(analyzed)) {
      return;
    }
    var instance =
        StringUtils.isBlank(analyzerInstance) ? DEFAULT_ANALYZER_NAME : analyzerInstance;

    var analyzedResultMap = analyzed.stream()
        .filter(it -> Objects.nonNull(it.getItemId()))
        .collect(
            toMap(AnalyzedItemRs::getItemId, identity(), (existing, replacement) -> replacement,
                LinkedHashMap::new));

    var itemsById = testItemRepository.findAllById(analyzedResultMap.keySet()).stream()
        .collect(toMap(TestItem::getItemId, identity()));

    Map<Long, TestItem> resolvedItemsById = new LinkedHashMap<>();
    Map<Long, AnalyzedItemRs> resolvedAnalyzedResultMap = new LinkedHashMap<>();

    analyzedResultMap.forEach((itemId, analyzedItemResult) -> {
      TestItem testItem = itemsById.get(itemId);
      if (testItem != null) {
        TestItem resolvedItem = resolveRetryItem(testItem);
        resolvedItemsById.put(resolvedItem.getItemId(), resolvedItem);
        resolvedAnalyzedResultMap.put(resolvedItem.getItemId(), analyzedItemResult);
      }
    });

    resolvedItemsById.values().stream()
        .filter(it -> Objects.nonNull(it.getLaunchId()))
        .collect(groupingBy(TestItem::getLaunchId))
        .forEach((launchId, launchItems) ->
            processLaunchResults(launchId, launchItems, resolvedAnalyzedResultMap, instance));
  }

  private TestItem resolveRetryItem(TestItem testItem) {
    if (testItem.getRetryOf() == null) {
      return testItem;
    }

    log.info("Analyzed item is retry {}, replacing with original {} for update",
        testItem.getItemId(), testItem.getRetryOf());
    return testItemRepository.findById(testItem.getRetryOf())
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND));
  }

  private void processLaunchResults(Long launchId, List<TestItem> testItems,
      Map<Long, AnalyzedItemRs> analyzedResultMap, String analyzerInstance) {

    Launch launch = launchRepository.findById(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));

    Long projectId = launch.getProjectId();
    Project project = projectService.findProjectById(projectId);

    int analyzedAmount = 0;

    for (TestItem testItem : testItems) {
      AnalyzedItemRs analyzedItemResult = analyzedResultMap.get(testItem.getItemId());
      if (analyzedItemResult != null && applyAnalysisResult(testItem, analyzedItemResult, project,
          analyzerInstance)) {
        analyzedAmount++;
      }
    }

    List<Long> indexItemIds = testItems.stream().map(TestItem::getItemId).collect(toList());

    defectUpdateStatisticsService.saveAutoAnalyzedDefectStatistics(testItems.size(),
        analyzedAmount, 0, projectId);

    logIndexer.indexItemsLogs(projectId, launchId, indexItemIds,
        AnalyzerUtils.getAnalyzerConfig(project));
  }

  /**
   * Overwrites the issue of a single test item with the analyzed result.
   *
   * @return {@code true} if the item's issue was changed
   */
  private boolean applyAnalysisResult(TestItem testItem, AnalyzedItemRs analyzed, Project project,
      String analyzerInstance) {

    log.info("Analysis has found a match: {}", analyzed);

    Long projectId = project.getId();
    IssueType beforeIssue = testItem.getItemResults().getIssue().getIssueType();

    TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);
    RelevantItemInfo relevantItemInfo = updateTestItemIssue(projectId, analyzed, testItem);
    TestItemActivityResource after = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);

    testItemStatisticsService.changeDefectStatistics(testItem, beforeIssue,
        testItem.getItemResults().getIssue().getIssueType());
    testItemRepository.save(testItem);

    eventPublisher.publishEvent(
        new ItemIssueTypeDefinedEvent(before, after, analyzerInstance, relevantItemInfo,
            project.getOrganizationId()));

    ofNullable(after.getTickets()).ifPresent(
        it -> eventPublisher.publishEvent(
            new LinkTicketEvent(before, after, analyzerInstance, project.getOrganizationId())));
    return true;
  }

  /**
   * Updates issue for a specified test item.
   *
   * @param projectId Project id
   * @param rs        Response from an analyzer
   * @param testItem  Test item to be updated
   * @return Updated issue entity
   */
  private RelevantItemInfo updateTestItemIssue(Long projectId, AnalyzedItemRs rs,
      TestItem testItem) {
    IssueType issueType = issueTypeHandler.defineIssueType(projectId, rs.getLocator());
    IssueEntity issueEntity = new IssueEntityBuilder(
        testItem.getItemResults().getIssue()).addIssueType(issueType)
        .addIgnoreFlag(testItem.getItemResults().getIssue().getIgnoreAnalyzer())
        .addAutoAnalyzedFlag(true)
        .get();
    issueEntity.setIssueId(testItem.getItemId());
    issueEntity.setTestItemResults(testItem.getItemResults());
    testItem.getItemResults().setIssue(issueEntity);

    RelevantItemInfo relevantItemInfo = null;
    if (rs.getRelevantItemId() != null) {
      Optional<TestItem> relevantItemOptional = testItemRepository.findById(rs.getRelevantItemId());
      if (relevantItemOptional.isPresent()) {
        if (relevantItemOptional.get().getRetryOf() != null) {
          relevantItemOptional = testItemRepository.findById(
              relevantItemOptional.get().getRetryOf());
        }
        relevantItemInfo = updateIssueFromRelevantItem(issueEntity, relevantItemOptional.get());
      } else {
        log.error(ErrorType.TEST_ITEM_NOT_FOUND.getDescription(), rs.getRelevantItemId());
      }
    }

    return relevantItemInfo;
  }

  /**
   * Updates issue with values are taken from most relevant item.
   *
   * @param issue        Issue to update
   * @param relevantItem Relevant item
   */
  private RelevantItemInfo updateIssueFromRelevantItem(IssueEntity issue, TestItem relevantItem) {
    ofNullable(relevantItem.getItemResults().getIssue()).ifPresent(relevantIssue -> {
      issue.setIssueDescription(relevantIssue.getIssueDescription());
      issue.setTickets(Sets.newHashSet(relevantIssue.getTickets()));
    });

    return AnalyzerUtils.TO_RELEVANT_ITEM_INFO.apply(relevantItem);
  }
}
