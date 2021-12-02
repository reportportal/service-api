/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.auto.starter;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsCollector;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class CollectingAutoAnalysisStarterTest {

	public static final Long INDEXED_LOG_COUNT = 5L;

	private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
	private final AnalyzeCollectorFactory analyzeCollectorFactory = mock(AnalyzeCollectorFactory.class);
	private final AnalyzeItemsCollector analyzeItemsCollector = mock(AnalyzeItemsCollector.class);
	private final AnalyzerService analyzerService = mock(AnalyzerService.class);
	private final LogIndexer logIndexer = mock(LogIndexer.class);

	private final CollectingAutoAnalysisStarter starter = new CollectingAutoAnalysisStarter(getLaunchHandler,
			analyzeCollectorFactory,
			analyzerService,
			logIndexer
	);

	@Test
	void shouldAnalyze() {

		final Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
		final ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, launch.getProjectId());
		final LaunchFinishedEvent event = new LaunchFinishedEvent(launch, user, "baseUrl");

		final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setIsAutoAnalyzerEnabled(true);
		final StartLaunchAutoAnalysisConfig startLaunchAutoAnalysisConfig = StartLaunchAutoAnalysisConfig.of(launch.getId(),
				analyzerConfig,
				Set.of(AnalyzeItemsMode.TO_INVESTIGATE),
				user
		);

		when(analyzerService.hasAnalyzers()).thenReturn(true);

		when(getLaunchHandler.get(event.getId())).thenReturn(launch);
		when(logIndexer.indexLaunchLogs(eq(launch), any(AnalyzerConfig.class))).thenReturn(INDEXED_LOG_COUNT);

		when(analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)).thenReturn(analyzeItemsCollector);
		final List<Long> itemIds = Lists.newArrayList(1L, 2L);
		when(analyzeItemsCollector.collectItems(launch.getProjectId(), launch.getId(), user)).thenReturn(itemIds);

		starter.start(startLaunchAutoAnalysisConfig);

		verify(analyzerService, times(1)).runAnalyzers(eq(launch), eq(itemIds), any(AnalyzerConfig.class));
		verify(logIndexer, times(1)).indexItemsLogs(eq(launch.getProjectId()), eq(launch.getId()), eq(itemIds), any(AnalyzerConfig.class));
	}

}