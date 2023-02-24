package com.epam.ta.reportportal.core.analyzer.auto.indexer;

import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JLaunchModeEnum;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class BatchLogIndexer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchLogIndexer.class);

	private final Integer launchBatchSize;
	private final Integer itemsBatchSize;
	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final LaunchPreparerService launchPreparerService;
	private final IndexerServiceClient indexerServiceClient;

	@Autowired
	public BatchLogIndexer(@Value("${rp.environment.variable.log-index.batch-size}") Integer launchBatchSize,
			@Value("${rp.environment.variable.item-analyze.batch-size}") Integer itemsBatchSize, LaunchRepository launchRepository,
			TestItemRepository testItemRepository, LaunchPreparerService launchPreparerService, IndexerServiceClient indexerServiceClient) {
		this.launchBatchSize = launchBatchSize;
		this.itemsBatchSize = itemsBatchSize;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
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

	@Transactional(readOnly = true)
	public Long index(AnalyzerConfig analyzerConfig, Launch launch, List<Long> itemIds) {
		AtomicLong indexedCount = new AtomicLong(0);
		Iterables.partition(itemIds, itemsBatchSize)
				.forEach(partition -> indexedCount.addAndGet(indexPartition(partition, analyzerConfig, launch)));
		return indexedCount.get();
	}

	private Long indexPartition(List<Long> itemIds, AnalyzerConfig analyzerConfig, Launch launch) {
		LOGGER.info("Indexing started for {} items.", itemIds.size());
		final Long indexedLogs = launchPreparerService.prepare(launch, testItemRepository.findAllById(itemIds), analyzerConfig)
				.map(it -> indexerServiceClient.index(Lists.newArrayList(it)))
				.orElse(0L);
		LOGGER.info("Indexing of {} logs is finished for {} items.", indexedLogs, itemIds.size());
		return indexedLogs;
	}

	private void index(Long projectId, AnalyzerConfig analyzerConfig, List<Long> launchIds, AtomicLong totalIndexed) {
		if (launchIds.isEmpty()) {
			return;
		}
		LOGGER.debug("Project {}. Found {} ids", projectId, launchIds.size());
		final List<Long> filteredIds = filterIds(launchIds);
		if (filteredIds.isEmpty()) {
			return;
		}
		LOGGER.debug("Project {}. Found {} filtered ids", projectId, filteredIds.size());
		final List<IndexLaunch> preparedLaunches = launchPreparerService.prepare(launchIds, analyzerConfig);
		if (preparedLaunches.isEmpty()) {
			return;
		}

		LOGGER.debug("Project {}. Start indexing for {} launches", projectId, preparedLaunches.size());
		final long indexed = indexByPartition(preparedLaunches);
		LOGGER.debug("Project {}. Indexed {} logs", projectId, indexed);
		totalIndexed.addAndGet(indexed);
	}

	private long indexByPartition(List<IndexLaunch> preparedLaunches) {
		return preparedLaunches.stream().map(indexLaunch -> {
			final Iterable<List<IndexTestItem>> lists = Iterables.partition(indexLaunch.getTestItems(), itemsBatchSize);
			return StreamSupport.stream(lists.spliterator(), false).map(partition -> {
				indexLaunch.setTestItems(partition);
				final Long indexed = indexerServiceClient.index(Lists.newArrayList(indexLaunch));
				return indexed;
			}).mapToLong(Long::longValue).sum();
		}).mapToLong(Long::longValue).sum();
	}

	private List<Long> filterIds(List<Long> launchIds) {
		return launchIds.stream()
				.filter(id -> launchRepository.hasItemsWithLogsWithLogLevel(id, List.of(JTestItemTypeEnum.STEP), LogLevel.ERROR_INT))
				.collect(Collectors.toList());
	}

	private List<Long> getLaunchIds(Long projectId) {
		return launchRepository.findIdsByProjectIdAndModeAndStatusNotEq(projectId,
				JLaunchModeEnum.DEFAULT,
				JStatusEnum.IN_PROGRESS,
				launchBatchSize
		);
	}

	private List<Long> getLaunchIds(Long projectId, Long launchId) {
		return launchRepository.findIdsByProjectIdAndModeAndStatusNotEqAfterId(projectId,
				JLaunchModeEnum.DEFAULT,
				JStatusEnum.IN_PROGRESS,
				launchId,
				launchBatchSize
		);
	}

}
