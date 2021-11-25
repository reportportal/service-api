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

package com.epam.ta.reportportal.core.launch.cluster.pipeline.data.resolver;

import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.ClusterDataProvider;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.resolver.evaluator.ClusterDataProviderEvaluator;

import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ClusterDataProviderResolver {

	private final List<ClusterDataProviderEvaluator> evaluators;

	public ClusterDataProviderResolver(List<ClusterDataProviderEvaluator> evaluators) {
		this.evaluators = evaluators;
	}

	public Optional<ClusterDataProvider> resolve(GenerateClustersConfig config) {
		return evaluators.stream().filter(e -> e.supports(config)).map(ClusterDataProviderEvaluator::getProvider).findFirst();
	}
}
