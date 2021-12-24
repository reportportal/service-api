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

import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.epam.ta.reportportal.ws.model.project.UniqueErrorConfig;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Map;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getUniqueErrorConfig;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class UniqueErrorAnalysisStarter {

	private final ClusterGenerator clusterGenerator;

	public UniqueErrorAnalysisStarter(ClusterGenerator clusterGenerator) {
		this.clusterGenerator = clusterGenerator;
	}

	public void start(ClusterEntityContext entityContext, Map<String, String> projectConfig) {

		final GenerateClustersConfig clustersConfig = new GenerateClustersConfig();

		clustersConfig.setEntityContext(entityContext);
		clustersConfig.setForUpdate(CollectionUtils.isNotEmpty(entityContext.getItemIds()));

		final UniqueErrorConfig uniqueErrorConfig = getUniqueErrorConfig(projectConfig);
		clustersConfig.setCleanNumbers(uniqueErrorConfig.isRemoveNumbers());

		final AnalyzerConfig analyzerConfig = getAnalyzerConfig(projectConfig);
		clustersConfig.setAnalyzerConfig(analyzerConfig);

		clusterGenerator.generate(clustersConfig);
	}
}
