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

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.subscriber.LaunchFinishedEventSubscriber;
import com.epam.ta.reportportal.core.events.handler.subscriber.impl.LaunchAutoAnalysisSubscriber;
import com.epam.ta.reportportal.core.events.handler.subscriber.impl.LaunchNotificationSubscriber;
import com.epam.ta.reportportal.core.events.handler.subscriber.impl.LaunchPatternAnalysisSubscriber;
import com.epam.ta.reportportal.core.events.handler.util.LaunchFinishedTestUtils;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchFinishedEventHandlerTest {

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);

	private final LaunchRepository launchRepository = mock(LaunchRepository.class);

	private final LaunchAutoAnalysisSubscriber autoAnalysisSubscriber = mock(LaunchAutoAnalysisSubscriber.class);

	private final LaunchNotificationSubscriber notificationSubscriber = mock(LaunchNotificationSubscriber.class);

	private final LaunchPatternAnalysisSubscriber patternAnalysisSubscriber = mock(LaunchPatternAnalysisSubscriber.class);

	private List<LaunchFinishedEventSubscriber> launchFinishedEventSubscribers = Lists.newArrayList(autoAnalysisSubscriber,
			notificationSubscriber,
			patternAnalysisSubscriber
	);

	private final LaunchFinishedEventHandler launchFinishedEventHandler = new LaunchFinishedEventHandler(projectRepository,
			launchRepository,
			launchFinishedEventSubscribers
	);

	@Test
	void shouldNotSendWhenLaunchInDebug() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEBUG);

		when(launchRepository.findById(event.getLaunchActivityResource().getId())).thenReturn(launch);

		launchFinishedEventHandler.onApplicationEvent(event);

		verify(projectRepository, times(0)).findById(launch.get().getId());
	}

	@Test
	void shouldSendWhenAutoAnalyzedDisabledEnabled() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
		launch.setName("name1");

		Map<ProjectAttributeEnum, String> mapping = ImmutableMap.<ProjectAttributeEnum, String>builder().put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED,
				"true"
		).put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED, "false").build();

		Project project = new Project();
		project.setId(1L);
		project.setProjectAttributes(LaunchFinishedTestUtils.getProjectAttributes(mapping));
		project.setSenderCases(LaunchFinishedTestUtils.getSenderCases());

		when(launchRepository.findById(event.getLaunchActivityResource().getId())).thenReturn(Optional.ofNullable(launch));
		when(projectRepository.findById(resource.getProjectId())).thenReturn(Optional.ofNullable(project));

		launchFinishedEventHandler.onApplicationEvent(event);
		verify(autoAnalysisSubscriber, times(1)).handleEvent(event, project, launch);
		verify(notificationSubscriber, times(1)).handleEvent(event, project, launch);

	}

}