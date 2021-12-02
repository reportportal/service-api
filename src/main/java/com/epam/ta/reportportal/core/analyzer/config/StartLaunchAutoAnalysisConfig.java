package com.epam.ta.reportportal.core.analyzer.config;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class StartLaunchAutoAnalysisConfig {
	private final Long launchId;
	private final AnalyzerConfig analyzerConfig;
	private final Set<AnalyzeItemsMode> analyzeItemsModes;
	private final ReportPortalUser user;

	private StartLaunchAutoAnalysisConfig(Long launchId, AnalyzerConfig analyzerConfig, Set<AnalyzeItemsMode> analyzeItemsModes,
			ReportPortalUser user) {
		this.launchId = launchId;
		this.analyzerConfig = analyzerConfig;
		this.analyzeItemsModes = analyzeItemsModes;
		this.user = user;
	}

	public static StartLaunchAutoAnalysisConfig of(Long launchId, AnalyzerConfig analyzerConfig, Set<AnalyzeItemsMode> analyzeItemsModes,
			ReportPortalUser user) {
		return new StartLaunchAutoAnalysisConfig(launchId, analyzerConfig, analyzeItemsModes, user);
	}

	public Long getLaunchId() {
		return launchId;
	}

	public AnalyzerConfig getAnalyzerConfig() {
		return analyzerConfig;
	}

	public Set<AnalyzeItemsMode> getAnalyzeItemsModes() {
		return analyzeItemsModes;
	}

	public ReportPortalUser getUser() {
		return user;
	}
}
