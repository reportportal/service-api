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
import com.epam.ta.reportportal.core.events.handler.subscriber.LaunchFinishedEventSubscriber;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchPatternAnalysisSubscriber implements LaunchFinishedEventSubscriber {

	private final PatternAnalyzer patternAnalyzer;

	@Autowired
	public LaunchPatternAnalysisSubscriber(PatternAnalyzer patternAnalyzer) {
		this.patternAnalyzer = patternAnalyzer;
	}

	@Override
	public void handleEvent(LaunchFinishedEvent launchFinishedEvent, Project project, Launch launch) {

		boolean isPatternAnalysisEnabled = BooleanUtils.toBoolean(ProjectUtils.getConfigParameters(project.getProjectAttributes())
				.get(ProjectAttributeEnum.AUTO_PATTERN_ANALYZER_ENABLED.getAttribute()));

		if (isPatternAnalysisEnabled) {
			patternAnalyzer.analyzeTestItems(launch, Collections.singleton(AnalyzeItemsMode.TO_INVESTIGATE));
		}
	}

	@Override
	public int getOrder() {
		return 3;
	}
}
