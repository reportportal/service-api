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

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.model.project.AnalyzerConfig;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class AutoAnalysisEnabledStarterTest {

  private final LaunchAutoAnalysisStarter delegate = mock(LaunchAutoAnalysisStarter.class);
  private final AutoAnalysisEnabledStarter autoAnalysisEnabledStarter = new AutoAnalysisEnabledStarter(
      delegate);

  @Test
  void shouldRunWhenAutoAnalysisEnabled() {

    final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.VIEWER, 1L);
    final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
    analyzerConfig.setIsAutoAnalyzerEnabled(true);

    final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(1L,
        analyzerConfig,
        Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
        user
    );

    autoAnalysisEnabledStarter.start(config);

    verify(delegate, times(1)).start(config);
  }

  @Test
  void shouldNotRunWhenAutoAnalysisDisabled() {
    final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.VIEWER, 1L);
    final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
    analyzerConfig.setIsAutoAnalyzerEnabled(false);

    final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(1L,
        analyzerConfig,
        Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
        user
    );

    autoAnalysisEnabledStarter.start(config);

    verify(delegate, times(0)).start(config);
  }

}
