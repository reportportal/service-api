/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
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
	public OperationCompletionRS deleteTestItem(Long itemId, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser reportPortalUser) {
		TestItem item = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		validate(item, reportPortalUser, projectDetails);
		testItemRepository.delete(item);
		return new OperationCompletionRS("Test Item with ID = '" + itemId + "' has been successfully deleted.");
	}

	@Override
	public List<OperationCompletionRS> deleteTestItem(Long[] ids, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser reportPortalUser) {
		return Stream.of(ids).map(it -> deleteTestItem(it, projectDetails, reportPortalUser)).collect(toList());
	}

	/**
	 * Validate {@link ReportPortalUser} credentials, {@link com.epam.ta.reportportal.entity.item.TestItemResults#status},
	 * {@link Launch#status} and {@link Launch} affiliation to the {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param testItem       {@link TestItem}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 */
	private void validate(TestItem testItem, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		expect(testItem.getItemResults().getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(TEST_ITEM_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] in progress state", testItem.getItemId())
		);
		Launch launch = testItem.getLaunch();
		expect(launch.getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED, formattedSupplier(
				"Unable to delete test item ['{}'] under launch ['{}'] with 'In progress' state",
				testItem.getItemId(),
				launch.getId()
		));
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Deleting testItem '{}' is not under specified project '{}'",
						testItem.getItemId(),
						projectDetails.getProjectId()
				)
		);
		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(user.getUsername(), launch.getUser().getLogin())) {
			/*
			 * Only PROJECT_MANAGER roles could delete testItems
			 */
			expect(projectDetails.getProjectRole(), equalTo(ProjectRole.PROJECT_MANAGER)).verify(ACCESS_DENIED);
		}
	}
}
