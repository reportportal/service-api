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

import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import com.epam.ta.reportportal.pipeline.PipelinePartProvider;

import java.time.Instant;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class SaveLastRunAttributePartProvider implements PipelinePartProvider<GenerateClustersConfig> {

	public static final String RP_CLUSTER_LAST_RUN_KEY = "rp.cluster.lastRun";

	private final ItemAttributeRepository itemAttributeRepository;

	public SaveLastRunAttributePartProvider(ItemAttributeRepository itemAttributeRepository) {
		this.itemAttributeRepository = itemAttributeRepository;
	}

	@Override
	public PipelinePart provide(GenerateClustersConfig config) {
		return () -> {
			final String lastRunDate = String.valueOf(Instant.now().toEpochMilli());
			final ClusterEntityContext entityContext = config.getEntityContext();
			itemAttributeRepository.findByLaunchIdAndKeyAndSystem(entityContext.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, true)
					.ifPresentOrElse(attr -> {
								attr.setValue(lastRunDate);
								itemAttributeRepository.save(attr);
							},
							() -> itemAttributeRepository.saveByLaunchId(entityContext.getLaunchId(),
									RP_CLUSTER_LAST_RUN_KEY,
									lastRunDate,
									true
							)
					);
		};
	}
}
