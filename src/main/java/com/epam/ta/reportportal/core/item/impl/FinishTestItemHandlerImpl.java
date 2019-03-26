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
package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.descendant.FinishDescendantsHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link FinishTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
class FinishTestItemHandlerImpl implements FinishTestItemHandler {

	private final TestItemRepository testItemRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final FinishDescendantsHandler<TestItem> finishDescendantsHandler;

	@Autowired
	FinishTestItemHandlerImpl(TestItemRepository testItemRepository, IssueTypeHandler issueTypeHandler,
			@Qualifier("finishTestItemDescendantsHandler") FinishDescendantsHandler<TestItem> finishDescendantsHandler) {
		this.testItemRepository = testItemRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.finishDescendantsHandler = finishDescendantsHandler;
	}

	@Override
	public OperationCompletionRS finishTestItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, Long testItemId,
			FinishTestItemRQ finishExecutionRQ) {
		TestItem testItem = testItemRepository.findById(testItemId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, testItemId));

		verifyTestItem(user, testItem, fromValue(finishExecutionRQ.getStatus()), testItem.isHasChildren());

		TestItemResults testItemResults = processItemResults(projectDetails, testItem, finishExecutionRQ, testItem.isHasChildren());

		testItem = new TestItemBuilder(testItem).addDescription(finishExecutionRQ.getDescription())
				.addAttributes(finishExecutionRQ.getAttributes())
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
	private TestItemResults processItemResults(ReportPortalUser.ProjectDetails projectDetails, TestItem testItem,
			FinishTestItemRQ finishExecutionRQ, boolean hasChildren) {
		TestItemResults testItemResults = ofNullable(testItem.getItemResults()).orElseGet(TestItemResults::new);
		Optional<StatusEnum> actualStatus = fromValue(finishExecutionRQ.getStatus());
		Issue providedIssue = finishExecutionRQ.getIssue();

		if (hasChildren) {
			if (testItemRepository.hasItemsInStatusByParent(testItem.getItemId(), StatusEnum.IN_PROGRESS)) {
				finishDescendantsHandler.finishDescendants(testItem,
						actualStatus.orElse(StatusEnum.INTERRUPTED),
						finishExecutionRQ.getEndTime(),
						projectDetails
				);
			}
			boolean isFailed = testItemRepository.hasDescendantsWithStatusNotEqual(testItem.getItemId(), JStatusEnum.PASSED);
			testItemResults.setStatus(isFailed ? FAILED : PASSED);
		} else {
			actualStatus.ifPresent(testItemResults::setStatus);
		}

		if (Preconditions.statusIn(FAILED, SKIPPED).test(testItemResults.getStatus()) && !hasChildren
				&& !ofNullable(testItem.getRetryOf()).isPresent()) {
			IssueEntity issueEntity = new IssueEntity();
			if (null != providedIssue) {
				//in provided issue should be locator id or NOT_ISSUE value
				String locator = providedIssue.getIssueType();
				if (!NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(locator)) {
					IssueType issueType = issueTypeHandler.defineIssueType(projectDetails.getProjectId(), locator);
					issueEntity = IssueConverter.TO_ISSUE.apply(providedIssue);
					issueEntity.setIssueType(issueType);
					issueEntity.setIssueId(testItem.getItemId());
					testItemResults.setIssue(issueEntity);
				}
			} else {
				IssueType toInvestigate = issueTypeHandler.defineIssueType(projectDetails.getProjectId(), TO_INVESTIGATE.getLocator());
				issueEntity.setIssueType(toInvestigate);
				issueEntity.setIssueId(testItem.getItemId());
				testItemResults.setIssue(issueEntity);
			}
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
	private void verifyTestItem(ReportPortalUser user, TestItem testItem, Optional<StatusEnum> actualStatus, boolean hasChildren) {
		Launch launch;
		if (ofNullable(testItem.getRetryOf()).isPresent()) {
			TestItem retryParent = testItemRepository.findById(testItem.getRetryOf())
					.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItem.getRetryOf()));

			launch = ofNullable(retryParent.getLaunch()).orElseGet(() -> ofNullable(retryParent.getParent()).map(TestItem::getLaunch)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
		} else {
			launch = ofNullable(testItem.getLaunch()).orElseGet(() -> ofNullable(testItem.getParent()).map(TestItem::getLaunch)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
		}
		expect(user.getUsername(), equalTo(launch.getUser().getLogin())).verify(FINISH_ITEM_NOT_ALLOWED, "You are not a launch owner.");

		expect(testItem.getItemResults().getStatus(), Preconditions.statusIn(IN_PROGRESS)).verify(REPORTING_ITEM_ALREADY_FINISHED,
				testItem.getItemId()
		);

		expect(!actualStatus.isPresent() && !hasChildren, equalTo(Boolean.FALSE)).verify(AMBIGUOUS_TEST_ITEM_STATUS, formattedSupplier(
				"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
				testItem.getItemId()
		));
	}
}
