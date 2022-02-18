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
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchPatternAnalysisRunnerTest {

	private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
	private final PatternAnalyzer patternAnalyzer = mock(PatternAnalyzer.class);

	private final LaunchPatternAnalysisRunner runner = new LaunchPatternAnalysisRunner(getLaunchHandler, patternAnalyzer);

	@Test
	public void shouldAnalyzeWhenEnabled() {

		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, launch.getProjectId());
		final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

		final Map<String, String> mapping = ImmutableMap.<String, String>builder()
				.put(ProjectAttributeEnum.AUTO_PATTERN_ANALYZER_ENABLED.getAttribute(), "true")
				.build();

		when(getLaunchHandler.get(event.getId())).thenReturn(launch);
		runner.handle(event, mapping);

		verify(patternAnalyzer, times(1)).analyzeTestItems(launch, Collections.singleton(AnalyzeItemsMode.TO_INVESTIGATE));

	}

	@Test
	public void shouldNotAnalyzeWhenDisabled() {

		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, launch.getProjectId());
		final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

		final Map<String, String> mapping = ImmutableMap.<String, String>builder()
				.put(ProjectAttributeEnum.AUTO_PATTERN_ANALYZER_ENABLED.getAttribute(), "false")
				.build();

		runner.handle(event, mapping);

		verify(getLaunchHandler, times(0)).get(event.getId());
		verify(patternAnalyzer, times(0)).analyzeTestItems(launch, Collections.singleton(AnalyzeItemsMode.TO_INVESTIGATE));

	}
}