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
import com.epam.ta.reportportal.core.analyzer.auto.indexer.IndexerStatusCache;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverContent;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class LogIndexerService implements LogIndexer {
	private static Logger LOGGER = LoggerFactory.getLogger(LogIndexerService.class);

	private final Integer launchBatchSize;

	private final TaskExecutor taskExecutor;

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final IndexerServiceClient indexerServiceClient;

	private final LaunchPreparerService launchPreparerService;

	private final IndexerStatusCache indexerStatusCache;

	@Autowired
	public LogIndexerService(@Value("${rp.environment.variable.log-index.batch-size}") Integer launchBatchSize,
			@Qualifier("logIndexTaskExecutor") TaskExecutor taskExecutor, LaunchRepository launchRepository,
			TestItemRepository testItemRepository, IndexerServiceClient indexerServiceClient, LaunchPreparerService launchPreparerService,
			IndexerStatusCache indexerStatusCache) {
		this.launchBatchSize = launchBatchSize;
		this.taskExecutor = taskExecutor;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.indexerServiceClient = indexerServiceClient;
		this.launchPreparerService = launchPreparerService;
		this.indexerStatusCache = indexerStatusCache;
	}

	@Override
	//TODO refactor to execute in single Transaction (because of CompletableFuture there is no transaction inside).
	//TODO Probably we should implement AsyncLogIndexer and use this service as sync delegate with transaction
	public CompletableFuture<Long> index(Long projectId, AnalyzerConfig analyzerConfig) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				indexerStatusCache.indexingStarted(projectId);
				final AtomicLong indexed = new AtomicLong(0L);
				iterateOverContent(launchBatchSize,
						pageable -> launchRepository.findIndexLaunchByProjectId(projectId, pageable.getPageSize(), pageable.getOffset()),
						indexLaunches -> {
							LOGGER.debug("Start indexing for {} launches", indexLaunches.size());
							final List<IndexLaunch> preparedLaunches = prepareLaunches(analyzerConfig, indexLaunches);
							indexed.addAndGet(indexerServiceClient.index(preparedLaunches));
							LOGGER.debug("Indexed {} logs", indexed);
						}
				);
				return indexed.get();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new ReportPortalException(e.getMessage());
			} finally {
				indexerStatusCache.indexingFinished(projectId);
			}
		}, taskExecutor);
	}

	/**
	 * Prepare launches for indexing
	 *
	 * @param analyzerConfig - Analyzer config
	 * @param indexLaunches  - Launches to be prepared
	 * @return List of prepared launches for indexing
	 */
	private List<IndexLaunch> prepareLaunches(AnalyzerConfig analyzerConfig, List<IndexLaunch> indexLaunches) {
		return indexLaunches.stream()
				.peek(l -> {
					final List<IndexTestItem> indexTestItemList = testItemRepository.findIndexTestItemByLaunchId(l.getLaunchId(),
							List.of(JTestItemTypeEnum.STEP)
					);
					if (!indexTestItemList.isEmpty()) {
						l.setTestItems(launchPreparerService.prepare(l.getLaunchId(), indexTestItemList));
					}
				})
				.filter(l -> !CollectionUtils.isEmpty(l.getTestItems()))
				.peek(l -> l.setAnalyzerConfig(analyzerConfig))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	//TODO same refactoring as for the method above
	public CompletableFuture<Long> indexLaunchLogs(Long projectId, Long launchId, AnalyzerConfig analyzerConfig) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				indexerStatusCache.indexingStarted(projectId);
				Launch launch = launchRepository.findById(launchId)
						.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
				Optional<IndexLaunch> indexLaunch = launchPreparerService.prepare(launch,
						testItemRepository.findTestItemsByLaunchId(launch.getId()),
						analyzerConfig
				);
				return indexLaunch.map(it -> {
					LOGGER.info("Start indexing for {} launches", 1);
					Long indexed = indexerServiceClient.index(Lists.newArrayList(it));
					LOGGER.info("Indexed {} logs", indexed);
					return indexed;
				}).orElse(0L);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new ReportPortalException(e.getMessage());
			} finally {
				indexerStatusCache.indexingFinished(projectId);
			}
		});
	}

	@Override
	@Transactional(readOnly = true)
	//TODO same refactoring as for the method above
	public Long indexItemsLogs(Long projectId, Long launchId, List<Long> itemIds, AnalyzerConfig analyzerConfig) {
		try {
			indexerStatusCache.indexingStarted(projectId);
			Launch launch = launchRepository.findById(launchId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
			return launchPreparerService.prepare(launch, testItemRepository.findAllById(itemIds), analyzerConfig)
					.map(it -> indexerServiceClient.index(Lists.newArrayList(it)))
					.orElse(0L);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ReportPortalException(e.getMessage());
		} finally {
			indexerStatusCache.indexingFinished(projectId);
		}
	}

	@Override
	public CompletableFuture<Long> indexPreparedLogs(Long projectId, IndexLaunch indexLaunch) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				indexerStatusCache.indexingStarted(projectId);
				return indexerServiceClient.index(Lists.newArrayList(indexLaunch));
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
				throw new ReportPortalException(ex.getMessage());
			} finally {
				indexerStatusCache.indexingFinished(projectId);
			}
		});
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
	public void indexDefectsUpdate(Long projectId, AnalyzerConfig analyzerConfig, List<TestItem> testItems) {
		if (CollectionUtils.isEmpty(testItems)) {
			return;
		}

		Map<Long, String> itemsForIndexUpdate = testItems.stream()
				.collect(Collectors.toMap(TestItem::getItemId, it -> it.getItemResults().getIssue().getIssueType().getLocator()));

		List<Long> missedItemIds = indexerServiceClient.indexDefectsUpdate(projectId, itemsForIndexUpdate);
		List<TestItem> missedItems = testItems.stream().filter(it -> missedItemIds.contains(it.getItemId())).collect(Collectors.toList());

		List<IndexLaunch> indexLaunchList = launchPreparerService.prepareLaunches(analyzerConfig, missedItems);

		indexerServiceClient.index(indexLaunchList);
	}

	@Async
	@Override
	public void indexItemsRemove(Long projectId, List<Long> itemsForIndexRemove) {
		indexerServiceClient.indexItemsRemove(projectId, itemsForIndexRemove);
	}

}
