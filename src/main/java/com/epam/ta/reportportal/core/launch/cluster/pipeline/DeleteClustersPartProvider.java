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
import com.epam.ta.reportportal.core.launch.cluster.DeleteClusterHandler;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import com.epam.ta.reportportal.pipeline.PipelinePartProvider;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DeleteClustersPartProvider implements PipelinePartProvider<GenerateClustersConfig> {

	private final DeleteClusterHandler deleteClusterHandler;

	public DeleteClustersPartProvider(DeleteClusterHandler deleteClusterHandler) {
		this.deleteClusterHandler = deleteClusterHandler;
	}

	@Override
	public PipelinePart provide(GenerateClustersConfig config) {
		return () -> {
			if (!config.isForUpdate()) {
				deleteClusterHandler.deleteLaunchClusters(config.getLaunchId());
			}
		};
	}
}
