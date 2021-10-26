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
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

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
	private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

	private final CreateClusterHandler createClusterHandler = mock(CreateClusterHandler.class);
	private final DeleteClusterHandler deleteClusterHandler = mock(DeleteClusterHandler.class);

	private final ClusterGenerator clusterGenerator = new ClusterGeneratorImpl(logClusterExecutor,
			analyzerStatusCache,
			analyzerServiceClient,
			createClusterHandler,
			deleteClusterHandler
	);

	@Test
	void shouldFailWhenNoAnalyzer() {
		when(analyzerServiceClient.hasClients()).thenReturn(false);

		final GenerateClustersRq generateRq = getGenerateRq(false);
		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> clusterGenerator.generate(generateRq));
		assertEquals("Impossible interact with integration. There are no analyzer services are deployed.", exception.getMessage());
	}

	@Test
	void shouldFailWhenCacheContainsLaunchId() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(true);

		final GenerateClustersRq generateRq = getGenerateRq(false);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> clusterGenerator.generate(generateRq));
		assertEquals("Impossible interact with integration. Clusters creation is in progress.", exception.getMessage());
	}

	@Test
	void shouldGenerateWithoutRemoveWhenForUpdate() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);

		final GenerateClustersRq generateRq = getGenerateRq(true);
		when(analyzerServiceClient.generateClusters(generateRq)).thenReturn(new ClusterData());

		clusterGenerator.generate(generateRq);

		verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
				generateRq.getLaunchId(),
				generateRq.getProject()
		);

		verify(deleteClusterHandler, times(0)).deleteLaunchClusters(generateRq.getLaunchId());

		verify(analyzerServiceClient, times(1)).generateClusters(generateRq);
		verify(createClusterHandler, times(1)).create(any(ClusterData.class));
		verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY,
				generateRq.getLaunchId()
		);
	}

	@Test
	void shouldRemoveAndGenerateWhenNotForUpdate() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(false);

		final GenerateClustersRq generateRq = getGenerateRq(false);
		when(analyzerServiceClient.generateClusters(generateRq)).thenReturn(new ClusterData());

		clusterGenerator.generate(generateRq);

		verify(analyzerStatusCache, times(1)).analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
				generateRq.getLaunchId(),
				generateRq.getProject()
		);

		verify(deleteClusterHandler, times(1)).deleteLaunchClusters(generateRq.getLaunchId());

		verify(analyzerServiceClient, times(1)).generateClusters(generateRq);
		verify(createClusterHandler, times(1)).create(any(ClusterData.class));
		verify(analyzerStatusCache, times(1)).analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY,
				generateRq.getLaunchId()
		);
	}

	private GenerateClustersRq getGenerateRq(boolean forUpdate) {
		final GenerateClustersRq generateClustersRq = new GenerateClustersRq();
		generateClustersRq.setProject(1L);
		generateClustersRq.setLaunchId(1L);
		generateClustersRq.setLaunchName("name");
		generateClustersRq.setForUpdate(forUpdate);
		generateClustersRq.setCleanNumbers(false);
		generateClustersRq.setNumberOfLogLines(1);
		return generateClustersRq;
	}

}