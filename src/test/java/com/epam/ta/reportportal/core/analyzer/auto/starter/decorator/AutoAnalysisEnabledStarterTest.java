package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class AutoAnalysisEnabledStarterTest {

	private final LaunchAutoAnalysisStarter delegate = mock(LaunchAutoAnalysisStarter.class);
	private final AutoAnalysisEnabledStarter autoAnalysisEnabledStarter = new AutoAnalysisEnabledStarter(delegate);

	@Test
	void shouldRunWhenAutoAnalysisEnabled() {

		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, 1L);
		final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setIsAutoAnalyzerEnabled(true);

		final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(1L,
				analyzerConfig,
				Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
				user
		);

		autoAnalysisEnabledStarter.start(config);

		verify(delegate, times(1)).start(config);
	}

	@Test
	void shouldNotRunWhenAutoAnalysisDisabled() {
		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, 1L);
		final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setIsAutoAnalyzerEnabled(false);

		final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(1L,
				analyzerConfig,
				Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
				user
		);

		autoAnalysisEnabledStarter.start(config);

		verify(delegate, times(0)).start(config);
	}

}