/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.events.handler.subscriber.impl;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.util.LaunchFinishedTestUtils;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchNotificationSubscriberTest {

	private final GetIntegrationHandler getIntegrationHandler = mock(GetIntegrationHandler.class);
	private final MailServiceFactory mailServiceFactory = mock(MailServiceFactory.class);
	private final LaunchRepository launchRepository = mock(LaunchRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);

	private Integration emailIntegration = mock(Integration.class);

	private EmailService emailService = mock(EmailService.class);

	private final LaunchNotificationSubscriber launchNotificationSubscriber = new LaunchNotificationSubscriber(getIntegrationHandler,
			mailServiceFactory,
			launchRepository,
			userRepository
	);

	@Test
	void shouldNotSendWhenNotificationsDisabled() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

		Map<ProjectAttributeEnum, String> mapping = ImmutableMap.<ProjectAttributeEnum, String>builder().put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED,
				"false"
		).build();

		Project project = new Project();
		project.setId(1L);
		project.setProjectAttributes(LaunchFinishedTestUtils.getProjectAttributes(mapping));

		launchNotificationSubscriber.handleEvent(event, project, launch.get());

		verify(getIntegrationHandler, times(0)).getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
				IntegrationGroupEnum.NOTIFICATION
		);

	}

	@Test
	void shouldSendWhenNotificationsEnabled() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);
		launch.get().setName("name1");

		Map<ProjectAttributeEnum, String> mapping = ImmutableMap.<ProjectAttributeEnum, String>builder().put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED,
				"true"
		).put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED, "true").build();

		Project project = new Project();
		project.setId(1L);
		project.setProjectAttributes(LaunchFinishedTestUtils.getProjectAttributes(mapping));
		project.setSenderCases(LaunchFinishedTestUtils.getSenderCases());

		when(getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
				IntegrationGroupEnum.NOTIFICATION
		)).thenReturn(Optional.ofNullable(emailIntegration));

		when(mailServiceFactory.getDefaultEmailService(emailIntegration)).thenReturn(Optional.ofNullable(emailService));

		launchNotificationSubscriber.handleEvent(event, project, launch.get());
		verify(emailService, times(2)).sendLaunchFinishNotification(any(), any(), any(), any());

	}

}