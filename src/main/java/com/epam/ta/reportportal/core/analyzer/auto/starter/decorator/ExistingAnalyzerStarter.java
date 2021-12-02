package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ExistingAnalyzerStarter implements LaunchAutoAnalysisStarter {

	private final AnalyzerService analyzerService;
	private final LaunchAutoAnalysisStarter launchAutoAnalysisStarter;

	public ExistingAnalyzerStarter(AnalyzerService analyzerService, LaunchAutoAnalysisStarter launchAutoAnalysisStarter) {
		this.analyzerService = analyzerService;
		this.launchAutoAnalysisStarter = launchAutoAnalysisStarter;
	}

	@Override
	public void start(StartLaunchAutoAnalysisConfig config) {
		expect(analyzerService.hasAnalyzers(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer services are deployed."
		);
		launchAutoAnalysisStarter.start(config);
	}
}
