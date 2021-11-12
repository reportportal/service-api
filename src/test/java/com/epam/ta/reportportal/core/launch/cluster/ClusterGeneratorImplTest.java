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

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersConfig;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.pipeline.PipelineConstructor;
import com.epam.ta.reportportal.pipeline.TransactionalPipeline;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import static com.epam.ta.reportportal.core.launch.cluster.utils.ConfigProvider.getConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ClusterGeneratorImplTest {

	private final TaskExecutor logClusterExecutor = new SyncTaskExecutor();

	private final AnalyzerStatusCache analyzerStatusCache = mock(AnalyzerStatusCache.class);

	private final PipelineConstructor<GenerateClustersConfig> pipelineConstructor = (PipelineConstructor<GenerateClustersConfig>) mock(
			PipelineConstructor.class);

	private final TransactionalPipeline transactionalPipeline = mock(TransactionalPipeline.class);

	private final ClusterGenerator clusterGenerator = new ClusterGeneratorImpl(logClusterExecutor,
			analyzerStatusCache,
			pipelineConstructor,
			transactionalPipeline
	);

	@Test
	void shouldFailWhenCacheContainsLaunchId() {
		when(analyzerStatusCache.containsLaunchId(anyString(), anyLong())).thenReturn(true);

		final GenerateClustersConfig config = getConfig(false);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> clusterGenerator.generate(config));
		assertEquals("Impossible interact with integration. Clusters creation is in progress.", exception.getMessage());
	}

}