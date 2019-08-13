package com.epam.ta.reportportal.core.events.handler.subscriber.impl;

import com.epam.ta.reportportal.core.analyzer.auto.strategy.AnalyzeItemsMode;
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
				.get(ProjectAttributeEnum.PATTERN_ANALYSIS_ENABLED.getAttribute()));

		if (isPatternAnalysisEnabled) {
			patternAnalyzer.analyzeTestItems(launch, Collections.singleton(AnalyzeItemsMode.TO_INVESTIGATE));
		}
	}

	@Override
	public int getOrder() {
		return 3;
	}
}
