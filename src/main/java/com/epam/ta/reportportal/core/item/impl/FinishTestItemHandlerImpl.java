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
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link FinishTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
class FinishTestItemHandlerImpl implements FinishTestItemHandler {

	private TestItemRepository testItemRepository;

	private IssueTypeHandler issueTypeHandler;

	//	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setIssueTypeHandler(IssueTypeHandler issueTypeHandler) {
		this.issueTypeHandler = issueTypeHandler;
	}

	//	@Autowired
	//	public void setExternalSystemRepository(ExternalSystemRepository externalSystemRepository) {
	//		this.externalSystemRepository = externalSystemRepository;
	//	}

	@Override
	public OperationCompletionRS finishTestItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, Long testItemId,
			FinishTestItemRQ finishExecutionRQ) {
		TestItem testItem = testItemRepository.findById(testItemId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, testItemId));

		boolean hasChildren = testItemRepository.hasChildren(testItem.getItemId(), testItem.getPath());
		verifyTestItem(user, finishExecutionRQ, testItem, fromValue(finishExecutionRQ.getStatus()), hasChildren);

		TestItemResults testItemResults = processItemResults(projectDetails.getProjectId(), testItem, finishExecutionRQ, hasChildren);

		testItem = new TestItemBuilder(testItem).addDescription(finishExecutionRQ.getDescription())
				.addTags(finishExecutionRQ.getTags())
				.addTestItemResults(testItemResults)
				.get();

		testItemRepository.save(testItem);
		return new OperationCompletionRS("TestItem with ID = '" + testItemId + "' successfully finished.");
	}

	/**
	 * If test item has descendants, it's status is resolved from statistics
	 * When status provided, no matter test item has or not descendants, test
	 * item status is resolved to provided
	 *
	 * @param testItem          Test item id
	 * @param finishExecutionRQ Finish test item request
	 * @return TestItemResults object
	 */
	private TestItemResults processItemResults(Long projectId, TestItem testItem, FinishTestItemRQ finishExecutionRQ, boolean hasChildren) {
		TestItemResults testItemResults = Optional.ofNullable(testItem.getItemResults()).orElseGet(TestItemResults::new);
		Optional<StatusEnum> actualStatus = fromValue(finishExecutionRQ.getStatus());
		Issue providedIssue = finishExecutionRQ.getIssue();

		if (actualStatus.isPresent() && !hasChildren) {
			testItemResults.setStatus(actualStatus.get());
		} else {
			testItemResults.setStatus(testItemRepository.identifyStatus(testItem.getItemId()));
		}

		if (Preconditions.statusIn(FAILED, SKIPPED).test(testItemResults.getStatus()) && !hasChildren) {
			IssueEntity issueEntity = new IssueEntity();
			if (null != providedIssue) {
				//in provided issue should be locator id or NOT_ISSUE value
				String locator = providedIssue.getIssueType();
				if (!NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(locator)) {
					IssueType issueType = issueTypeHandler.defineIssueType(testItem.getItemId(), projectId, locator);
					issueEntity = IssueConverter.TO_ISSUE.apply(providedIssue);
					issueEntity.setIssueType(issueType);
				}
			} else {
				IssueType toInvestigate = issueTypeHandler.defineIssueType(testItem.getItemId(), projectId, TO_INVESTIGATE.getLocator());
				issueEntity.setIssueType(toInvestigate);
			}
			issueEntity.setIssueId(testItem.getItemId());
			testItemResults.setIssue(issueEntity);
		}
		testItemResults.setEndTime(TO_LOCAL_DATE_TIME.apply(finishExecutionRQ.getEndTime()));
		return testItemResults;
	}

	/**
	 * Validation procedure for specified test item
	 *
	 * @param user         Report portal user
	 * @param testItem     Test item
	 * @param actualStatus Actual status of item
	 * @param hasChildren  Does item contain children
	 */
	private void verifyTestItem(ReportPortalUser user, FinishExecutionRQ finishExecutionRQ, TestItem testItem,
			Optional<StatusEnum> actualStatus, boolean hasChildren) {
		Launch launch = Optional.ofNullable(testItem.getLaunch()).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND));
		expect(user.getUserId(), equalTo(launch.getUserId())).verify(FINISH_ITEM_NOT_ALLOWED, "You are not a launch owner.");

		expect(testItem.getItemResults().getStatus(), Preconditions.statusIn(IN_PROGRESS)).verify(REPORTING_ITEM_ALREADY_FINISHED,
				testItem.getItemId()
		);

		expect(!actualStatus.isPresent() && !hasChildren, equalTo(Boolean.FALSE)).verify(AMBIGUOUS_TEST_ITEM_STATUS, formattedSupplier(
				"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
				testItem.getItemId()
		));

		expect(finishExecutionRQ.getEndTime(), date -> TO_LOCAL_DATE_TIME.apply(date).isAfter(testItem.getStartTime())).verify(FINISH_TIME_EARLIER_THAN_START_TIME,
				"The finish time should not be earlier than the start time."
		);
	}
}
