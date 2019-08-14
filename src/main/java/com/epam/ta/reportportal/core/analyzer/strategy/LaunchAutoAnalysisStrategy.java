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
import com.epam.ta.reportportal.core.analyzer.auto.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final AnalyzerServiceAsync analyzerServiceAsync;
	private final AnalyzeCollectorFactory analyzeCollectorFactory;
	private final LogIndexer logIndexer;

	@Autowired
	public LaunchAutoAnalysisStrategy(ProjectRepository projectRepository, LaunchRepository launchRepository,
			AnalyzerServiceAsync analyzerServiceAsync, AnalyzeCollectorFactory analyzeCollectorFactory, LogIndexer logIndexer) {
		super(projectRepository, launchRepository);
		this.analyzerServiceAsync = analyzerServiceAsync;
		this.analyzeCollectorFactory = analyzeCollectorFactory;
		this.logIndexer = logIndexer;
	}

	public void analyze(ReportPortalUser.ProjectDetails projectDetails, AnalyzeLaunchRQ analyzeRQ) {
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

		List<Long> itemIds = collectItemsByModes(project, launch.getId(), analyzeRQ.getAnalyzeItemsModes());

		analyzerServiceAsync.analyze(launch, itemIds, analyzerConfig)
				.thenApply(it -> logIndexer.indexItemsLogs(project.getId(), launch.getId(), itemIds, analyzerConfig));
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
	 * @see com.epam.ta.reportportal.core.analyzer.auto.strategy.AnalyzeItemsCollector
	 */
	private List<Long> collectItemsByModes(Project project, Long launchId, List<String> analyzeItemsMode) {
		return analyzeItemsMode.stream()
				.map(AnalyzeItemsMode::fromString)
				.flatMap(it -> analyzeCollectorFactory.getCollector(it).collectItems(project.getId(), launchId).stream())
				.distinct()
				.collect(toList());
	}
}
