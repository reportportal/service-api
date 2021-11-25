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

package com.epam.ta.reportportal.core.events.handler.launch;

import com.epam.reportportal.extension.event.LaunchAutoAnalysisFinishEvent;
import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchAutoAnalysisRunner implements ConfigurableEventHandler<LaunchFinishedEvent, Map<String, String>> {

	private final GetLaunchHandler getLaunchHandler;
	private final AnalyzerService analyzerService;
	private final AnalyzeCollectorFactory analyzeCollectorFactory;
	private final LogIndexer logIndexer;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public LaunchAutoAnalysisRunner(GetLaunchHandler getLaunchHandler, AnalyzerService analyzerService, AnalyzeCollectorFactory analyzeCollectorFactory,
			LogIndexer logIndexer, ApplicationEventPublisher eventPublisher) {
		this.getLaunchHandler = getLaunchHandler;
		this.analyzerService = analyzerService;
		this.analyzeCollectorFactory = analyzeCollectorFactory;
		this.logIndexer = logIndexer;
		this.eventPublisher = eventPublisher;
	}

	@Override
	@Transactional
	public void handle(LaunchFinishedEvent launchFinishedEvent, Map<String, String> projectConfig) {
		final Launch launch = getLaunchHandler.get(launchFinishedEvent.getId());

		final AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(projectConfig);
		logIndexer.indexLaunchLogs(launch, analyzerConfig);
		if (BooleanUtils.isTrue(analyzerConfig.getIsAutoAnalyzerEnabled())) {
			final List<Long> itemIds = analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)
					.collectItems(launch.getProjectId(), launch.getId(), launchFinishedEvent.getUser());

			analyzerService.runAnalyzers(launch, itemIds, analyzerConfig);
			logIndexer.indexItemsLogs(launch.getProjectId(), launch.getId(), itemIds, analyzerConfig);
		}
		eventPublisher.publishEvent(new LaunchAutoAnalysisFinishEvent(launch.getId()));
	}

}
