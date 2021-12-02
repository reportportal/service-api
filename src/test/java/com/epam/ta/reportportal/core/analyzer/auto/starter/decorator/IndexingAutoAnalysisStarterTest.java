package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class IndexingAutoAnalysisStarterTest {

	private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
	private final LogIndexer logIndexer = mock(LogIndexer.class);
	private final LaunchAutoAnalysisStarter delegate = mock(LaunchAutoAnalysisStarter.class);

	private final IndexingAutoAnalysisStarter indexingAutoAnalysisStarter = new IndexingAutoAnalysisStarter(getLaunchHandler,
			logIndexer,
			delegate
	);

	@Test
	void shouldIndex() {
		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();

		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, 1L);
		final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(launch.getId(),
				new AnalyzerConfig(),
				Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
				user
		);

		when(getLaunchHandler.get(config.getLaunchId())).thenReturn(launch);

		indexingAutoAnalysisStarter.start(config);

		verify(logIndexer, times(1)).indexLaunchLogs(launch, config.getAnalyzerConfig());
	}

}