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

import com.epam.ta.reportportal.commons.DbUtils;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.launch.IFinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.IRetriesLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.events.LaunchFinishForcedEvent;
import com.epam.ta.reportportal.events.LaunchFinishedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.commons.EntityUtils.update;
import static com.epam.ta.reportportal.commons.Preconditions.hasProjectRoles;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.util.Predicates.IS_RETRY;
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

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private IRetriesLaunchHandler retriesLaunchHandler;

	@Override
	public OperationCompletionRS finishLaunch(String launchId, FinishExecutionRQ finishLaunchRQ, String projectName, String username) {

		Launch launch = launchRepository.findOne(launchId);
		validate(launchId, launch, finishLaunchRQ);
		Project project = validateRoles(launch, username, projectName);

		launch.setEndTime(finishLaunchRQ.getEndTime());
		if (!Strings.isNullOrEmpty(finishLaunchRQ.getDescription())) {
			launch.setDescription(finishLaunchRQ.getDescription());
		}
		if (!CollectionUtils.isEmpty(finishLaunchRQ.getTags())) {
			launch.setTags(Sets.newHashSet(trimStrings(update(finishLaunchRQ.getTags()))));
		}

		Optional<Status> status = fromValue(finishLaunchRQ.getStatus());
		status.ifPresent(providedStatus -> {
			/* Validate provided status */
			expect(providedStatus, not(Preconditions.statusIn(IN_PROGRESS, SKIPPED))).verify(INCORRECT_FINISH_STATUS,
					formattedSupplier("Cannot finish launch '{}' with status '{}'", launchId, providedStatus)
			);
			/* Validate actual launch status */
			if (PASSED.equals(providedStatus)) {
				/* Validate actual launch status */
				expect(launch.getStatus(), Preconditions.statusIn(IN_PROGRESS, PASSED)).verify(INCORRECT_FINISH_STATUS,
						formattedSupplier("Cannot finish launch '{}' with current status '{}' as 'PASSED'", launchId, launch.getStatus())
				);
				/*
				 * Calculate status from launch statistics and validate it
				 */
				Status fromStatistics = StatisticsHelper.getStatusFromStatistics(launch.getStatistics());
				expect(fromStatistics, Preconditions.statusIn(IN_PROGRESS, PASSED)).verify(INCORRECT_FINISH_STATUS,
						formattedSupplier("Cannot finish launch '{}' with calculated automatically status '{}' as 'PASSED'", launchId,
								fromStatistics
						)
				);
			}
		});
		launch.setStatus(status.orElse(StatisticsHelper.getStatusFromStatistics(launch.getStatistics())));
		try {
			launchRepository.save(launch);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while Launch updating.", exp);
		}
		eventPublisher.publishEvent(new LaunchFinishedEvent(launch, project));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully finished.");
	}

	@Override
	public OperationCompletionRS stopLaunch(String launchId, FinishExecutionRQ finishLaunchRQ, String projectName, String userName) {
		Launch launch = launchRepository.findOne(launchId);
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);

		validateRoles(launch, userName, projectName);

		expect(launch, not(Preconditions.LAUNCH_FINISHED)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		);

		launch.setEndTime(finishLaunchRQ.getEndTime());
		if (null != launch.getDescription()) {
			launch.setDescription(launch.getDescription().concat(LAUNCH_STOP_DESCRIPTION));
		} else {
			launch.setDescription(LAUNCH_STOP_DESCRIPTION);
		}
		Set<String> newTags = launch.getTags();
		if (null == newTags) {
			newTags = new HashSet<>();
		}
		newTags.add(LAUNCH_STOP_TAG);
		launch.setTags(newTags);
		launch.setStatus(fromValue(finishLaunchRQ.getStatus()).orElse(STOPPED));
		try {
			launchRepository.save(launch);
			if (launchRepository.hasItems(launch, IN_PROGRESS)) {
				// Find all IN_PROGRESS children and interrupt them
				List<TestItem> itemsInProgress = testItemRepository.findInStatusItems(IN_PROGRESS.name(), launch.getId());
				interruptItems(itemsInProgress);
			}
			retriesLaunchHandler.handleRetries(launch);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while Launch updating.", exp);
		}
		eventPublisher.publishEvent(new LaunchFinishForcedEvent(launch, userName));
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

	private void validate(final String launchId, Launch launch, FinishExecutionRQ finishExecutionRQ) {
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);

		expect(launch, not(Preconditions.LAUNCH_FINISHED)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		);

		expect(finishExecutionRQ, Preconditions.finishSameTimeOrLater(launch.getStartTime())).verify(FINISH_TIME_EARLIER_THAN_START_TIME,
				finishExecutionRQ.getEndTime(), launch.getStartTime(), launchId
		);

		final List<TestItem> items = testItemRepository.findByLaunch(launch);
		expect(items, not(Preconditions.HAS_IN_PROGRESS_ITEMS)).verify(FINISH_LAUNCH_NOT_ALLOWED, new Supplier<String>() {
			@Override
			public String get() {
				String[] values = { launchId, DbUtils.toIds(getInProgressItems(items)).stream().collect(Collectors.joining(",")),
						IN_PROGRESS.name() };
				return MessageFormatter.arrayFormat("Launch '{}' has items '[{}]' with '{}' status", values).getMessage();
			}

			@Override
			public String toString() {
				return get();
			}
		});
	}

	private Project validateRoles(Launch launch, String userName, String projectName) {
		User user = userRepository.findOne(userName);
		expect(user, notNull()).verify(USER_NOT_FOUND, userName);

		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		if (user.getRole() != ADMINISTRATOR && !user.getId().equalsIgnoreCase(launch.getUserRef())) {
			expect(launch.getProjectRef(), equalTo(projectName)).verify(ACCESS_DENIED);
			/*
			 * Only PROJECT_MANAGER roles could delete launches
			 */
			UserConfig userConfig = ProjectUtils.findUserConfigByLogin(project, user.getId());
			expect(userConfig, hasProjectRoles(Collections.singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
		}
		return project;
	}

	/**
	 * Convert list of test items in list of items with IN_PROGRESS status only
	 *
	 * @param items
	 * @return
	 */
	private List<TestItem> getInProgressItems(List<TestItem> items) {
		return items.stream().filter(descendant -> IN_PROGRESS.equals(descendant.getStatus())).collect(Collectors.toList());
	}

	private void interruptItems(List<TestItem> testItems) {
		testItems.forEach(this::interruptItem);
	}

	private void interruptItem(TestItem item) {
		if (!INTERRUPTED.equals(item.getStatus())) {
			item.setStatus(INTERRUPTED);
			item.setEndTime(Calendar.getInstance().getTime());
			item = testItemRepository.save(item);
			if (!item.hasChilds() && !IS_RETRY.test(item)) {
				Project project = projectRepository.findOne(launchRepository.findOne(item.getLaunchRef()).getProjectRef());
				item = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
						.updateExecutionStatistics(item);
				if (null != item.getIssue()) {
					item = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
							.updateIssueStatistics(item);
				}
			}
			if (null != item.getParent()) {
				interruptItem(testItemRepository.findOne(item.getParent()));
			}
		}
	}
}
