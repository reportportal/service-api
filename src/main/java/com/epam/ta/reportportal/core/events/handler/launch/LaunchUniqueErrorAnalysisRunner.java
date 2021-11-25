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

package com.epam.ta.reportportal.core.events.handler.launch;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.launch.cluster.ClusterGenerator;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.epam.ta.reportportal.ws.model.project.UniqueErrorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getUniqueErrorConfig;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchUniqueErrorAnalysisRunner implements ConfigurableEventHandler<LaunchFinishedEvent, Map<String, String>> {

	private final ClusterGenerator clusterGenerator;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public LaunchUniqueErrorAnalysisRunner(@Qualifier("uniqueErrorGenerator") ClusterGenerator clusterGenerator,
			ApplicationEventPublisher eventPublisher) {
		this.clusterGenerator = clusterGenerator;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void handle(LaunchFinishedEvent launchFinishedEvent, Map<String, String> projectConfig) {

		final UniqueErrorConfig uniqueErrorConfig = getUniqueErrorConfig(projectConfig);

		if (uniqueErrorConfig.isEnabled()) {
			final GenerateClustersConfig clustersConfig = new GenerateClustersConfig();
			clustersConfig.setForUpdate(false);
			clustersConfig.setCleanNumbers(uniqueErrorConfig.isRemoveNumbers());

			final AnalyzerConfig analyzerConfig = getAnalyzerConfig(projectConfig);
			clustersConfig.setAnalyzerConfig(analyzerConfig);

			final ClusterEntityContext entityContext = ClusterEntityContext.of(launchFinishedEvent.getId(),
					launchFinishedEvent.getProjectId()
			);
			clustersConfig.setEntityContext(entityContext);

			clusterGenerator.generate(clustersConfig);
		}

	}
}
