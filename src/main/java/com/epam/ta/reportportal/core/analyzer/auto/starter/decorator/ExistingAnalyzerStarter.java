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

package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ExistingAnalyzerStarter implements LaunchAutoAnalysisStarter {
  private final AnalyzerService analyzerService;
  private final LaunchAutoAnalysisStarter launchAutoAnalysisStarter;

  public ExistingAnalyzerStarter(AnalyzerService analyzerService,
      LaunchAutoAnalysisStarter launchAutoAnalysisStarter) {
    this.analyzerService = analyzerService;
    this.launchAutoAnalysisStarter = launchAutoAnalysisStarter;
  }

  @Override
  public void start(StartLaunchAutoAnalysisConfig config) {
    expect(analyzerService.hasAnalyzers(), Predicate.isEqual(true)).verify(
        ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
        "There are no analyzer services are deployed."
    );
    launchAutoAnalysisStarter.start(config);
  }
}
