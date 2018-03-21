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
package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemResults;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.store.database.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueType.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueType.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link FinishTestItemHandler}
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 * @author Dzianis Shlychkou
 * @author Andrei_Ramanchuk
 */
@Service
class FinishTestItemHandlerImpl implements FinishTestItemHandler {

	private LaunchRepository launchRepository;

	private TestItemRepository testItemRepository;

	private Function<Issue, IssueEntity> TO_ISSUE = from -> {
		IssueEntity issue = new IssueEntity();
		issue.setAutoAnalyzed(from.getAutoAnalyzed());
		issue.setIgnoreAnalyzer(from.getIgnoreAnalyzer());
		issue.setIssueDescription(from.getComment());
		return issue;
	};

	//	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	//	@Autowired
	//	public void setExternalSystemRepository(ExternalSystemRepository externalSystemRepository) {
	//		this.externalSystemRepository = externalSystemRepository;
	//	}

	@Override
	public OperationCompletionRS finishTestItem(Long testItemId, FinishTestItemRQ finishExecutionRQ, String username) {

		TestItem testItem = testItemRepository.findById(testItemId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, testItemId));

		verifyTestItem(testItem, testItemId, finishExecutionRQ, fromValue(finishExecutionRQ.getStatus()));
		//
		//		Launch launch = Optional.ofNullable(testItem.getTestItemStructure().getLaunch())
		//				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND));
		//
		//		if (!launch.getUserRef().equalsIgnoreCase(username)) {
		//			fail().withError(FINISH_ITEM_NOT_ALLOWED, "You are not launch owner.");
		//		}
		//		final Project project = projectRepository.findOne(launch.getProjectRef());

		TestItemResults testItemResults = processItemResults(testItem, finishExecutionRQ);
		testItem = new TestItemBuilder(testItem).addDescription(finishExecutionRQ.getDescription())
				.addTags(finishExecutionRQ.getTags())
				.addTestItemResults(testItemResults, finishExecutionRQ.getEndTime())
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
	private TestItemResults processItemResults(TestItem testItem, FinishTestItemRQ finishExecutionRQ) {
		TestItemResults testItemResults = Optional.ofNullable(testItem.getTestItemResults()).orElse(new TestItemResults());
		Optional<StatusEnum> actualStatus = fromValue(finishExecutionRQ.getStatus());
		Issue providedIssue = finishExecutionRQ.getIssue();
		boolean hasChildren = testItemRepository.hasChildren(testItem.getItemId());
		if (actualStatus.isPresent() && !hasChildren) {
			testItemResults.setStatus(actualStatus.get());
		} else {
			testItemResults.setStatus(testItemRepository.identifyStatus(testItem.getItemId()));
		}

		if (Preconditions.statusIn(FAILED, SKIPPED).test(testItemResults.getStatus()) && !hasChildren) {
			if (null != providedIssue) {
				//in provided issue should be locator id or NOT_ISSUE value
				String locator = providedIssue.getIssueType();
				if (!NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(locator)) {
					List<IssueType> projectIssueTypes = testItemRepository.selectIssueLocatorsByProject(1L); //TODO project
					IssueType issueType = verifyIssue(testItem.getItemId(), providedIssue, projectIssueTypes);
					IssueEntity issue = TO_ISSUE.apply(providedIssue);
					issue.setIssueType(issueType);
					testItemResults.setIssue(issue);
				}
			} else {
				List<IssueType> issueTypes = testItemRepository.selectIssueLocatorsByProject(1L); //TODO  project
				IssueType toInvestigate = issueTypes.stream()
						.filter(it -> it.getLocator().equalsIgnoreCase(TO_INVESTIGATE.getLocator()))
						.findFirst()
						.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_ERROR));
				IssueEntity issue = new IssueEntity();
				issue.setIssueType(toInvestigate);
				testItemResults.setIssue(issue);
			}
		}
		return testItemResults;
	}

	/**
	 * Validation procedure for specified test item
	 *
	 * @param testItemId        ID of test item
	 * @param finishExecutionRQ Request data
	 * @param actualStatus      Actual status of item
	 * @return TestItem updated item
	 */

	private void verifyTestItem(TestItem testItem, final Long testItemId, FinishTestItemRQ finishExecutionRQ,
			Optional<StatusEnum> actualStatus) {
		expect(testItem.getTestItemResults().getStatus(), Preconditions.statusIn(IN_PROGRESS)).verify(
				REPORTING_ITEM_ALREADY_FINISHED, testItem.getItemId());
		List<TestItem> items = testItemRepository.selectItemsInStatusByParent(testItem.getItemId(), IN_PROGRESS);
		expect(items.isEmpty(), equalTo(true)).verify(FINISH_ITEM_NOT_ALLOWED,
				formattedSupplier("Test item '{}' has descendants with '{}' status. All descendants '{}'", testItemId, IN_PROGRESS.name(),
						items
				)
		);
		//		try {
		//
		//			expect(!actualStatus.isPresent() && !testItem.hasChilds(), equalTo(Boolean.FALSE), formattedSupplier(
		//					"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
		//					testItemId
		//			)).verify();

		expect(finishExecutionRQ.getEndTime(), Preconditions.sameTimeOrLater(testItem.getStartTime())).verify(
				FINISH_TIME_EARLIER_THAN_START_TIME, finishExecutionRQ.getEndTime(), testItem.getStartTime(), testItemId);
		//
		//			/*
		//			 * If there is issue provided we have to be sure issue type is
		//			 * correct
		//			 */
		//		} catch (BusinessRuleViolationException e) {
		//			fail().withError(AMBIGUOUS_TEST_ITEM_STATUS, e.getMessage());
		//		}
	}

	private IssueType verifyIssue(Long testItemId, Issue issue, List<IssueType> projectIssueTypes) {
		return projectIssueTypes.stream()
				.filter(it -> it.getTestItemIssueType().getLocator().equalsIgnoreCase(issue.getIssueType()))
				.findAny()
				.orElseThrow(() -> new ReportPortalException(
						AMBIGUOUS_TEST_ITEM_STATUS, formattedSupplier(
						"Invalid test item issue type definition '{}' is requested for item '{}'. Valid issue types locators are: {}",
						issue.getIssueType(), testItemId, projectIssueTypes.stream().map(IssueType::getLocator).collect(toList())
				)));
	}
}
