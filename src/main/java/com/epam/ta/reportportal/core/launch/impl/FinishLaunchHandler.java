/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.core.launch.IFinishLaunchHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.launch.LaunchTag;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.store.commons.EntityUtils.update;
import static com.epam.ta.reportportal.store.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.store.commons.Predicates.not;
import static com.epam.ta.reportportal.store.database.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link IFinishLaunchHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class FinishLaunchHandler implements IFinishLaunchHandler {
	private static final String LAUNCH_STOP_DESCRIPTION = " stopped";
	private static final String LAUNCH_STOP_TAG = "stopped";

	private LaunchRepository launchRepository;

	private TestItemRepository testItemRepository;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	//	@Autowired
	//	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Override
	public OperationCompletionRS finishLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, String projectName, String username) {
		//TODO validate roles

		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));
		validate(launch, finishLaunchRQ);
		if (!Strings.isNullOrEmpty(finishLaunchRQ.getDescription())) {
			launch.setDescription(finishLaunchRQ.getDescription());
		}
		if (!CollectionUtils.isEmpty(finishLaunchRQ.getTags())) {
			Set<String> tags = Sets.newHashSet(trimStrings(update(finishLaunchRQ.getTags())));
			launch.setTags(tags.stream().map(LaunchTag::new).collect(Collectors.toSet()));
		}
		Optional<StatusEnum> statusEnum = fromValue(finishLaunchRQ.getStatus());
		StatusEnum fromStatistics = PASSED;
		if (launchRepository.identifyStatus(launchId)) {
			fromStatistics = StatusEnum.FAILED;
		}
		StatusEnum fromStatisticsStatus = fromStatistics;
		statusEnum.ifPresent(providedStatus -> validateProvidedStatus(launch, providedStatus, fromStatisticsStatus));
		launch.setStatus(statusEnum.orElse(fromStatistics));
		launchRepository.save(launch);
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully finished.");
	}

	private void validateProvidedStatus(Launch launch, StatusEnum providedStatus, StatusEnum calculatedStatus) {
		/* Validate provided status */
		expect(providedStatus, not(Preconditions.statusIn(IN_PROGRESS, SKIPPED))).verify(INCORRECT_FINISH_STATUS,
				formattedSupplier("Cannot finish launch '{}' with status '{}'", launch.getId(), providedStatus)
		);
		if (PASSED.equals(providedStatus)) {
				/* Validate actual launch status */
			expect(launch.getStatus(), Preconditions.statusIn(IN_PROGRESS, PASSED)).verify(INCORRECT_FINISH_STATUS,
					formattedSupplier("Cannot finish launch '{}' with current status '{}' as 'PASSED'", launch.getId(), launch.getStatus())
			);
				/*
				 * Calculate status from launch statistics and validate it
				 */
			expect(calculatedStatus, Preconditions.statusIn(IN_PROGRESS, PASSED)).verify(INCORRECT_FINISH_STATUS,
					formattedSupplier("Cannot finish launch '{}' with calculated automatically status '{}' as 'PASSED'", launch.getId(),
							calculatedStatus
					)
			);
		}
	}

	@Override
	public OperationCompletionRS stopLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, String projectName, String userName) {
		//		Launch launch = launchRepository.findOne(launchId);
		//		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);
		//
		//		validateRoles(launch, userName, projectName);
		//
		//		expect(launch, not(Preconditions.LAUNCH_FINISHED)).verify(FINISH_LAUNCH_NOT_ALLOWED,
		//				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		//		);
		//
		//		launch.setEndTime(finishLaunchRQ.getEndTime());
		//		if (null != launch.getDescription()) {
		//			launch.setDescription(launch.getDescription().concat(LAUNCH_STOP_DESCRIPTION));
		//		} else {
		//			launch.setDescription(LAUNCH_STOP_DESCRIPTION);
		//		}
		//		Set<String> newTags = launch.getTags();
		//		if (null == newTags) {
		//			newTags = new HashSet<>();
		//		}
		//		newTags.add(LAUNCH_STOP_TAG);
		//		launch.setTags(newTags);
		//		launch.setStatus(fromValue(finishLaunchRQ.getStatus()).orElse(STOPPED));
		//		try {
		//			launchRepository.save(launch);
		//			if (launchRepository.hasItems(launch, IN_PROGRESS)) {
		//				// Find all IN_PROGRESS children and interrupt them
		//				List<TestItem> itemsInProgress = testItemRepository.findInStatusItems(IN_PROGRESS.name(), launch.getId());
		//				interruptItems(itemsInProgress);
		//			}
		//			retriesLaunchHandler.handleRetries(launch);
		//		} catch (Exception exp) {
		//			throw new ReportPortalException("Error while Launch updating.", exp);
		//		}
		//		eventPublisher.publishEvent(new LaunchFinishForcedEvent(launch, userName));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully stopped.");
	}

	@Override
	public List<OperationCompletionRS> stopLaunch(BulkRQ<FinishExecutionRQ> bulkRQ, String projectName, String userName) {
		return bulkRQ.getEntities()
				.entrySet()
				.stream()
				.map(entry -> stopLaunch(entry.getKey(), entry.getValue(), projectName, userName))
				.collect(toList());
	}

	private void validate(Launch launch, FinishExecutionRQ finishExecutionRQ) {
		expect(launch.getStatus(), equalTo(IN_PROGRESS)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		);

		expect(finishExecutionRQ, Preconditions.finishSameTimeOrLater(launch.getStartTime())).verify(FINISH_TIME_EARLIER_THAN_START_TIME,
				finishExecutionRQ.getEndTime(), launch.getStartTime(), launch.getId()
		);

		List<TestItem> items = testItemRepository.selectItemsInStatusByLaunch(launch.getId(), IN_PROGRESS);

		expect(items.isEmpty(), equalTo(true)).verify(FINISH_LAUNCH_NOT_ALLOWED, new Supplier<String>() {
			public String get() {
				String[] values = { launch.getId().toString(),
						items.stream().map(it -> it.getItemId().toString()).collect(Collectors.joining(",")), IN_PROGRESS.name() };
				return MessageFormatter.arrayFormat("Launch '{}' has items '[{}]' with '{}' status", values).getMessage();
			}

			public String toString() {
				return get();
			}
		});
	}

	//	private Project validateRoles(Launch launch, String userName, String projectName) {
	//		ProjectUser projectUser = projectRepository.selectProjectUser(projectName, userName);
	//		expect(projectUser, notNull()).verify(PROJECT_NOT_FOUND, projectName);
	//
	//		if (projectUser.getUser().getRole() != ADMINISTRATOR && !Objects.equals(launch.getUserId(), projectUser.getUser().getId())) {
	//			expect(launch.getProjectId(), equalTo(projectUser.getProject().getId())).verify(ACCESS_DENIED);
	//				/*
	//				 * Only PROJECT_MANAGER roles could delete launches
	//				 */
	//
	//			UserConfig userConfig = ProjectUtils.findUserConfigByLogin(project, user.getId());
	//			expect(userConfig, hasProjectRoles(Collections.singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
	//		}
	//		return project;
	//	}

	//	private void interruptItems(List<TestItem> testItems) {
	//		testItems.forEach(this::interruptItem);
	//	}
	//
	//	private void interruptItem(TestItem item) {
	//		if (!INTERRUPTED.equals(item.getStatus())) {
	//			item.setStatus(INTERRUPTED);
	//			item.setEndTime(Calendar.getInstance().getTime());
	//			item = testItemRepository.save(item);
	//			if (!item.hasChilds() && !IS_RETRY.test(item)) {
	//				Project project = projectRepository.findOne(launchRepository.findOne(item.getLaunchRef()).getProjectRef());
	//				item = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
	//						.updateExecutionStatistics(item);
	//				if (null != item.getIssue()) {
	//					item = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
	//							.updateIssueStatistics(item);
	//				}
	//			}
	//			if (null != item.getParent()) {
	//				interruptItem(testItemRepository.findOne(item.getParent()));
	//			}
	//		}
	//	}
}
