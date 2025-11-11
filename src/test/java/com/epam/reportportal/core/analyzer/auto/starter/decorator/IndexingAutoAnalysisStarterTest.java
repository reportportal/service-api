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

package com.epam.reportportal.core.analyzer.auto.starter.decorator;

import static com.epam.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.reportportal.core.launch.GetLaunchHandler;
import com.epam.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.reportportal.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.model.project.AnalyzerConfig;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class IndexingAutoAnalysisStarterTest {

  private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
  private final LogIndexer logIndexer = mock(LogIndexer.class);
  private final LaunchAutoAnalysisStarter delegate = mock(LaunchAutoAnalysisStarter.class);

  private final IndexingAutoAnalysisStarter indexingAutoAnalysisStarter = new IndexingAutoAnalysisStarter(
      getLaunchHandler,
      logIndexer,
      delegate
  );

  @Test
  void shouldIndex() {
    final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();

    final ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(launch.getId(),
        new AnalyzerConfig(),
        Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
        user
    );

    when(getLaunchHandler.get(config.getLaunchId())).thenReturn(launch);

    indexingAutoAnalysisStarter.start(config);

    verify(logIndexer, times(1)).indexLaunchLogs(launch, config.getAnalyzerConfig());
  }

}
