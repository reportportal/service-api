package com.epam.ta.reportportal.core.analyzer.auto.indexer;

import static com.epam.ta.reportportal.entity.AnalyzeMode.ALL_LAUNCHES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.jooq.enums.JLaunchModeEnum;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.reportportal.model.analyzer.IndexTestItem;
import com.epam.reportportal.model.project.AnalyzerConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

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
  void indexWhenHasErrorLogs() {

    final List<Long> firstPortionIds = List.of(1L, 2L);
    final List<Long> secondPortionIds = List.of(3L);
    when(launchRepository.findIdsByProjectIdAndModeAndStatusNotEq(eq(1L),
        any(JLaunchModeEnum.class),
        any(JStatusEnum.class),
        anyInt()
    )).thenReturn(firstPortionIds);
    when(launchRepository.hasItemsWithLogsWithLogLevel(eq(1L), anyList(), anyInt())).thenReturn(
        true);
    when(launchRepository.hasItemsWithLogsWithLogLevel(eq(2L), anyList(), anyInt())).thenReturn(
        true);
    when(launchRepository.hasItemsWithLogsWithLogLevel(eq(3L), anyList(), anyInt())).thenReturn(
        true);

    final IndexLaunch firstIndex = new IndexLaunch();
    final List<IndexTestItem> firstIndexItems = List.of(new IndexTestItem());
    firstIndex.setTestItems(firstIndexItems);
    final IndexLaunch secondIndex = new IndexLaunch();
    final List<IndexTestItem> secondIndexItems = List.of(new IndexTestItem());
    secondIndex.setTestItems(secondIndexItems);
    when(launchPreparerService.prepare(eq(firstPortionIds), any(AnalyzerConfig.class))).thenReturn(
        List.of(firstIndex, secondIndex));

    when(launchRepository.findIdsByProjectIdAndModeAndStatusNotEqAfterId(eq(1L),
        any(JLaunchModeEnum.class),
        any(JStatusEnum.class),
        eq(2L),
        anyInt()
    )).thenReturn(secondPortionIds);

    final IndexLaunch thirdIndex = new IndexLaunch();
    final List<IndexTestItem> thirdIndexItems = List.of(new IndexTestItem(), new IndexTestItem(),
        new IndexTestItem());
    thirdIndex.setTestItems(thirdIndexItems);
    when(launchPreparerService.prepare(eq(secondPortionIds), any(AnalyzerConfig.class))).thenReturn(
        List.of(thirdIndex));

    batchLogIndexer.index(1L, analyzerConfig());

    final int expectedIndexedTimes = Stream.of(firstIndexItems, secondIndexItems, thirdIndexItems)
        .map(Collection::size)
        .mapToInt(this::getIndexedTimes)
        .sum();

    verify(indexerServiceClient, times(expectedIndexedTimes)).index(anyList());

  }

  private int getIndexedTimes(int expectedIndexedItems) {
    return BigDecimal.valueOf(expectedIndexedItems)
        .divide(BigDecimal.valueOf(batchSize), RoundingMode.CEILING).intValue();
  }

  @Test
  void indexWhenLaunchHasNoErrorLogs() {

    final List<Long> ids = List.of(1L, 2L);
    when(launchRepository.findIdsByProjectIdAndModeAndStatusNotEq(eq(1L),
        any(JLaunchModeEnum.class),
        any(JStatusEnum.class),
        anyInt()
    )).thenReturn(ids);
    when(launchRepository.hasItemsWithLogsWithLogLevel(eq(1L), anyList(), anyInt())).thenReturn(
        false);
    when(launchRepository.hasItemsWithLogsWithLogLevel(eq(2L), anyList(), anyInt())).thenReturn(
        false);

    batchLogIndexer.index(1L, analyzerConfig());

    verify(launchPreparerService, times(0)).prepare(anyList(), any(AnalyzerConfig.class));
    verify(indexerServiceClient, times(0)).index(anyList());

  }

  private AnalyzerConfig analyzerConfig() {
    AnalyzerConfig analyzerConfig = new AnalyzerConfig();
    analyzerConfig.setAnalyzerMode(ALL_LAUNCHES.getValue());
    return analyzerConfig;
  }
}