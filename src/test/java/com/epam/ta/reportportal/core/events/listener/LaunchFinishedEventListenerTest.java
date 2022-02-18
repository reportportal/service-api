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

package com.epam.ta.reportportal.core.events.listener;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.subscriber.impl.delegate.ProjectConfigDelegatingSubscriber;
import com.epam.ta.reportportal.core.events.subscriber.impl.launch.finish.LaunchFinishedMessagePublisher;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchFinishedEventListenerTest {

	private final LaunchFinishedMessagePublisher launchFinishedMessagePublisher = mock(LaunchFinishedMessagePublisher.class);
	private final ProjectConfigDelegatingSubscriber<LaunchFinishedEvent> delegatingSubscriber = (ProjectConfigDelegatingSubscriber<LaunchFinishedEvent>) mock(
			ProjectConfigDelegatingSubscriber.class);

	private final LaunchFinishedEventListener eventListener = new LaunchFinishedEventListener(List.of(launchFinishedMessagePublisher,
			delegatingSubscriber
	));

	@Test
	void shouldNotSendWhenLaunchInDebug() {

		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEBUG).get();

		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, launch.getProjectId());
		final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

		eventListener.onApplicationEvent(event);

		verify(launchFinishedMessagePublisher, times(0)).handleEvent(event);
		verify(delegatingSubscriber, times(0)).handleEvent(event);
	}

	@Test
	void shouldSendWhenAutoAnalyzedDisabledEnabled() {

		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();

		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, launch.getProjectId());
		final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

		eventListener.onApplicationEvent(event);

		verify(launchFinishedMessagePublisher, times(1)).handleEvent(event);
		verify(delegatingSubscriber, times(1)).handleEvent(event);

	}

}