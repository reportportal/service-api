/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.IssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.core.launch.util.AttributesValidator.validateAttributes;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.UpdateLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateLaunchHandler implements com.epam.ta.reportportal.core.launch.UpdateLaunchHandler {

	private LaunchRepository launchRepository;

	private TestItemRepository testItemRepository;

	private ProjectRepository projectRepository;

	private IssuesAnalyzer analyzerService;

	private LogIndexer logIndexer;

	@Autowired
	private AnalyzeCollectorFactory analyzeCollectorFactory;

	@Autowired
	public UpdateLaunchHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ProjectRepository projectRepository, IssuesAnalyzer analyzerService, LogIndexer logIndexer) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.projectRepository = projectRepository;
		this.analyzerService = analyzerService;
		this.logIndexer = logIndexer;
	}

	@Override
	public OperationCompletionRS updateLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			UpdateLaunchRQ rq) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectName()));
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));
		validate(launch, user, projectDetails, rq.getMode());
		validateAttributes(rq.getAttributes());
		launch = new LaunchBuilder(launch).addMode(rq.getMode())
				.addDescription(rq.getDescription())
				.overwriteAttributes(rq.getAttributes())
				.get();
		reindexLogs(launch, AnalyzerUtils.getAnalyzerConfig(project), project.getId());
		launchRepository.save(launch);
		return new OperationCompletionRS("Launch with ID = '" + launch.getId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> updateLaunch(BulkRQ<UpdateLaunchRQ> rq, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		return rq.getEntities()
				.entrySet()
				.stream()
				.map(entry -> updateLaunch(entry.getKey(), projectDetails, user, entry.getValue()))
				.collect(toList());
	}

	@Override
	public OperationCompletionRS startLaunchAnalyzer(ReportPortalUser.ProjectDetails projectDetails, AnalyzeLaunchRQ analyzeRQ,
			ReportPortalUser user) {
		expect(analyzerService.hasAnalyzers(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer services are deployed."
		);

		AnalyzeMode analyzeMode = AnalyzeMode.fromString(analyzeRQ.getAnalyzerHistoryMode())
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, analyzeRQ.getAnalyzeItemsMode()));

		Launch launch = launchRepository.findById(analyzeRQ.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, analyzeRQ.getLaunchId()));

		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				Suppliers.formattedSupplier("Launch with ID '{}' is not under '{}' project.",
						analyzeRQ.getLaunchId(),
						projectDetails.getProjectName()
				)
		);

		/* Do not process debug launches */
		expect(launch.getMode(), equalTo(LaunchModeEnum.DEFAULT)).verify(INCORRECT_REQUEST, "Cannot analyze launches in debug mode.");

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		AnalyzerConfig analyzerConfig = getAnalyzerConfig(project);
		analyzerConfig.setAnalyzerMode(analyzeMode.getValue());

		List<Long> items = collectItemsByModes(project, user.getUsername(), launch.getId(), analyzeRQ.getAnalyzeItemsMode());

		analyzerService.analyze(launch, items, analyzerConfig);

		return new OperationCompletionRS("Auto-analyzer for launch ID='" + launch.getId() + "' started.");
	}

	/**
	 * Collect item ids for analyzer according to provided analyzer configuration.
	 *
	 * @param project          Project
	 * @param username         Username
	 * @param launchId         Launch id
	 * @param analyzeItemsMode {@link AnalyzeItemsMode}
	 * @return List of ids
	 * @see AnalyzeItemsMode
	 * @see AnalyzeCollectorFactory
	 * @see com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsCollector
	 */
	private List<Long> collectItemsByModes(Project project, String username, Long launchId, List<String> analyzeItemsMode) {
		return analyzeItemsMode.stream()
				.map(AnalyzeItemsMode::fromString)
				.flatMap(it -> analyzeCollectorFactory.getCollector(it).collectItems(project.getId(), launchId, username).stream())
				.distinct()
				.collect(toList());
	}

	/**
	 * If launch mode has changed - reindex items
	 *
	 * @param launch Update launch
	 */
	private void reindexLogs(Launch launch, AnalyzerConfig analyzerConfig, Long projectId) {
		List<Long> items = testItemRepository.selectIdsNotInIssueByLaunch(launch.getId(), TestItemIssueGroup.TO_INVESTIGATE.getLocator())
				.stream()
				.map(TestItem::getItemId)
				.collect(toList());
		if (!CollectionUtils.isEmpty(items)) {
			if (Mode.DEBUG.name().equals(launch.getMode().name())) {
				logIndexer.cleanIndex(projectId, items);
			} else {
				logIndexer.indexLogs(Collections.singletonList(launch.getId()), analyzerConfig);
			}
		}
	}

	/**
	 * Valide {@link ReportPortalUser} credentials
	 *
	 * @param launch {@link Launch}
	 * @param user   {@link ReportPortalUser}
	 * @param mode   {@link Launch#mode}
	 */
	private void validate(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, Mode mode) {
		if (projectDetails.getProjectRole() == ProjectRole.CUSTOMER && null != mode) {
			expect(mode, equalTo(Mode.DEFAULT)).verify(ACCESS_DENIED);
		}
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
			if (projectDetails.getProjectRole().lowerThan(PROJECT_MANAGER)) {
				expect(user.getUsername(), Predicate.isEqual(launch.getUser().getLogin())).verify(ACCESS_DENIED);
			}
		}
	}

}