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

import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import org.junit.jupiter.api.Test;

import static com.epam.ta.reportportal.core.launch.cluster.pipeline.SaveLastRunAttributePartProvider.RP_CLUSTER_LAST_RUN_KEY;
import static com.epam.ta.reportportal.core.launch.cluster.utils.ConfigProvider.getConfig;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class SaveLastRunAttributePartProviderTest {

	private final ItemAttributeRepository itemAttributeRepository = mock(ItemAttributeRepository.class);

	private final SaveLastRunAttributePartProvider provider = new SaveLastRunAttributePartProvider(itemAttributeRepository);

	@Test
	void shouldDeletePreviousAndSaveNew() {
		final GenerateClustersConfig config = getConfig(false);

		final PipelinePart pipelinePart = provider.provide(config);

		pipelinePart.handle();

		verify(itemAttributeRepository, times(1)).deleteAllByLaunchIdAndKeyAndSystem(eq(config.getEntityContext().getLaunchId()),
				eq(RP_CLUSTER_LAST_RUN_KEY),
				eq(true)
		);
		verify(itemAttributeRepository, times(1)).saveByLaunchId(eq(config.getEntityContext().getLaunchId()),
				eq(RP_CLUSTER_LAST_RUN_KEY),
				anyString(),
				eq(true)
		);
	}

	@Test
	void shouldNotSaveLastRunWhenForUpdate() {
		final GenerateClustersConfig config = getConfig(true);

		final PipelinePart pipelinePart = provider.provide(config);

		pipelinePart.handle();

		verify(itemAttributeRepository, times(0)).deleteAllByLaunchIdAndKeyAndSystem(eq(config.getEntityContext().getLaunchId()),
				eq(RP_CLUSTER_LAST_RUN_KEY),
				eq(true)
		);
		verify(itemAttributeRepository, times(0)).saveByLaunchId(eq(config.getEntityContext().getLaunchId()),
				eq(RP_CLUSTER_LAST_RUN_KEY),
				anyString(),
				eq(true)
		);
	}

}
