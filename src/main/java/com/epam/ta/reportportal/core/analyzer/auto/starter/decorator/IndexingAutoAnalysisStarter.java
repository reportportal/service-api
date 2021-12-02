package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class IndexingAutoAnalysisStarter implements LaunchAutoAnalysisStarter {

	private final GetLaunchHandler getLaunchHandler;
	private final LogIndexer logIndexer;
	private final LaunchAutoAnalysisStarter launchAutoAnalysisStarter;

	public IndexingAutoAnalysisStarter(GetLaunchHandler getLaunchHandler, LogIndexer logIndexer,
			LaunchAutoAnalysisStarter launchAutoAnalysisStarter) {
		this.getLaunchHandler = getLaunchHandler;
		this.logIndexer = logIndexer;
		this.launchAutoAnalysisStarter = launchAutoAnalysisStarter;
	}

	@Override
	@Transactional
	public void start(StartLaunchAutoAnalysisConfig config) {
		final Launch launch = getLaunchHandler.get(config.getLaunchId());
		logIndexer.indexLaunchLogs(launch, config.getAnalyzerConfig());
		launchAutoAnalysisStarter.start(config);
	}
}
