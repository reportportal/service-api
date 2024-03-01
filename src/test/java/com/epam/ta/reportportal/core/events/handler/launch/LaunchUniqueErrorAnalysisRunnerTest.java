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

package com.epam.ta.reportportal.core.events.handler.launch;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.launch.cluster.UniqueErrorAnalysisStarter;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchUniqueErrorAnalysisRunnerTest {

  private final UniqueErrorAnalysisStarter starter = mock(UniqueErrorAnalysisStarter.class);

  private final LaunchUniqueErrorAnalysisRunner runner = new LaunchUniqueErrorAnalysisRunner(
      starter);

  @Test
  void shouldStartWhenEnabled() {

    final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
    final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER,
        launch.getProjectId());
    final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

    final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute(), "true")
        .put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(), "true")
        .build();

    runner.handle(event, projectConfig);

    final ArgumentCaptor<ClusterEntityContext> entityContextCaptor = ArgumentCaptor.forClass(
        ClusterEntityContext.class);
    verify(starter, times(1)).start(entityContextCaptor.capture(), anyMap());

    final ClusterEntityContext entityContext = entityContextCaptor.getValue();

    assertEquals(event.getId(), entityContext.getLaunchId());
    assertEquals(event.getProjectId(), entityContext.getProjectId());
  }

  @Test
  void shouldNotStartWhenDisabled() {

    final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
    final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER,
        launch.getProjectId());
    final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

    final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute(), "false")
        .put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(), "true")
        .build();

    runner.handle(event, projectConfig);

    verify(starter, times(0)).start(any(ClusterEntityContext.class), anyMap());

  }

}