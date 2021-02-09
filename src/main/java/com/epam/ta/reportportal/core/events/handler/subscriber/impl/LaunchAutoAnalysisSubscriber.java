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

import com.epam.reportportal.extension.event.LaunchAutoAnalysisFinishEvent;
import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerServiceAsync;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.subscriber.LaunchFinishedEventSubscriber;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchAutoAnalysisSubscriber implements LaunchFinishedEventSubscriber {

	private final AnalyzerServiceAsync analyzerServiceAsync;
	private final AnalyzeCollectorFactory analyzeCollectorFactory;
	private final LogIndexer logIndexer;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public LaunchAutoAnalysisSubscriber(AnalyzerServiceAsync analyzerServiceAsync, AnalyzeCollectorFactory analyzeCollectorFactory,
			LogIndexer logIndexer, ApplicationEventPublisher eventPublisher) {
		this.analyzerServiceAsync = analyzerServiceAsync;
		this.analyzeCollectorFactory = analyzeCollectorFactory;
		this.logIndexer = logIndexer;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void handleEvent(LaunchFinishedEvent launchFinishedEvent, Project project, Launch launch) {
		AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(project);
		if (BooleanUtils.isTrue(analyzerConfig.getIsAutoAnalyzerEnabled())) {
			List<Long> itemIds = analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)
					.collectItems(project.getId(), launch.getId(), launchFinishedEvent.getUser());
			logIndexer.indexLaunchLogs(project.getId(), launch.getId(), analyzerConfig).join();
			analyzerServiceAsync.analyze(launch, itemIds, analyzerConfig).join();

			//TODO provide executor
			CompletableFuture.supplyAsync(() -> logIndexer.indexItemsLogs(project.getId(), launch.getId(), itemIds, analyzerConfig));
		} else {
			logIndexer.indexLaunchLogs(project.getId(), launch.getId(), analyzerConfig);
		}
		eventPublisher.publishEvent(new LaunchAutoAnalysisFinishEvent(launch.getId()));
	}

	@Override
	public int getOrder() {
		return 1;
	}
}
