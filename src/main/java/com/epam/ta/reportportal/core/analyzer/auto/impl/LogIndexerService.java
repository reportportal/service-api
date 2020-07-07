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
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class LogIndexerService implements LogIndexer {
	private static Logger LOGGER = LoggerFactory.getLogger(LogIndexerService.class);

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final IndexerServiceClient indexerServiceClient;

	private final LaunchPreparerService launchPreparerService;

	private final IndexerStatusCache indexerStatusCache;

	@Autowired
	public LogIndexerService(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			IndexerServiceClient indexerServiceClient, LaunchPreparerService launchPreparerService, IndexerStatusCache indexerStatusCache) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.indexerServiceClient = indexerServiceClient;
		this.launchPreparerService = launchPreparerService;
		this.indexerStatusCache = indexerStatusCache;
	}

	@Override
	@Transactional(readOnly = true)
	public CompletableFuture<Long> indexLaunchesLogs(Long projectId, List<Long> launchIds, AnalyzerConfig analyzerConfig) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				indexerStatusCache.indexingStarted(projectId);
				List<IndexLaunch> indexLaunches = prepareLaunches(launchRepository.findAllById(launchIds), analyzerConfig);
				LOGGER.info("Start indexing for {} launches", indexLaunches.size());
				Long indexed = indexerServiceClient.index(indexLaunches);
				LOGGER.info("Indexed {} logs", indexed);
				return indexed;
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

	/**
	 * Prepare launches for indexing
	 *
	 * @param launches       - Launches to be prepared
	 * @param analyzerConfig - Analyzer config
	 * @return List of prepared launches for indexing
	 */
	private List<IndexLaunch> prepareLaunches(List<Launch> launches, AnalyzerConfig analyzerConfig) {
		return launches.stream()
				.filter(it -> testItemRepository.hasItemsWithIssueByLaunch(it.getId()))
				.map(it -> launchPreparerService.prepare(it, testItemRepository.findTestItemsByLaunchId(it.getId()), analyzerConfig))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}
}
