/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.events.handler.launch;

import com.epam.reportportal.base.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.reportportal.base.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.reportportal.base.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.reportportal.base.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.reportportal.base.core.events.domain.LaunchFinishedEvent;
import com.epam.reportportal.base.core.events.handler.ConfigurableEventHandler;
import com.epam.reportportal.base.infrastructure.model.project.AnalyzerConfig;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Triggers auto analysis when a launch finishes, if enabled.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchAutoAnalysisRunner implements
    ConfigurableEventHandler<LaunchFinishedEvent, Map<String, String>> {

  private final LaunchAutoAnalysisStarter autoAnalysisStarter;

  public LaunchAutoAnalysisRunner(LaunchAutoAnalysisStarter autoAnalysisStarter) {
    this.autoAnalysisStarter = autoAnalysisStarter;
  }

  @Override
  public void handle(LaunchFinishedEvent launchFinishedEvent, Map<String, String> projectConfig) {
    final AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(projectConfig);
    final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(
        launchFinishedEvent.getId(),
        analyzerConfig,
        Set.of(AnalyzeItemsMode.IGNORE_IMMEDIATE),
        launchFinishedEvent.getUserId(),
        launchFinishedEvent.getUserLogin()
    );
    autoAnalysisStarter.start(config);
  }

}
