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
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.util.LaunchFinishedTestUtils;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchNotificationRunnerTest {

  private final GetProjectHandler getProjectHandler = mock(GetProjectHandler.class);
  private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
  private final GetIntegrationHandler getIntegrationHandler = mock(GetIntegrationHandler.class);
  private final MailServiceFactory mailServiceFactory = mock(MailServiceFactory.class);
  private final UserRepository userRepository = mock(UserRepository.class);

  private Integration emailIntegration = mock(Integration.class);

  private EmailService emailService = mock(EmailService.class);

  private final LaunchNotificationRunner runner = new LaunchNotificationRunner(getProjectHandler,
      getLaunchHandler,
      getIntegrationHandler,
      mailServiceFactory,
      userRepository
  );

  @Test
  void shouldNotSendWhenNotificationsDisabled() {

    final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
    final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER,
        launch.getProjectId());
    final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

    final Map<String, String> mapping = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute(), "false")
        .build();

    runner.handle(event, mapping);

    verify(getIntegrationHandler, times(0)).getEnabledByProjectIdOrGlobalAndIntegrationGroup(
        event.getProjectId(),
        IntegrationGroupEnum.NOTIFICATION
    );

  }

  @Test
  void shouldSendWhenNotificationsEnabled() {

    final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
    launch.setName("name1");
    final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER,
        launch.getProjectId());
    final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

    final Map<String, String> mapping = ImmutableMap.<String, String>builder()
        .put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute(), "true")
        .put(ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED.getAttribute(), "true")
        .build();

    final Project project = new Project();
    project.setId(1L);
    project.setSenderCases(LaunchFinishedTestUtils.getSenderCases());

    when(emailIntegration.getName()).thenReturn("email server");

    when(
        getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(event.getProjectId(),
            IntegrationGroupEnum.NOTIFICATION
        )).thenReturn(Optional.ofNullable(emailIntegration));

    when(userRepository.findLoginById(any())).thenReturn(Optional.of("owner"));
    when(mailServiceFactory.getDefaultEmailService(emailIntegration)).thenReturn(
        Optional.ofNullable(emailService));

    when(getLaunchHandler.get(event.getId())).thenReturn(launch);
    when(getProjectHandler.get(event.getProjectId())).thenReturn(project);
    when(getLaunchHandler.hasItemsWithIssues(launch)).thenReturn(Boolean.TRUE);

    runner.handle(event, mapping);
    verify(emailService, times(2)).sendLaunchFinishNotification(any(), any(), any(), any());

  }

}