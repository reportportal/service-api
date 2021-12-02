package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import org.springframework.core.task.TaskExecutor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class AsyncAutoAnalysisStarter implements LaunchAutoAnalysisStarter {

	private final TaskExecutor executor;
	private final LaunchAutoAnalysisStarter launchAutoAnalysisStarter;

	public AsyncAutoAnalysisStarter(TaskExecutor executor, LaunchAutoAnalysisStarter launchAutoAnalysisStarter) {
		this.executor = executor;
		this.launchAutoAnalysisStarter = launchAutoAnalysisStarter;
	}

	@Override
	public void start(StartLaunchAutoAnalysisConfig config) {
		executor.execute(() -> launchAutoAnalysisStarter.start(config));
	}
}
