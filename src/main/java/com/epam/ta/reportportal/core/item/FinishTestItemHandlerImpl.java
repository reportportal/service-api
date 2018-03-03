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
import com.epam.ta.reportportal.store.database.entity.item.TestItemTag;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.store.commons.EntityUtils.update;
import static com.epam.ta.reportportal.store.database.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueType.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueType.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestItemHandlerImpl.class);

	private LaunchRepository launchRepository;
	private TestItemRepository testItemRepository;
	//	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepo) {
		this.launchRepository = launchRepo;
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

		//verifyTestItem(testItem, testItemId, finishExecutionRQ, fromValue(finishExecutionRQ.getStatus()));

		//testItem.getTestItemResults().setDuration((float) (finishExecutionRQ.getEndTime().getTime() - testItem.getStartTime().getTime()));

		if (!isNullOrEmpty(finishExecutionRQ.getDescription())) {
			testItem.setDescription(finishExecutionRQ.getDescription());
		}
		if (!isEmpty(finishExecutionRQ.getTags())) {
			testItem.setTags(StreamSupport.stream(trimStrings(update(finishExecutionRQ.getTags())).spliterator(), false)
					.map(TestItemTag::new)
					.collect(toSet()));
		}

		Long launchId = testItem.getTestItemStructure().getLaunchId();
		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));

		//		if (!launch.getUserRef().equalsIgnoreCase(username)) {
		//			fail().withError(FINISH_ITEM_NOT_ALLOWED, "You are not launch owner.");
		//		}

		//		final Project project = projectRepository.findOne(launch.getProjectRef());

		Optional<StatusEnum> actualStatus = fromValue(finishExecutionRQ.getStatus());
		Issue providedIssue = finishExecutionRQ.getIssue();


		/*
		 * If test item has descendants, it's status is resolved from statistics
		 * When status provided, no matter test item has or not descendants, test
		 * item status is resolved to provided
		 */
		boolean hasChildren = testItemRepository.hasChildren(testItemId);
		if (actualStatus.isPresent() && !hasChildren) {
			testItem.getTestItemResults().setStatus(actualStatus.get());
		} else {
			testItem.getTestItemResults().setStatus(testItemRepository.identifyStatus(testItemId));
		}

		if (Preconditions.statusIn(FAILED, SKIPPED).test(testItem.getTestItemResults().getStatus())) {
			if (null != providedIssue) {
				//in provided issue should be locator id or NOT_ISSUE value
				String locator = providedIssue.getIssueType();
				if (!NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(locator)) {
					List<IssueType> projectIssueTypes = testItemRepository.selectIssueLocatorsByProject(1L);
					IssueType issueType = verifyIssue(testItemId, providedIssue, projectIssueTypes);
					com.epam.ta.reportportal.store.database.entity.item.issue.Issue issue = TO_ISSUE.apply(providedIssue);
					issue.setIssueType(issueType.getId());
					testItem.getTestItemResults().setIssue(issue);
				}
			} else {
				List<IssueType> issueTypes = testItemRepository.selectIssueLocatorsByProject(1L);
				IssueType toInvestigate = issueTypes.stream()
						.filter(it -> it.getLocator().equalsIgnoreCase(TO_INVESTIGATE.getLocator()))
						.findFirst()
						.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_ERROR));
				com.epam.ta.reportportal.store.database.entity.item.issue.Issue issue = new com.epam.ta.reportportal.store.database.entity.item.issue.Issue();
				issue.setIssueType(toInvestigate.getId());
				testItem.getTestItemResults().setIssue(issue);

			}
			testItemRepository.save(testItem);
		}
		return new OperationCompletionRS("TestItem with ID = '" + testItemId + "' successfully finished.");
	}

	private Function<Issue, com.epam.ta.reportportal.store.database.entity.item.issue.Issue> TO_ISSUE = from -> {
		com.epam.ta.reportportal.store.database.entity.item.issue.Issue issue = new com.epam.ta.reportportal.store.database.entity.item.issue.Issue();
		issue.setAutoAnalyzed(from.getAutoAnalyzed());
		issue.setIgnoreAnalyzer(from.getIgnoreAnalyzer());
		issue.setIssueDescription(from.getComment());
		return issue;
	};

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
		//		try {
		//			expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, testItemId);
		//			expect(testItem, not(Preconditions.TEST_ITEM_FINISHED)).verify(REPORTING_ITEM_ALREADY_FINISHED, testItem.getId());
		//
		//			expect(!actualStatus.isPresent() && !testItem.hasChilds(), equalTo(Boolean.FALSE), formattedSupplier(
		//					"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
		//					testItemId
		//			)).verify();
		//			List<TestItem> descendants = Collections.emptyList();
		//			if (testItem.hasChilds()) {
		//				descendants = testItemRepository.findDescendants(testItem.getId());
		//			}
		//			expect(descendants, not(Preconditions.HAS_IN_PROGRESS_ITEMS)).verify(FINISH_ITEM_NOT_ALLOWED,
		//					formattedSupplier("Test item '{}' has descendants with '{}' status. All descendants '{}'", testItemId,
		//							IN_PROGRESS.name(), descendants
		//					)
		//			);
		//			expect(finishExecutionRQ, Preconditions.finishSameTimeOrLater(testItem.getStartTime())).verify(
		//					FINISH_TIME_EARLIER_THAN_START_TIME, finishExecutionRQ.getEndTime(), testItem.getStartTime(), testItemId);
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
				.orElseThrow(() -> new ReportPortalException(AMBIGUOUS_TEST_ITEM_STATUS, formattedSupplier(
						"Invalid test item issue type definition '{}' is requested for item '{}'. Valid issue types locators are: {}",
						issue.getIssueType(), testItemId, projectIssueTypes.stream().map(IssueType::getLocator).collect(toList())
				)));
	}

	/**
	 * Issue type recognition for specified test item from
	 *
	 * @param testItem      Test item
	 * @param providedIssue Issue
	 * @param project       Project
	 * @return TestItem
	 */
	//		TestItem awareTestItemIssueTypeFromStatus(TestItemResults testItemResults, final Issue providedIssue, final Project project, String submitter) {
	//			if (FAILED.equals(testItem.getStatus()) || SKIPPED.equals(testItem.getStatus())) {
	//				if (null != providedIssue) {
	//					verifyIssue(testItem.getId(), providedIssue, project.getConfiguration());
	//					String issueType = providedIssue.getIssueType();
	//					if (!issueType.equalsIgnoreCase(NOT_ISSUE_FLAG.getValue())) {
	//						TestItemIssue issue = new TestItemIssue(project.getConfiguration().getByLocator(issueType).getLocator(),
	//								providedIssue.getComment()
	//						);
	//
	//						//set provided external issues if any present
	//						issue.setExternalSystemIssues(
	//								Optional.ofNullable(providedIssue.getExternalSystemIssues()).map(issues -> issues.stream().peek(it -> {
	//									//not sure if it propogates exception correctly
	//									expect(externalSystemRepository.exists(it.getExternalSystemId()), equalTo(true)).verify(
	//											EXTERNAL_SYSTEM_NOT_FOUND, it.getExternalSystemId());
	//								}).map(TestItemUtils.externalIssueDtoConverter(submitter)).collect(Collectors.toSet())).orElse(null));
	//						issue.setIgnoreAnalyzer(BooleanUtils.toBoolean(providedIssue.getIgnoreAnalyzer()));
	//						testItem.setIssue(issue);
	//					}
	//				} else {
	//					testItem.setIssue(new TestItemIssue());
	//					/* For AA saving reference initialization */
	//					//				Launch launch = launchRepository.findOne(testItem.getLaunchRef());
	//					//				expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, testItem.getLaunchRef());
	//					//				if (Mode.DEFAULT.equals(launch.getMode()) && project.getConfiguration().getIsAutoAnalyzerEnabled()) {
	//					//					testItem = analyzeItem(launch.getId(), testItem, project.getConfiguration().getAnalyzeOnTheFly());
	//					//				}
	//				}
	//			}
	//			return testItem;
	//		}

	/**
	 * Analyze current item if auto analysis goes automatically and on the fly
	 * of test items processing. Analyze only items that have
	 * {@link com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType#TO_INVESTIGATE} issue
	 *
	 * @param launchId        Launch id
	 * @param testItem        Test item to analyze
	 * @param analyzeOnTheFly
	 * @return
	 */
/*	private TestItem analyzeItem(String launchId, TestItem testItem, Boolean analyzeOnTheFly) {
		TestItemIssue issue = testItem.getIssue();
		analyzeOnTheFly = Optional.ofNullable(analyzeOnTheFly).orElse(false);
		if (null != issue && analyzeOnTheFly && TO_INVESTIGATE.getLocator().equals(issue.getIssueType())) {
			List<TestItem> analyzedItem = issuesAnalyzer.analyze(launchId, Collections.singletonList(testItem));
			return analyzedItem.get(0);
		}
		return testItem;
	}*/
}
