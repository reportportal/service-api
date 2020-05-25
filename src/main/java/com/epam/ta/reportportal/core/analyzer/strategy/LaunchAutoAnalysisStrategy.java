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
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsCollector;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.events.AnalysisEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchAutoAnalysisStrategy extends AbstractLaunchAnalysisStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLaunchAnalysisStrategy.class);

	private final AnalyzerServiceAsync analyzerServiceAsync;
	private final AnalyzeCollectorFactory analyzeCollectorFactory;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public LaunchAutoAnalysisStrategy(ProjectRepository projectRepository, LaunchRepository launchRepository,
			AnalyzerServiceAsync analyzerServiceAsync, AnalyzeCollectorFactory analyzeCollectorFactory,
			ApplicationEventPublisher eventPublisher) {
		super(projectRepository, launchRepository);
		this.analyzerServiceAsync = analyzerServiceAsync;
		this.analyzeCollectorFactory = analyzeCollectorFactory;
		this.eventPublisher = eventPublisher;
	}

	public void analyze(AnalyzeLaunchRQ analyzeRQ, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		expect(analyzerServiceAsync.hasAnalyzers(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer services are deployed."
		);

		AnalyzeMode analyzeMode = AnalyzeMode.fromString(analyzeRQ.getAnalyzerHistoryMode())
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, analyzeRQ.getAnalyzerHistoryMode()));

		Launch launch = launchRepository.findById(analyzeRQ.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, analyzeRQ.getLaunchId()));
		validateLaunch(launch, projectDetails);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		AnalyzerConfig analyzerConfig = getAnalyzerConfig(project);
		analyzerConfig.setAnalyzerMode(analyzeMode.getValue());

		List<Long> itemIds = collectItemsByModes(launch.getId(), analyzeRQ.getAnalyzeItemsModes(), project, user);

		eventPublisher.publishEvent(new AnalysisEvent(launch, itemIds, analyzerConfig));
	}

	/**
	 * Collect item ids for analyzer according to provided analyzer configuration.
	 *
	 * @param project          Project
	 * @param launchId         Launch id
	 * @param analyzeItemsMode {@link AnalyzeItemsMode}
	 * @return List of ids
	 * @see AnalyzeItemsMode
	 * @see AnalyzeCollectorFactory
	 * @see AnalyzeItemsCollector
	 */
	private List<Long> collectItemsByModes(Long launchId, List<String> analyzeItemsMode, Project project, ReportPortalUser user) {
		return analyzeItemsMode.stream().map(AnalyzeItemsMode::fromString).flatMap(it -> {
			List<Long> itemIds = analyzeCollectorFactory.getCollector(it).collectItems(project.getId(), launchId, user);
			LOGGER.debug("Item itemIds collected by '{}' mode: {}", it, itemIds);
			return itemIds.stream();
		}).distinct().collect(toList());
	}
}
