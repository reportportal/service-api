package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.Set;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class AsyncAutoAnalysisStarterTest {

	private final SyncTaskExecutor taskExecutor = mock(SyncTaskExecutor.class);
	private final LaunchAutoAnalysisStarter delegate = mock(LaunchAutoAnalysisStarter.class);

	private final AsyncAutoAnalysisStarter asyncAutoAnalysisStarter = new AsyncAutoAnalysisStarter(taskExecutor, delegate);

	@Test
	void shouldExecute() {
		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, 1L);
		final StartLaunchAutoAnalysisConfig config = StartLaunchAutoAnalysisConfig.of(1L,
				new AnalyzerConfig(),
				Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
				user
		);

		doCallRealMethod().when(taskExecutor).execute(any());

		asyncAutoAnalysisStarter.start(config);

		verify(delegate, times(1)).start(config);
	}

}