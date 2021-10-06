package com.epam.ta.reportportal.core.analyzer.auto.indexer;

import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.LaunchPreparerService;
import com.epam.ta.reportportal.dao.LaunchRepository;
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
	private LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);

	private final BatchLogIndexer batchLogIndexer = new BatchLogIndexer(batchSize, launchRepository, launchPreparerService, indexerServiceClient);

	@Test
	void index() {

		final List<Long> ids = List.of(1L, 2L);
		when(launchRepository.findIdsByProjectIdAndModeAndStatusNotEq(eq(1L),
				any(JLaunchModeEnum.class),
				any(JStatusEnum.class),
				anyInt()
		)).thenReturn(ids);

		final IndexLaunch firstIndex = new IndexLaunch();
		final IndexLaunch secondIndex = new IndexLaunch();

		when(launchRepository.findIndexLaunchByIdsAndLogLevel(eq(ids), anyInt())).thenReturn(List.of(firstIndex, secondIndex));

		final List<Long> secondPortionIds = List.of(3L);
		when(launchRepository.findIdsByProjectIdAndModeAndStatusNotEqAfterId(eq(1L),
				any(JLaunchModeEnum.class),
				any(JStatusEnum.class),
				eq(2L),
				anyInt()
		)).thenReturn(secondPortionIds);
		final IndexLaunch thirdIndex = new IndexLaunch();
		when(launchRepository.findIndexLaunchByIdsAndLogLevel(eq(secondPortionIds), anyInt())).thenReturn(List.of(thirdIndex));

		doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			((IndexLaunch) args[0]).setTestItems(List.of(new IndexTestItem()));
			return null;
		}).when(launchPreparerService).fillLaunch(any(IndexLaunch.class));

		batchLogIndexer.index(1L, analyzerConfig());

		verify(indexerServiceClient, times(2)).index(anyList());

	}

	private AnalyzerConfig analyzerConfig() {
		AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setAnalyzerMode(ALL_LAUNCHES.getValue());
		return analyzerConfig;
	}
}