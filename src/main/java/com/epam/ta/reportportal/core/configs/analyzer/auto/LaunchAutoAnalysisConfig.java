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

package com.epam.ta.reportportal.core.configs.analyzer.auto;

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.starter.CollectingAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.starter.decorator.AsyncAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.starter.decorator.AutoAnalysisEnabledStarter;
import com.epam.ta.reportportal.core.analyzer.auto.starter.decorator.ExistingAnalyzerStarter;
import com.epam.ta.reportportal.core.analyzer.auto.starter.decorator.IndexingAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class LaunchAutoAnalysisConfig {

  private final GetLaunchHandler getLaunchHandler;

  private final AnalyzeCollectorFactory analyzeCollectorFactory;
  private final AnalyzerService analyzerService;

  private final LogIndexer logIndexer;

  private final TaskExecutor autoAnalyzeTaskExecutor;

  @Autowired
  public LaunchAutoAnalysisConfig(GetLaunchHandler getLaunchHandler,
      AnalyzeCollectorFactory analyzeCollectorFactory,
      AnalyzerService analyzerService, LogIndexer logIndexer,
      TaskExecutor autoAnalyzeTaskExecutor) {
    this.getLaunchHandler = getLaunchHandler;
    this.analyzeCollectorFactory = analyzeCollectorFactory;
    this.analyzerService = analyzerService;
    this.logIndexer = logIndexer;
    this.autoAnalyzeTaskExecutor = autoAnalyzeTaskExecutor;
  }

  @Bean
  public LaunchAutoAnalysisStarter manualAnalysisStarter() {
    return new ExistingAnalyzerStarter(analyzerService, asyncAutoAnalysisStarter());
  }

  @Bean
  public LaunchAutoAnalysisStarter autoAnalysisStarter() {
    return new ExistingAnalyzerStarter(analyzerService, indexingAutoAnalysisStarter());
  }

  @Bean
  public CollectingAutoAnalysisStarter collectingAutoAnalysisStarter() {
    return new CollectingAutoAnalysisStarter(getLaunchHandler, analyzeCollectorFactory,
        analyzerService, logIndexer);
  }

  @Bean
  public AsyncAutoAnalysisStarter asyncAutoAnalysisStarter() {
    return new AsyncAutoAnalysisStarter(autoAnalyzeTaskExecutor, collectingAutoAnalysisStarter());
  }

  @Bean
  public AutoAnalysisEnabledStarter autoAnalysisEnabledStarter() {
    return new AutoAnalysisEnabledStarter(collectingAutoAnalysisStarter());
  }

  @Bean
  public IndexingAutoAnalysisStarter indexingAutoAnalysisStarter() {
    return new IndexingAutoAnalysisStarter(getLaunchHandler, logIndexer,
        autoAnalysisEnabledStarter());
  }

}
