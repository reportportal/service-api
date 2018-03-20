/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.Predicates.not;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_IS_NOT_FINISHED;
import static com.epam.ta.reportportal.ws.model.ErrorType.TEST_ITEM_IS_NOT_FINISHED;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link DeleteTestItemHandler}
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
class DeleteTestItemHandlerImpl implements DeleteTestItemHandler {

	private TestItemRepository testItemRepository;

	// TODO ANALYZER
	//	@Autowired
	//	private ILogIndexer logIndexer;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public OperationCompletionRS deleteTestItem(Long itemId, String projectName, String username) {

		//	Project project = projectRepository.findOne(projectName);
		//	expect(project, notNull()).verify(PROJECT_NOT_FOUND, project);
		//
		//	User user = userRepository.findOne(username);
		//	expect(user, notNull()).verify(USER_NOT_FOUND, username);

		TestItem item = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		validate(item, projectName);

		// TODO validate roles
		//validateRoles(item, user, project);

		testItemRepository.delete(item);
		return new OperationCompletionRS("Test Item with ID = '" + itemId + "' has been successfully deleted.");
	}

	@Override
	public List<OperationCompletionRS> deleteTestItem(Long[] ids, String project, String user) {
		//logIndexer.cleanIndex(project, Arrays.asList(ids));
		return Stream.of(ids).map(it -> deleteTestItem(it, project, user)).collect(toList());
	}

	private void validate(TestItem testItem, String projectName) {
		expect(testItem.getTestItemResults().getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(TEST_ITEM_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] in progress state", testItem.getItemId())
		);
		Launch launch = testItem.getLaunch();
		expect(launch.getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] under launch ['{}'] with 'In progress' state", testItem.getItemId(),
						launch.getId()
				)
		);
		//		expect(projectName, equalTo(parentLaunch.getProjectRef())).verify(FORBIDDEN_OPERATION,
		//				formattedSupplier("Deleting testItem '{}' is not under specified project '{}'", testItem.getId(), projectName)
		//		);
	}

	//	private void validateRoles(TestItem testItem, User user, Project project) {
	//		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
	//		if (user.getRole() != ADMINISTRATOR && !user.getId().equalsIgnoreCase(launch.getUserRef())) {
	//				/*
	//				 * Only PROJECT_MANAGER roles could delete testItems
	//				 */
	//			UserConfig userConfig = findUserConfigByLogin(project, user.getId());
	//			expect(userConfig, hasProjectRoles(singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
	//		}
	//	}
}
