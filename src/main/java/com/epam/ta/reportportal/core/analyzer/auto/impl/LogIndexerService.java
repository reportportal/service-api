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

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.analyzer.auto.indexer.BatchLogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.indexer.IndexerStatusCache;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.reportportal.model.project.AnalyzerConfig;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class LogIndexerService implements LogIndexer {

  private static Logger LOGGER = LoggerFactory.getLogger(LogIndexerService.class);

  private final BatchLogIndexer batchLogIndexer;

  private final TaskExecutor taskExecutor;

  private final LaunchRepository launchRepository;

  private final TestItemRepository testItemRepository;

  private final IndexerServiceClient indexerServiceClient;

  private final LaunchPreparerService launchPreparerService;

  private final IndexerStatusCache indexerStatusCache;

  @Autowired
  public LogIndexerService(BatchLogIndexer batchLogIndexer,
      @Qualifier("logIndexTaskExecutor") TaskExecutor taskExecutor,
      LaunchRepository launchRepository, TestItemRepository testItemRepository,
      IndexerServiceClient indexerServiceClient,
      LaunchPreparerService launchPreparerService, IndexerStatusCache indexerStatusCache) {
    this.batchLogIndexer = batchLogIndexer;
    this.taskExecutor = taskExecutor;
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.indexerServiceClient = indexerServiceClient;
    this.launchPreparerService = launchPreparerService;
    this.indexerStatusCache = indexerStatusCache;
  }

  @Override
  public CompletableFuture<Long> index(Long projectId, AnalyzerConfig analyzerConfig) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LOGGER.info("Start indexing for project: {}", projectId);
        indexerStatusCache.indexingStarted(projectId);
        final Long indexed = batchLogIndexer.index(projectId, analyzerConfig);
        LOGGER.info("Indexing finished for project: {}. Logs indexed: {}", projectId, indexed);
        return indexed;
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        throw new ReportPortalException(e.getMessage());
      } finally {
        indexerStatusCache.indexingFinished(projectId);
      }
    }, taskExecutor);
  }

  @Override
  @Transactional(readOnly = true)
  public Long indexLaunchLogs(Launch launch, AnalyzerConfig analyzerConfig) {
    try {
      indexerStatusCache.indexingStarted(launch.getProjectId());
      final List<Long> itemIds = testItemRepository.selectIdsWithIssueByLaunch(launch.getId());
      return batchLogIndexer.index(analyzerConfig, launch, itemIds);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new ReportPortalException(e.getMessage());
    } finally {
      indexerStatusCache.indexingFinished(launch.getProjectId());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Long indexItemsLogs(Long projectId, Long launchId, List<Long> itemIds,
      AnalyzerConfig analyzerConfig) {
    try {
      indexerStatusCache.indexingStarted(projectId);
      Launch launch = launchRepository.findById(launchId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
      return batchLogIndexer.index(analyzerConfig, launch, itemIds);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new ReportPortalException(e.getMessage());
    } finally {
      indexerStatusCache.indexingFinished(projectId);
    }
  }

  @Override
  public void deleteIndex(Long project) {
    indexerServiceClient.deleteIndex(project);
  }

  @Override
  public CompletableFuture<Long> cleanIndex(Long index, List<Long> ids) {
    return CollectionUtils.isEmpty(ids) ?
        CompletableFuture.completedFuture(0L) :
        CompletableFuture.supplyAsync(() -> indexerServiceClient.cleanIndex(index, ids));
  }

  @Async
  @Override
  public void indexDefectsUpdate(Long projectId, AnalyzerConfig analyzerConfig,
      List<TestItem> testItems) {
    if (CollectionUtils.isEmpty(testItems)) {
      return;
    }

    Map<Long, String> itemsForIndexUpdate = testItems.stream()
        .collect(Collectors.toMap(TestItem::getItemId,
            it -> it.getItemResults().getIssue().getIssueType().getLocator()));

    List<Long> missedItemIds = indexerServiceClient.indexDefectsUpdate(projectId,
        itemsForIndexUpdate);
    List<TestItem> missedItems = testItems.stream()
        .filter(it -> missedItemIds.contains(it.getItemId())).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(missedItems)) {
      List<IndexLaunch> indexLaunchList = launchPreparerService.prepare(analyzerConfig,
          missedItems);
      indexerServiceClient.index(indexLaunchList);
    }
  }

  @Override
  public int indexItemsRemove(Long projectId, Collection<Long> itemsForIndexRemove) {
    return indexerServiceClient.indexItemsRemove(projectId, itemsForIndexRemove);
  }

  @Async
  @Override
  public void indexItemsRemoveAsync(Long projectId, Collection<Long> itemsForIndexRemove) {
    indexerServiceClient.indexItemsRemoveAsync(projectId, itemsForIndexRemove);
  }

  @Async
  @Override
  public void indexLaunchesRemove(Long projectId, Collection<Long> launchesForIndexRemove) {
    indexerServiceClient.indexLaunchesRemove(projectId, launchesForIndexRemove);
  }

}
