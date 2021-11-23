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

package com.epam.ta.reportportal.core.launch.cluster.pipeline.data.resolver.evaluator;

import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.ClusterDataProvider;

import java.util.function.Predicate;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ClusterDataProviderEvaluator {

	private final Predicate<GenerateClustersConfig> supportsPredicate;
	private final ClusterDataProvider clusterDataProvider;

	public ClusterDataProviderEvaluator(Predicate<GenerateClustersConfig> supportsPredicate, ClusterDataProvider clusterDataProvider) {
		this.supportsPredicate = supportsPredicate;
		this.clusterDataProvider = clusterDataProvider;
	}

	public boolean supports(GenerateClustersConfig config) {
		return supportsPredicate.test(config);
	}

	public ClusterDataProvider getProvider() {
		return clusterDataProvider;
	}
}
