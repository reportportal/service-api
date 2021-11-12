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

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersConfig;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class AnalyzerClusterDataProvider implements ClusterDataProvider {

	private final LaunchPreparerService launchPreparerService;
	private final AnalyzerServiceClient analyzerServiceClient;

	public AnalyzerClusterDataProvider(LaunchPreparerService launchPreparerService, AnalyzerServiceClient analyzerServiceClient) {
		this.launchPreparerService = launchPreparerService;
		this.analyzerServiceClient = analyzerServiceClient;
	}

	@Override
	public Optional<ClusterData> provide(GenerateClustersConfig config) {
		expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer services are deployed."
		);
		return getGenerateRq(config).map(analyzerServiceClient::generateClusters);
	}

	private Optional<GenerateClustersRq> getGenerateRq(GenerateClustersConfig config) {
		return launchPreparerService.prepare(config.getLaunchId(), config.getAnalyzerConfig()).map(indexLaunch -> {
			final GenerateClustersRq generateClustersRq = new GenerateClustersRq();
			generateClustersRq.setLaunch(indexLaunch);
			generateClustersRq.setProject(config.getProject());
			generateClustersRq.setCleanNumbers(config.isCleanNumbers());
			generateClustersRq.setForUpdate(config.isForUpdate());
			generateClustersRq.setNumberOfLogLines(config.getAnalyzerConfig().getNumberOfLogLines());
			return generateClustersRq;
		});
	}
}
