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

package com.epam.ta.reportportal.core.analyzer.auto.impl.preparer;

import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchPreparerServiceImplTest {

	private final LaunchRepository launchRepository = mock(LaunchRepository.class);
	private final ClusterRepository clusterRepository = mock(ClusterRepository.class);
	private final TestItemPreparerService testItemPreparerService = mock(TestItemPreparerService.class);

	private final LaunchPreparerServiceImpl preparerService = new LaunchPreparerServiceImpl(launchRepository,
			clusterRepository,
			testItemPreparerService
	);

	@Test
	void prepare() {

		final long launchId = 1L;

		final IndexLaunch indexLaunch = new IndexLaunch();
		indexLaunch.setLaunchId(launchId);
		indexLaunch.setLaunchName("name");
		indexLaunch.setProjectId(1L);

		when(launchRepository.findIndexLaunchByIdsAndLogLevel(eq(List.of(launchId)), anyInt())).thenReturn(List.of(indexLaunch));

		final IndexTestItem indexTestItem = new IndexTestItem();
		when(testItemPreparerService.prepare(indexLaunch.getLaunchId())).thenReturn(List.of(indexTestItem));

		final Cluster cluster = new Cluster();
		when(clusterRepository.findAllByLaunchId(indexLaunch.getLaunchId())).thenReturn(List.of(cluster));

		final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		preparerService.prepare(List.of(launchId), analyzerConfig);
	}

}