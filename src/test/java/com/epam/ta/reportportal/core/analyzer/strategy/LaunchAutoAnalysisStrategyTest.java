/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.strategy;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerServiceAsync;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.AnalyzeItemsCollector;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@ExtendWith(MockitoExtension.class)
class LaunchAutoAnalysisStrategyTest {
	private final Launch launch = mock(Launch.class);
	private final Project project = mock(Project.class);

	@Mock
	private CompletableFuture<Void> completableFuture;

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final LaunchRepository launchRepository = mock(LaunchRepository.class);
	private final AnalyzerServiceAsync analyzerServiceAsync = mock(AnalyzerServiceAsync.class);
	private final AnalyzeCollectorFactory analyzeCollectorFactory = mock(AnalyzeCollectorFactory.class);
	private final AnalyzeItemsCollector analyzeItemsCollector = mock(AnalyzeItemsCollector.class);
	private final LogIndexer logIndexer = mock(LogIndexer.class);

	private final LaunchAutoAnalysisStrategy launchAutoAnalysisStrategy = new LaunchAutoAnalysisStrategy(projectRepository,
			launchRepository,
			analyzerServiceAsync,
			analyzeCollectorFactory,
			logIndexer
	);

	@Test
	void analyzeTest() {

		when(analyzerServiceAsync.hasAnalyzers()).thenReturn(true);

		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));
		when(launch.getId()).thenReturn(1L);
		when(launch.getProjectId()).thenReturn(1L);
		when(launch.getMode()).thenReturn(LaunchModeEnum.DEFAULT);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(project.getId()).thenReturn(1L);

		when(project.getProjectAttributes()).thenReturn(Sets.newHashSet());
		when(analyzeCollectorFactory.getCollector(any(AnalyzeItemsMode.class))).thenReturn(analyzeItemsCollector);
		when(analyzeItemsCollector.collectItems(1L, 1L)).thenReturn(Lists.newArrayList());

		ReportPortalUser.ProjectDetails projectDetails = new ReportPortalUser.ProjectDetails(1L, "name", ProjectRole.PROJECT_MANAGER);
		AnalyzeLaunchRQ analyzeLaunchRQ = new AnalyzeLaunchRQ();
		analyzeLaunchRQ.setLaunchId(1L);
		analyzeLaunchRQ.setAnalyzerHistoryMode("ALL");
		analyzeLaunchRQ.setAnalyzeItemsModes(Lists.newArrayList("TO_INVESTIGATE"));
		analyzeLaunchRQ.setAnalyzerTypeName("patternAnalyzer");
		when(analyzerServiceAsync.analyze(any(Launch.class), anyList(), any(AnalyzerConfig.class))).thenReturn(completableFuture);
		launchAutoAnalysisStrategy.analyze(projectDetails, analyzeLaunchRQ);
		verify(analyzerServiceAsync, times(1)).analyze(any(Launch.class), anyList(), any(AnalyzerConfig.class));

	}
}