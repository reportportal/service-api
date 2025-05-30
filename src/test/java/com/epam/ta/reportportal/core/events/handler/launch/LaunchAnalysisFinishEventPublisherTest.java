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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchAnalysisFinishEventPublisherTest {

  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

  private final LaunchAnalysisFinishEventPublisher publisher = new LaunchAnalysisFinishEventPublisher(
      eventPublisher);

  @Test
  void shouldSendEvent() {

    final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
    final ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER,
        launch.getProjectId());
    final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

    final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "true")
        .build();

    publisher.handle(event, projectConfig);

    verify(eventPublisher, times(1)).publishEvent(any());

  }

}
