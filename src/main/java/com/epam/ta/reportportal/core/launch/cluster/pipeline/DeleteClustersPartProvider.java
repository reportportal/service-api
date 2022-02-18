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
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import com.epam.ta.reportportal.pipeline.PipelinePartProvider;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DeleteClustersPartProvider implements PipelinePartProvider<GenerateClustersConfig> {

	private final ClusterRepository clusterRepository;
	private final LogRepository logRepository;

	public DeleteClustersPartProvider(ClusterRepository clusterRepository, LogRepository logRepository) {
		this.clusterRepository = clusterRepository;
		this.logRepository = logRepository;
	}

	@Override
	public PipelinePart provide(GenerateClustersConfig config) {
		return () -> {
			final ClusterEntityContext entityContext = config.getEntityContext();
			if (config.isForUpdate()) {
				logRepository.updateClusterIdSetNullByItemIds(entityContext.getItemIds());
				clusterRepository.deleteClusterTestItemsByItemIds(entityContext.getItemIds());
			} else {
				logRepository.updateClusterIdSetNullByLaunchId(entityContext.getLaunchId());
				clusterRepository.deleteClusterTestItemsByLaunchId(entityContext.getLaunchId());
				clusterRepository.deleteAllByLaunchId(entityContext.getLaunchId());
			}
		};
	}
}
