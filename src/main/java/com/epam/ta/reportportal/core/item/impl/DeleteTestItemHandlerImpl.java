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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.TestItemStructureRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemStructure;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
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

	private TestItemStructureRepository structureRepository;

	// TODO ANALYZER
	//	@Autowired
	//	private ILogIndexer logIndexer;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setStructureRepository(TestItemStructureRepository structureRepository) {
		this.structureRepository = structureRepository;
	}

	@Override
	public OperationCompletionRS deleteTestItem(Long itemId, String projectName, ReportPortalUser reportPortalUser) {
		TestItemStructure item = structureRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		validate(item.getTestItem(), reportPortalUser, projectName);
		structureRepository.delete(item);
		return new OperationCompletionRS("Test Item with ID = '" + itemId + "' has been successfully deleted.");
	}

	@Override
	public List<OperationCompletionRS> deleteTestItem(Long[] ids, String project, ReportPortalUser reportPortalUser) {
		return Stream.of(ids).map(it -> deleteTestItem(it, project, reportPortalUser)).collect(toList());
	}

	/**
	 * TODO document this
	 *
	 * @param testItem
	 * @param user
	 * @param projectName
	 */
	private void validate(TestItem testItem, ReportPortalUser user, String projectName) {
		expect(testItem.getItemStructure().getItemResults().getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(
				TEST_ITEM_IS_NOT_FINISHED, formattedSupplier("Unable to delete test item ['{}'] in progress state", testItem.getItemId()));
		Launch launch = testItem.getItemStructure().getLaunch();
		expect(launch.getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] under launch ['{}'] with 'In progress' state", testItem.getItemId(),
						launch.getId()
				)
		);
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Deleting testItem '{}' is not under specified project '{}'", testItem.getItemId(), projectName)
		);
		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(user.getUserId(), launch.getUserId())) {
			/*
			 * Only PROJECT_MANAGER roles could delete testItems
			 */
			expect(projectDetails.getProjectRole(), equalTo(ProjectRole.PROJECT_MANAGER)).verify(ACCESS_DENIED);
		}
	}
}
