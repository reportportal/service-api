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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.launch.cluster.ClusterGenerator;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchUniqueErrorAnalysisRunnerTest {

	private final ClusterGenerator clusterGenerator = mock(ClusterGenerator.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

	private final LaunchUniqueErrorAnalysisRunner runner = new LaunchUniqueErrorAnalysisRunner(clusterGenerator, eventPublisher);

	@Test
	void shouldAnalyzeWhenEnabled() {

		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, launch.getProjectId());
		final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

		final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
				.put(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute(), "true")
				.put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(), "true")
				.build();

		runner.handle(event, projectConfig);

		final ArgumentCaptor<GenerateClustersConfig> configArgumentCaptor = ArgumentCaptor.forClass(GenerateClustersConfig.class);
		verify(clusterGenerator, times(1)).generate(configArgumentCaptor.capture());

		final GenerateClustersConfig config = configArgumentCaptor.getValue();

		assertEquals(event.getId(), config.getEntityContext().getLaunchId());
		assertEquals(event.getProjectId(), config.getEntityContext().getProjectId());
		assertFalse(config.isForUpdate());
		assertTrue(config.isCleanNumbers());
	}

	@Test
	void shouldNotAnalyzeWhenDisabled() {

		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, launch.getProjectId());
		final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

		final Map<String, String> projectConfig = ImmutableMap.<String, String>builder()
				.put(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute(), "false")
				.put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(), "true")
				.build();

		runner.handle(event, projectConfig);

		verify(clusterGenerator, times(0)).generate(any(GenerateClustersConfig.class));

	}

}