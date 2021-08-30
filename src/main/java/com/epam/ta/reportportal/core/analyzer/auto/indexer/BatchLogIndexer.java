package com.epam.ta.reportportal.core.analyzer.auto.indexer;

import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.LaunchPreparerService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.jooq.enums.JLaunchModeEnum;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Iterables;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class BatchLogIndexer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchLogIndexer.class);

	private final Integer launchBatchSize;
	private final LaunchRepository launchRepository;
	private final LaunchPreparerService launchPreparerService;
	private final IndexerServiceClient indexerServiceClient;

	@Autowired
	public BatchLogIndexer(@Value("${rp.environment.variable.log-index.batch-size}") Integer launchBatchSize,
			LaunchRepository launchRepository, LaunchPreparerService launchPreparerService, IndexerServiceClient indexerServiceClient) {
		this.launchBatchSize = launchBatchSize;
		this.launchRepository = launchRepository;
		this.launchPreparerService = launchPreparerService;
		this.indexerServiceClient = indexerServiceClient;
	}

	@Transactional(readOnly = true)
	public Long index(Long projectId, AnalyzerConfig analyzerConfig) {
		final AtomicLong totalIndexed = new AtomicLong(0L);

		List<Long> ids = getLaunchIds(projectId);
		index(projectId, analyzerConfig, ids, totalIndexed);

		while (launchBatchSize == ids.size()) {
			final Long lastLaunchId = Iterables.getLast(ids);
			ids = getLaunchIds(projectId, lastLaunchId);
			index(projectId, analyzerConfig, ids, totalIndexed);
		}

		return totalIndexed.get();
	}

	private void index(Long projectId, AnalyzerConfig analyzerConfig, List<Long> launchIds, AtomicLong totalIndexed) {
		if (launchIds.isEmpty()) {
			return;
		}
		LOGGER.debug("Project {}. Found {} ids", projectId, launchIds.size());
		final List<IndexLaunch> launchesForIndexing = launchRepository.findIndexLaunchByIdsAndLogLevel(launchIds, LogLevel.ERROR.toInt());
		final List<IndexLaunch> preparedLaunches = prepareLaunches(analyzerConfig, launchesForIndexing);
		LOGGER.debug("Project {}. Start indexing for {} launches", projectId, preparedLaunches.size());
		final Long indexed = indexerServiceClient.index(preparedLaunches);
		LOGGER.debug("Project {}. Indexed {} logs", projectId, indexed);
		totalIndexed.addAndGet(indexed);
	}

	private List<Long> getLaunchIds(Long projectId) {
		return launchRepository.findIdsByProjectIdAndModeAndStatusNotEq(projectId,
				JLaunchModeEnum.DEFAULT,
				JStatusEnum.PASSED,
				launchBatchSize
		);
	}

	private List<Long> getLaunchIds(Long projectId, Long launchId) {
		return launchRepository.findIdsByProjectIdAndModeAndStatusNotEqAfterId(projectId,
				JLaunchModeEnum.DEFAULT,
				JStatusEnum.PASSED,
				launchId,
				launchBatchSize
		);
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
				.peek(launchPreparerService::fillLaunch)
				.filter(l -> CollectionUtils.isNotEmpty(l.getTestItems()))
				.peek(l -> l.setAnalyzerConfig(analyzerConfig))
				.collect(Collectors.toList());
	}
}
