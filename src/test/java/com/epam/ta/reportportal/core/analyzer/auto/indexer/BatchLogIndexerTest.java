package com.epam.ta.reportportal.core.analyzer.auto.indexer;

import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.jooq.enums.JLaunchModeEnum;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.epam.ta.reportportal.entity.AnalyzeMode.ALL_LAUNCHES;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BatchLogIndexerTest {

	private Integer batchSize = 2;

	private IndexerServiceClient indexerServiceClient = mock(IndexerServiceClient.class);
	private LaunchRepository launchRepository = mock(LaunchRepository.class);
	private TestItemRepository testItemRepository = mock(TestItemRepository.class);
	private LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);

	private final BatchLogIndexer batchLogIndexer = new BatchLogIndexer(batchSize,
			batchSize,
			launchRepository,
			testItemRepository,
			launchPreparerService,
			indexerServiceClient
	);

	@Test
	void index() {

		final List<Long> ids = List.of(1L, 2L);
		when(launchRepository.findIdsByProjectIdAndModeAndStatusNotEq(eq(1L), any(JLaunchModeEnum.class), any(JStatusEnum.class), anyInt()))
				.thenReturn(ids);

		final IndexLaunch firstIndex = new IndexLaunch();
		firstIndex.setTestItems(List.of(new IndexTestItem()));
		final IndexLaunch secondIndex = new IndexLaunch();
		secondIndex.setTestItems(List.of(new IndexTestItem()));
		when(launchPreparerService.prepare(eq(ids), any(AnalyzerConfig.class))).thenReturn(List.of(firstIndex, secondIndex));

		final List<Long> secondPortionIds = List.of(3L);
		when(launchRepository.findIdsByProjectIdAndModeAndStatusNotEqAfterId(eq(1L),
				any(JLaunchModeEnum.class),
				any(JStatusEnum.class),
				eq(2L),
				anyInt()
		)).thenReturn(secondPortionIds);

		final IndexLaunch thirdIndex = new IndexLaunch();
		thirdIndex.setTestItems(List.of(new IndexTestItem()));
		when(launchPreparerService.prepare(eq(secondPortionIds), any(AnalyzerConfig.class))).thenReturn(List.of(thirdIndex));

		batchLogIndexer.index(1L, analyzerConfig());

		verify(indexerServiceClient, times(2)).index(anyList());

	}

	private AnalyzerConfig analyzerConfig() {
		AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setAnalyzerMode(ALL_LAUNCHES.getValue());
		return analyzerConfig;
	}
}