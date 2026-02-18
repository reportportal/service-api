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

package com.epam.reportportal.base.core.analyzer.config;

import com.epam.reportportal.base.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.reportportal.base.infrastructure.model.project.AnalyzerConfig;
import java.util.Set;
import lombok.Getter;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Getter
public class StartLaunchAutoAnalysisConfig {

  private final Long launchId;
  private final AnalyzerConfig analyzerConfig;
  private final Set<AnalyzeItemsMode> analyzeItemsModes;
  private final Long userId;
  private final String userLogin;

  private StartLaunchAutoAnalysisConfig(Long launchId, AnalyzerConfig analyzerConfig,
      Set<AnalyzeItemsMode> analyzeItemsModes,
      Long userId, String userLogin) {
    this.launchId = launchId;
    this.analyzerConfig = analyzerConfig;
    this.analyzeItemsModes = analyzeItemsModes;
    this.userId = userId;
    this.userLogin = userLogin;
  }

  public static StartLaunchAutoAnalysisConfig of(Long launchId, AnalyzerConfig analyzerConfig,
      Set<AnalyzeItemsMode> analyzeItemsModes, Long userId, String userLogin) {
    return new StartLaunchAutoAnalysisConfig(launchId, analyzerConfig, analyzeItemsModes, userId, userLogin);
  }

}
