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

package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersConfig;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.Optional;

import static com.epam.ta.reportportal.core.launch.cluster.ClusterGeneratorImpl.RP_CLUSTER_LAST_RUN_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ClusterGeneratorImplTest {

	private final TaskExecutor logClusterExecutor = new SyncTaskExecutor();

	private final AnalyzerStatusCache analyzerStatusCache = mock(AnalyzerStatusCache.class);
	private final LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);
	private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

	private final CreateClusterHandler createClusterHandler = mock(CreateClusterHandler.class);
	private final DeleteClusterHandler deleteClusterHandler = mock(DeleteClusterHandler.class);

	private final ItemAttributeRepository itemAttributeRepository = mock(ItemAttributeRepository.class);

	private final ClusterGenerator clusterGenerator = new ClusterGeneratorImpl(logClusterExecutor,
			analyzerStatusCache,
			launchPreparerService,
			analyzerServiceClient,
			createClusterHandler,
			deleteClusterHandler,
			itemAttributeRepository
	);

	@Test
	void shouldFailWhenNoAnalyzer() {
		when(analyzerServiceClient.hasClients()).thenReturn(false);

		final GenerateClustersConfig config = getConfig(false);
		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> clusterGenerator.generate(config));
		assertEquals("Impossible interact with integration. There are no analyzer services are deployed.", exception.getMessage());
	}

	@Test
	void shouldFailWhenCacheContainsLaunchId() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(true);

		final GenerateClustersConfig config = getConfig(false);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> clusterGenerator.generate(config));
		assertEquals("Impossible interact with integration. Clusters creation is in progress.", exception.getMessage());
	}

	@Test
	void shouldGenerateWithoutRemoveWhenForUpdate() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);
		when(launchPreparerService.prepare(anyLong(), any(AnalyzerConfig.class))).thenReturn(Optional.of(new IndexLaunch()));

		final GenerateClustersConfig config = getConfig(true);
		when(analyzerServiceClient.generateClusters(any(GenerateClustersRq.class))).thenReturn(new ClusterData());

		clusterGenerator.generate(config);

		verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
				config.getLaunchId(),
				config.getProject()
		);

		verify(deleteClusterHandler, times(0)).deleteLaunchClusters(config.getLaunchId());

		verify(analyzerServiceClient, times(1)).generateClusters(any(GenerateClustersRq.class));
		verify(createClusterHandler, times(1)).create(any(ClusterData.class));
		verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId());
	}

	@Test
	void shouldSaveAttributeWhenNotExists() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);
		when(launchPreparerService.prepare(anyLong(), any(AnalyzerConfig.class))).thenReturn(Optional.of(new IndexLaunch()));

		final GenerateClustersConfig config = getConfig(true);
		final ClusterData clusterData = new ClusterData();
		clusterData.setLaunchId(config.getLaunchId());
		when(analyzerServiceClient.generateClusters(any(GenerateClustersRq.class))).thenReturn(clusterData);

		when(itemAttributeRepository.findByLaunchIdAndKeyAndSystem(config.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, false)).thenReturn(
				Optional.empty());

		clusterGenerator.generate(config);

		verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
				config.getLaunchId(),
				config.getProject()
		);

		verify(deleteClusterHandler, times(0)).deleteLaunchClusters(config.getLaunchId());

		verify(analyzerServiceClient, times(1)).generateClusters(any(GenerateClustersRq.class));
		verify(createClusterHandler, times(1)).create(any(ClusterData.class));
		verify(itemAttributeRepository, times(1)).saveByLaunchId(anyLong(), anyString(), anyString(), anyBoolean());
		verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId());
	}

	@Test
	void shouldNotSaveAttributeWhenExists() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);
		when(launchPreparerService.prepare(anyLong(), any(AnalyzerConfig.class))).thenReturn(Optional.of(new IndexLaunch()));

		final GenerateClustersConfig config = getConfig(true);
		when(analyzerServiceClient.generateClusters(any(GenerateClustersRq.class))).thenReturn(new ClusterData());

		when(itemAttributeRepository.findByLaunchIdAndKeyAndSystem(config.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, false)).thenReturn(
				Optional.of(new ItemAttribute()));

		clusterGenerator.generate(config);

		verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
				config.getLaunchId(),
				config.getProject()
		);

		verify(deleteClusterHandler, times(0)).deleteLaunchClusters(config.getLaunchId());

		verify(analyzerServiceClient, times(1)).generateClusters(any(GenerateClustersRq.class));
		verify(createClusterHandler, times(1)).create(any(ClusterData.class));
		verify(itemAttributeRepository, times(0)).saveByLaunchId(anyLong(), anyString(), anyString(), anyBoolean());
		verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId());
	}

	@Test
	void shouldRemoveAndGenerateWhenNotForUpdate() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);
		when(launchPreparerService.prepare(anyLong(), any(AnalyzerConfig.class))).thenReturn(Optional.of(new IndexLaunch()));

		final GenerateClustersConfig config = getConfig(false);
		when(analyzerServiceClient.generateClusters(any(GenerateClustersRq.class))).thenReturn(new ClusterData());

		clusterGenerator.generate(config);

		verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
				config.getLaunchId(),
				config.getProject()
		);

		verify(deleteClusterHandler, times(1)).deleteLaunchClusters(config.getLaunchId());

		verify(analyzerServiceClient, times(1)).generateClusters(any(GenerateClustersRq.class));
		verify(createClusterHandler, times(1)).create(any(ClusterData.class));
		verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId());
	}

	private GenerateClustersConfig getConfig(boolean forUpdate) {
		final GenerateClustersConfig config = new GenerateClustersConfig();
		final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setNumberOfLogLines(1);
		config.setAnalyzerConfig(analyzerConfig);
		config.setProject(1L);
		config.setLaunchId(1L);
		config.setForUpdate(forUpdate);
		config.setCleanNumbers(false);
		return config;
	}

}