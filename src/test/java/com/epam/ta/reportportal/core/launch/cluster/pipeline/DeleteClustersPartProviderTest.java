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

package com.epam.ta.reportportal.core.launch.cluster.pipeline;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersConfig;
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import org.junit.jupiter.api.Test;

import static com.epam.ta.reportportal.core.launch.cluster.utils.ConfigProvider.getConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class DeleteClustersPartProviderTest {

	private final ClusterRepository clusterRepository = mock(ClusterRepository.class);
	private final LogRepository logRepository = mock(LogRepository.class);

	private final DeleteClustersPartProvider provider = new DeleteClustersPartProvider(clusterRepository, logRepository);

	@Test
	void shouldDeleteWhenNotForUpdate() {
		final GenerateClustersConfig config = getConfig(false);
		final PipelinePart pipelinePart = provider.provide(config);
		pipelinePart.handle();

		verify(logRepository, times(1)).updateClusterIdSetNullByLaunchId(config.getLaunchId());
		verify(clusterRepository, times(1)).deleteClusterTestItemsByLaunchId(config.getLaunchId());
		verify(clusterRepository, times(1)).deleteAllByLaunchId(config.getLaunchId());
	}

	@Test
	void shouldNotDeleteWhenForUpdate() {
		final GenerateClustersConfig config = getConfig(true);
		final PipelinePart pipelinePart = provider.provide(config);
		pipelinePart.handle();

		verify(logRepository, times(0)).updateClusterIdSetNullByLaunchId(config.getLaunchId());
		verify(clusterRepository, times(0)).deleteClusterTestItemsByLaunchId(config.getLaunchId());
		verify(clusterRepository, times(0)).deleteAllByLaunchId(config.getLaunchId());
	}

}