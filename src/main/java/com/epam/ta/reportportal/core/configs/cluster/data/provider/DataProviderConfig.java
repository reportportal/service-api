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

package com.epam.ta.reportportal.core.configs.cluster.data.provider;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.AnalyzerItemClusterDataProvider;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.AnalyzerLaunchClusterDataProvider;
import com.epam.ta.reportportal.dao.TestItemRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class DataProviderConfig {

  private final GetLaunchHandler getLaunchHandler;

  private final LaunchPreparerService launchPreparerService;
  private final AnalyzerServiceClient analyzerServiceClient;

  private final TestItemRepository testItemRepository;

  public DataProviderConfig(GetLaunchHandler getLaunchHandler,
      LaunchPreparerService launchPreparerService,
      AnalyzerServiceClient analyzerServiceClient, TestItemRepository testItemRepository) {
    this.getLaunchHandler = getLaunchHandler;
    this.launchPreparerService = launchPreparerService;
    this.analyzerServiceClient = analyzerServiceClient;
    this.testItemRepository = testItemRepository;
  }

  @Bean
  public AnalyzerLaunchClusterDataProvider analyzerLaunchClusterDataProvider() {
    return new AnalyzerLaunchClusterDataProvider(analyzerServiceClient, launchPreparerService);
  }

  @Bean
  public AnalyzerItemClusterDataProvider analyzerItemClusterDataProvider() {
    return new AnalyzerItemClusterDataProvider(analyzerServiceClient, getLaunchHandler,
        testItemRepository, launchPreparerService);
  }
}
