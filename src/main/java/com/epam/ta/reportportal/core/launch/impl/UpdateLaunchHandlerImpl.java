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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceAsync;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.impl.LaunchPreparerService;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.launch.UpdateLaunchHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ItemInfoUtils;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter;
import com.epam.ta.reportportal.ws.model.BulkInfoUpdateRQ;
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

import java.util.List;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link UpdateLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateLaunchHandlerImpl implements UpdateLaunchHandler {

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final LogRepository logRepository;

	private final ProjectRepository projectRepository;

	private final AnalyzerServiceAsync analyzerServiceAsync;

	private final LogIndexer logIndexer;

	private final LaunchPreparerService launchPreparerService;

	private final AnalyzeCollectorFactory analyzeCollectorFactory;

	@Autowired
	public UpdateLaunchHandlerImpl(LaunchRepository launchRepository, TestItemRepository testItemRepository, LogRepository logRepository,
			ProjectRepository projectRepository, AnalyzerServiceAsync analyzerServiceAsync, LogIndexer logIndexer,
			LaunchPreparerService launchPreparerService, AnalyzeCollectorFactory analyzeCollectorFactory) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
		this.projectRepository = projectRepository;
		this.analyzerServiceAsync = analyzerServiceAsync;
		this.logIndexer = logIndexer;
		this.launchPreparerService = launchPreparerService;
		this.analyzeCollectorFactory = analyzeCollectorFactory;
	}

	@Override
	public OperationCompletionRS updateLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			UpdateLaunchRQ rq) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectName()));
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));
		validate(launch, user, projectDetails, rq.getMode());
		launch = new LaunchBuilder(launch).addMode(rq.getMode())
				.addDescription(rq.getDescription())
				.overwriteAttributes(rq.getAttributes())
				.get();
		launchRepository.save(launch);
		reindexLogs(launch, AnalyzerUtils.getAnalyzerConfig(project), project.getId());
		return new OperationCompletionRS("Launch with ID = '" + launch.getId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> updateLaunch(BulkRQ<Long, UpdateLaunchRQ> rq, ReportPortalUser.ProjectDetails projectDetails,
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
		expect(analyzerServiceAsync.hasAnalyzers(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
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

		List<Long> itemIds = collectItemsByModes(project, user.getUsername(), launch.getId(), analyzeRQ.getAnalyzeItemsMode());

		analyzerServiceAsync.analyze(launch, itemIds, analyzerConfig)
				.thenApply(it -> logIndexer.indexItemsLogs(project.getId(), launch.getId(), itemIds, analyzerConfig));

		return new OperationCompletionRS("Auto-analyzer for launch ID='" + launch.getId() + "' started.");
	}

	@Override
	public OperationCompletionRS bulkInfoUpdate(BulkInfoUpdateRQ bulkUpdateRq, ReportPortalUser.ProjectDetails projectDetails) {
		expect(projectRepository.existsById(projectDetails.getProjectId()), Predicate.isEqual(true)).verify(PROJECT_NOT_FOUND,
				projectDetails.getProjectId()
		);

		List<Launch> launches = launchRepository.findAllById(bulkUpdateRq.getIds());
		launches.forEach(it -> ItemInfoUtils.updateDescription(bulkUpdateRq.getDescription(), it.getDescription())
				.ifPresent(it::setDescription));

		bulkUpdateRq.getAttributes().forEach(it -> {
			switch (it.getAction()) {
				case DELETE: {
					launches.forEach(launch -> {
						ItemAttribute toDelete = ItemInfoUtils.findAttributeByResource(launch.getAttributes(), it.getFrom());
						launch.getAttributes().remove(toDelete);
					});
					break;
				}
				case UPDATE: {
					launches.forEach(launch -> ItemInfoUtils.updateAttribute(launch.getAttributes(), it));
					break;
				}
				case CREATE: {
					launches.stream()
							.filter(launch -> ItemInfoUtils.containsAttribute(launch.getAttributes(), it.getTo()))
							.forEach(launch -> {
								ItemAttribute itemAttribute = ItemAttributeConverter.FROM_RESOURCE.apply(it.getTo());
								itemAttribute.setLaunch(launch);
								launch.getAttributes().add(itemAttribute);
							});
					break;
				}
			}
		});

		return new OperationCompletionRS("Attributes successfully updated");
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
		List<TestItem> items = testItemRepository.findAllNotInIssueGroupByLaunch(launch.getId(), TestItemIssueGroup.TO_INVESTIGATE);
		if (!CollectionUtils.isEmpty(items)) {
			if (LaunchModeEnum.DEBUG.equals(launch.getMode())) {
				logIndexer.cleanIndex(projectId,
						logRepository.findIdsByTestItemIds(items.stream().map(TestItem::getItemId).collect(toList()))
				);
			} else {
				launchPreparerService.prepare(launch, items, analyzerConfig).ifPresent(it -> logIndexer.indexPreparedLogs(projectId, it));
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