package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class AutoAnalysisEnabledStarter implements LaunchAutoAnalysisStarter {

	private final LaunchAutoAnalysisStarter launchAutoAnalysisStarter;

	public AutoAnalysisEnabledStarter(LaunchAutoAnalysisStarter launchAutoAnalysisStarter) {
		this.launchAutoAnalysisStarter = launchAutoAnalysisStarter;
	}

	@Override
	public void start(StartLaunchAutoAnalysisConfig config) {
		if (config.getAnalyzerConfig().getIsAutoAnalyzerEnabled()) {
			launchAutoAnalysisStarter.start(config);
		}
	}
}
