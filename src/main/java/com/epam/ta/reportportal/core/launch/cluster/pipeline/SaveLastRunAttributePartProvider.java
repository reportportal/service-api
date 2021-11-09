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
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import com.epam.ta.reportportal.pipeline.PipelinePartProvider;

import java.time.Instant;

import static com.epam.ta.reportportal.core.launch.cluster.ClusterGeneratorImpl.RP_CLUSTER_LAST_RUN_KEY;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class SaveLastRunAttributePartProvider implements PipelinePartProvider<GenerateClustersConfig> {

	private final ItemAttributeRepository itemAttributeRepository;

	public SaveLastRunAttributePartProvider(ItemAttributeRepository itemAttributeRepository) {
		this.itemAttributeRepository = itemAttributeRepository;
	}

	@Override
	public PipelinePart provide(GenerateClustersConfig config) {
		return () -> {
			final String lastRunDate = String.valueOf(Instant.now().toEpochMilli());
			itemAttributeRepository.findByLaunchIdAndKeyAndSystem(config.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, false)
					.ifPresentOrElse(attr -> {
						attr.setValue(lastRunDate);
						itemAttributeRepository.save(attr);
					}, () -> itemAttributeRepository.saveByLaunchId(config.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, lastRunDate, false));
		};
	}
}
