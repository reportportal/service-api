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

package com.epam.reportportal.base.core.events.handler.launch;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.base.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.reportportal.base.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.reportportal.base.core.events.domain.LaunchFinishedEvent;
import com.epam.reportportal.base.core.launch.impl.LaunchTestUtil;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchAutoAnalysisRunnerTest {

  private final LaunchAutoAnalysisStarter starter = mock(LaunchAutoAnalysisStarter.class);

  private final LaunchAutoAnalysisRunner runner = new LaunchAutoAnalysisRunner(starter);

  @Test
  void shouldAnalyzeWhenEnabled() {

    final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
    final ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.VIEWER,
        launch.getProjectId());
    final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl", 1L);

    final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "true")
        .build();

    runner.handle(event, projectConfig);

    verify(starter, times(1)).start(any(StartLaunchAutoAnalysisConfig.class));

  }

}
