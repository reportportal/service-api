/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.cluster.pipeline.data;

import static com.epam.ta.reportportal.core.launch.cluster.utils.ConfigProvider.getConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.reportportal.model.project.AnalyzerConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class AnalyzerItemClusterDataProviderTest {

  private final LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);
  private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
  private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

  private final AnalyzerItemClusterDataProvider provider = new AnalyzerItemClusterDataProvider(
      analyzerServiceClient,
      getLaunchHandler,
      testItemRepository,
      launchPreparerService
  );

  @Test
  void shouldFailWhenNoAnalyzer() {
    when(analyzerServiceClient.hasClients()).thenReturn(false);

    final GenerateClustersConfig config = getConfig(false);
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> provider.provide(config));
    assertEquals(
        "Impossible interact with integration. There are no analyzer services are deployed.",
        exception.getMessage());
  }

  @Test
  void shouldReturnDataWhenIndexLaunchExists() {
    when(analyzerServiceClient.hasClients()).thenReturn(true);

    final GenerateClustersConfig config = getConfig(false);
    addItemIds(config);

    final Launch launch = new Launch();
    when(getLaunchHandler.get(config.getEntityContext().getLaunchId())).thenReturn(launch);

    final List<TestItem> testItems = List.of(new TestItem(), new TestItem());
    when(testItemRepository.findAllById(config.getEntityContext().getItemIds())).thenReturn(
        testItems);

    when(launchPreparerService.prepare(launch, testItems, config.getAnalyzerConfig())).thenReturn(
        Optional.of(new IndexLaunch()));
    when(analyzerServiceClient.generateClusters(any(GenerateClustersRq.class))).thenReturn(
        new ClusterData());
    final Optional<ClusterData> data = provider.provide(config);
    assertTrue(data.isPresent());
  }

  @Test
  void shouldNotReturnDataWhenNoItemIds() {
    when(analyzerServiceClient.hasClients()).thenReturn(true);

    final GenerateClustersConfig config = getConfig(false);

    final Optional<ClusterData> data = provider.provide(config);
    assertTrue(data.isEmpty());

    verify(getLaunchHandler, times(0)).get(anyLong());
    verify(testItemRepository, times(0)).findAllById(anyList());
    verify(launchPreparerService, times(0)).prepare(any(Launch.class), anyList(),
        any(AnalyzerConfig.class));
  }

  @Test
  void shouldNotReturnDataWhenNoIndexLaunch() {
    when(analyzerServiceClient.hasClients()).thenReturn(true);

    final GenerateClustersConfig config = getConfig(false);
    addItemIds(config);

    final Launch launch = new Launch();
    when(getLaunchHandler.get(config.getEntityContext().getLaunchId())).thenReturn(launch);

    final List<TestItem> testItems = List.of(new TestItem(), new TestItem());
    when(testItemRepository.findAllById(config.getEntityContext().getItemIds())).thenReturn(
        testItems);

    when(launchPreparerService.prepare(launch, testItems, config.getAnalyzerConfig())).thenReturn(
        Optional.of(new IndexLaunch()));

    final Optional<ClusterData> data = provider.provide(config);
    assertTrue(data.isEmpty());
  }

  private void addItemIds(GenerateClustersConfig config) {
    final ClusterEntityContext entityContext = config.getEntityContext();
    config.setEntityContext(
        ClusterEntityContext.of(entityContext.getLaunchId(), entityContext.getProjectId(),
            List.of(1L, 2L)));
  }

}