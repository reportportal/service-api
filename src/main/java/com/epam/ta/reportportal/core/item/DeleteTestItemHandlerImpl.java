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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.Preconditions.IN_PROGRESS;
import static com.epam.ta.reportportal.commons.Preconditions.hasProjectRoles;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.database.entity.Status.RESETED;
import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.ta.reportportal.database.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link DeleteTestItemHandler}
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
class DeleteTestItemHandlerImpl implements DeleteTestItemHandler {

	@Autowired
	private TestItemRepository testItemRepository;
	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;
	@Autowired
	private LaunchRepository launchRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ILogIndexer logIndexer;

	@Override
	public OperationCompletionRS deleteTestItem(String itemId, String projectName, String username, boolean isBatch) {
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, project);

		User user = userRepository.findOne(username);
		expect(user, notNull()).verify(USER_NOT_FOUND, username);

		TestItem item = testItemRepository.findOne(itemId);
		validate(itemId, item, projectName);
		validateRoles(item, user, project);
		try {

			StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(
					project.getConfiguration().getStatisticsCalculationStrategy());
			statisticsFacade.deleteExecutionStatistics(item);

			if (!item.getStatistics().getIssueCounter().isEmpty()) {
				statisticsFacade.deleteIssueStatistics(item);
			}

			testItemRepository.delete(itemId);
			if (!isBatch) {
				logIndexer.cleanIndex(projectName, singletonList(itemId));
			}

			if (null != item.getParent()) {
				TestItem parent = testItemRepository.findOne(item.getParent());
				if (!testItemRepository.findAllDescendants(parent.getId()).isEmpty()) {
					statisticsFacade.updateParentStatusFromStatistics(parent);
				} else {
					parent.setHasChilds(false);
					parent.setStatus(RESETED);
					testItemRepository.save(parent);
				}
			}

			Launch launch = launchRepository.findOne(item.getLaunchRef());
			/*
			 * We do not have to update launch statistics in case launch is in
			 * progress
			 */
			if (not(IN_PROGRESS).test(launch)) {
				statisticsFacade.updateLaunchFromStatistics(launch);
			}
		} catch (Exception e) {
			throw new ReportPortalException("Error during deleting TestStep item", e);
		}
		return new OperationCompletionRS("Test Item with ID = '" + itemId + "' has been successfully deleted.");
	}

	@Override
	public List<OperationCompletionRS> deleteTestItem(String[] ids, String project, String user) {
		logIndexer.cleanIndex(project, Arrays.asList(ids));
		return Stream.of(ids).map(it -> deleteTestItem(it, project, user, true)).collect(toList());
	}

	private void validate(String testItemId, TestItem testItem, String projectName) {
		expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, testItemId);
		expect(testItem, not(IN_PROGRESS)).verify(TEST_ITEM_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] in progress state", testItem.getId())
		);
		Launch parentLaunch = launchRepository.findOne(testItem.getLaunchRef());
		expect(parentLaunch, not(IN_PROGRESS)).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] under launch ['{}'] with 'In progress' state", testItem.getId(),
						testItem.getLaunchRef()
				)
		);
		expect(projectName, equalTo(parentLaunch.getProjectRef())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Deleting testItem '{}' is not under specified project '{}'", testItem.getId(), projectName)
		);
	}

	private void validateRoles(TestItem testItem, User user, Project project) {
		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		if (user.getRole() != ADMINISTRATOR && !user.getId().equalsIgnoreCase(launch.getUserRef())) {
			/*
			 * Only PROJECT_MANAGER roles could delete testItems
			 */
			UserConfig userConfig = findUserConfigByLogin(project, user.getId());
			expect(userConfig, hasProjectRoles(singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
		}
	}
}
