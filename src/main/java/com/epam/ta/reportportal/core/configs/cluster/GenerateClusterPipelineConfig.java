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

package com.epam.ta.reportportal.core.configs.cluster;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersConfig;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.launch.cluster.CreateClusterHandler;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.AnalyzerClusterDataProvider;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.DeleteClustersPartProvider;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.SaveClusterDataPartProvider;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.SaveLastRunAttributePartProvider;
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.pipeline.PipelineConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class GenerateClusterPipelineConfig {

	private final CreateClusterHandler createClusterHandler;

	private final LaunchPreparerService launchPreparerService;
	private final AnalyzerServiceClient analyzerServiceClient;

	private final ClusterRepository clusterRepository;
	private final LogRepository logRepository;
	private final ItemAttributeRepository itemAttributeRepository;

	@Autowired
	public GenerateClusterPipelineConfig(CreateClusterHandler createClusterHandler, LaunchPreparerService launchPreparerService,
			AnalyzerServiceClient analyzerServiceClient, ClusterRepository clusterRepository, LogRepository logRepository,
			ItemAttributeRepository itemAttributeRepository) {
		this.createClusterHandler = createClusterHandler;
		this.launchPreparerService = launchPreparerService;
		this.analyzerServiceClient = analyzerServiceClient;
		this.clusterRepository = clusterRepository;
		this.logRepository = logRepository;
		this.itemAttributeRepository = itemAttributeRepository;
	}

	@Bean
	public PipelineConstructor<GenerateClustersConfig> generateClustersPipelineConstructor() {
		return new PipelineConstructor<>(List.of(deleteClustersPartProvider(),
				saveClusterDataPartProvider(),
				saveLastRunAttributePartProvider()
		));
	}

	@Bean
	public DeleteClustersPartProvider deleteClustersPartProvider() {
		return new DeleteClustersPartProvider(clusterRepository, logRepository);
	}

	@Bean
	public AnalyzerClusterDataProvider analyzerClusterDataProvider() {
		return new AnalyzerClusterDataProvider(launchPreparerService, analyzerServiceClient);
	}

	@Bean
	public SaveClusterDataPartProvider saveClusterDataPartProvider() {
		return new SaveClusterDataPartProvider(analyzerClusterDataProvider(), createClusterHandler);
	}

	@Bean
	public SaveLastRunAttributePartProvider saveLastRunAttributePartProvider() {
		return new SaveLastRunAttributePartProvider(itemAttributeRepository);
	}
}
