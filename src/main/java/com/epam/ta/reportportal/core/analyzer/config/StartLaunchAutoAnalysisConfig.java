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

package com.epam.ta.reportportal.core.analyzer.config;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class StartLaunchAutoAnalysisConfig {

  private final Long launchId;
  private final AnalyzerConfig analyzerConfig;
  private final Set<AnalyzeItemsMode> analyzeItemsModes;
  private final ReportPortalUser user;

  private StartLaunchAutoAnalysisConfig(Long launchId, AnalyzerConfig analyzerConfig,
      Set<AnalyzeItemsMode> analyzeItemsModes,
      ReportPortalUser user) {
    this.launchId = launchId;
    this.analyzerConfig = analyzerConfig;
    this.analyzeItemsModes = analyzeItemsModes;
    this.user = user;
  }

  public static StartLaunchAutoAnalysisConfig of(Long launchId, AnalyzerConfig analyzerConfig,
      Set<AnalyzeItemsMode> analyzeItemsModes,
      ReportPortalUser user) {
    return new StartLaunchAutoAnalysisConfig(launchId, analyzerConfig, analyzeItemsModes, user);
  }

  public Long getLaunchId() {
    return launchId;
  }

  public AnalyzerConfig getAnalyzerConfig() {
    return analyzerConfig;
  }

  public Set<AnalyzeItemsMode> getAnalyzeItemsModes() {
    return analyzeItemsModes;
  }

  public ReportPortalUser getUser() {
    return user;
  }
}
