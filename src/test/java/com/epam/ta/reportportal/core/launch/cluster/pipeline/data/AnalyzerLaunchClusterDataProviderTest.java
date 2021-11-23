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

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.epam.ta.reportportal.core.launch.cluster.utils.ConfigProvider.getConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class AnalyzerLaunchClusterDataProviderTest {
	private final LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);
	private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

	private final AnalyzerLaunchClusterDataProvider provider = new AnalyzerLaunchClusterDataProvider(analyzerServiceClient,
			launchPreparerService
	);

	@Test
	void shouldFailWhenNoAnalyzer() {
		when(analyzerServiceClient.hasClients()).thenReturn(false);

		final GenerateClustersConfig config = getConfig(false);
		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> provider.provide(config));
		assertEquals("Impossible interact with integration. There are no analyzer services are deployed.", exception.getMessage());
	}

	@Test
	void shouldReturnDataWhenIndexLaunchExists() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);

		final GenerateClustersConfig config = getConfig(false);

		when(launchPreparerService.prepare(config.getEntityContext().getLaunchId(),
				config.getAnalyzerConfig()
		)).thenReturn(Optional.of(new IndexLaunch()));
		when(analyzerServiceClient.generateClusters(any(GenerateClustersRq.class))).thenReturn(new ClusterData());
		final Optional<ClusterData> data = provider.provide(config);
		assertTrue(data.isPresent());
	}

	@Test
	void shouldNotReturnDataWhenNoIndexLaunch() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);

		final GenerateClustersConfig config = getConfig(false);

		when(launchPreparerService.prepare(config.getEntityContext().getLaunchId(),
				config.getAnalyzerConfig()
		)).thenReturn(Optional.empty());
		final Optional<ClusterData> data = provider.provide(config);
		assertTrue(data.isEmpty());
	}
}