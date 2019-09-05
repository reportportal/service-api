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

import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.util.LaunchFinishedTestUtils;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchPatternAnalysisSubscriberTest {

	private final PatternAnalyzer patternAnalyzer = mock(PatternAnalyzer.class);

	private final LaunchPatternAnalysisSubscriber patternAnalysisSubscriber = new LaunchPatternAnalysisSubscriber(patternAnalyzer);

	@Test
	public void shouldAnalyzeWhenEnabled() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

		Map<ProjectAttributeEnum, String> mapping = ImmutableMap.<ProjectAttributeEnum, String>builder().put(ProjectAttributeEnum.PATTERN_ANALYSIS_ENABLED,
				"true"
		).build();
		Project project = new Project();
		project.setId(1L);
		project.setProjectAttributes(LaunchFinishedTestUtils.getProjectAttributes(mapping));

		patternAnalysisSubscriber.handleEvent(event, project, launch.get());

		verify(patternAnalyzer, times(1)).analyzeTestItems(launch.get(), Collections.singleton(AnalyzeItemsMode.TO_INVESTIGATE));

	}

	@Test
	public void shouldNotAnalyzeWhenDisabled() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

		Map<ProjectAttributeEnum, String> mapping = ImmutableMap.<ProjectAttributeEnum, String>builder().put(ProjectAttributeEnum.PATTERN_ANALYSIS_ENABLED,
				"false"
		).build();
		Project project = new Project();
		project.setId(1L);
		project.setProjectAttributes(LaunchFinishedTestUtils.getProjectAttributes(mapping));

		patternAnalysisSubscriber.handleEvent(event, project, launch.get());

		verify(patternAnalyzer, times(0)).analyzeTestItems(launch.get(), Sets.newHashSet());

	}
}