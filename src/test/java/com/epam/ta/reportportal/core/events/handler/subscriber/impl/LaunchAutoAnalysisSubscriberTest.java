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

package com.epam.ta.reportportal.core.events.handler.subscriber.impl;

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerServiceAsync;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsCollector;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.util.LaunchFinishedTestUtils;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchAutoAnalysisSubscriberTest {

	private final AnalyzerServiceAsync analyzerServiceAsync = mock(AnalyzerServiceAsync.class);
	private final AnalyzeCollectorFactory analyzeCollectorFactory = mock(AnalyzeCollectorFactory.class);
	private final AnalyzeItemsCollector analyzeItemsCollector = mock(AnalyzeItemsCollector.class);
	private final LogIndexer logIndexer = mock(LogIndexer.class);

	private CompletableFuture<Long> indexed = mock(CompletableFuture.class);
	private CompletableFuture<Void> analyzed = mock(CompletableFuture.class);

	private final LaunchAutoAnalysisSubscriber autoAnalysisSubscriber = new LaunchAutoAnalysisSubscriber(analyzerServiceAsync,
			analyzeCollectorFactory,
			logIndexer
	);

	//	AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(project);
	//		if (BooleanUtils.isTrue(analyzerConfig.getIsAutoAnalyzerEnabled()) && analyzerServiceAsync.hasAnalyzers()) {
	//		List<Long> itemIds = analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)
	//				.collectItems(project.getId(), launch.getId(), null);
	//		logIndexer.indexLaunchLogs(project.getId(), launch.getId(), analyzerConfig).join();
	//		analyzerServiceAsync.analyze(launch, itemIds, analyzerConfig).join();
	//		CompletableFuture.supplyAsync(() -> logIndexer.indexItemsLogs(project.getId(), launch.getId(), itemIds, analyzerConfig));
	//	} else {
	//		logIndexer.indexLaunchLogs(project.getId(), launch.getId(), analyzerConfig);
	//	}

	@Test
	void shouldAnalyzeWhenEnabled() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

		Map<ProjectAttributeEnum, String> mapping = ImmutableMap.<ProjectAttributeEnum, String>builder().put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED,
				"true"
		).build();
		Project project = new Project();
		project.setId(1L);
		project.setProjectAttributes(LaunchFinishedTestUtils.getProjectAttributes(mapping));

		when(analyzerServiceAsync.hasAnalyzers()).thenReturn(true);
		when(analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)).thenReturn(analyzeItemsCollector);
		when(analyzeItemsCollector.collectItems(any(), any(), any())).thenReturn(Lists.newArrayList(1L, 2L));
		when(logIndexer.indexLaunchLogs(any(), any(), any())).thenReturn(indexed);
		when(analyzerServiceAsync.analyze(any(), any(), any())).thenReturn(analyzed);
		autoAnalysisSubscriber.handleEvent(event, project, launch.get());

		verify(logIndexer, times(1)).indexLaunchLogs(any(), any(), any());
		verify(analyzerServiceAsync, times(1)).analyze(any(), any(), any());

	}

	@Test
	void shouldNotAnalyzeWhenDisabled() {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent(resource, 1L, "user");

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

		Map<ProjectAttributeEnum, String> mapping = ImmutableMap.<ProjectAttributeEnum, String>builder().put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED,
				"false"
		).build();
		Project project = new Project();
		project.setId(1L);
		project.setProjectAttributes(LaunchFinishedTestUtils.getProjectAttributes(mapping));

		autoAnalysisSubscriber.handleEvent(event, project, launch.get());

		verify(analyzerServiceAsync, times(0)).analyze(any(), any(), any());
		verify(logIndexer, times(1)).indexLaunchLogs(any(), any(), any());

	}

}