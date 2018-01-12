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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.commons.EntityUtils.update;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

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

	private ProjectRepository projectRepository;
	private LaunchRepository launchRepository;
	private TestItemRepository testItemRepository;
	private StatisticsFacadeFactory statisticsFacadeFactory;
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepo) {
		this.launchRepository = launchRepo;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setStatisticsFacadeFactory(StatisticsFacadeFactory statisticsFacadeFactory) {
		this.statisticsFacadeFactory = statisticsFacadeFactory;
	}

	@Autowired
	public void setExternalSystemRepository(ExternalSystemRepository externalSystemRepository) {
		this.externalSystemRepository = externalSystemRepository;
	}

	@Override
	public OperationCompletionRS finishTestItem(String testItemId, FinishTestItemRQ finishExecutionRQ, String username) {

		TestItem testItem = testItemRepository.findOne(testItemId);

		verifyTestItem(testItem, testItemId, finishExecutionRQ, fromValue(finishExecutionRQ.getStatus()));

		testItem.setEndTime(finishExecutionRQ.getEndTime());
		if (!isNullOrEmpty(finishExecutionRQ.getDescription())) {
			testItem.setItemDescription(finishExecutionRQ.getDescription());
		}
		if (!isEmpty(finishExecutionRQ.getTags())) {
			testItem.setTags(Sets.newHashSet(trimStrings(update(finishExecutionRQ.getTags()))));
		}
		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, testItem.getLaunchRef());
		if (!launch.getUserRef().equalsIgnoreCase(username)) {
			fail().withError(FINISH_ITEM_NOT_ALLOWED, "You are not launch owner.");
		}

		final Project project = projectRepository.findOne(launch.getProjectRef());

		Optional<Status> actualStatus = fromValue(finishExecutionRQ.getStatus());
		Issue providedIssue = finishExecutionRQ.getIssue();

		StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(
				project.getConfiguration().getStatisticsCalculationStrategy());

		/*
		 * If test item has descendants, it's status is resolved from statistics
		 * When status provided, no meter test item has or not descendants, test
		 * item status is resolved to provided
		 */
		if (!actualStatus.isPresent() && testItem.hasChilds()) {
			testItem = statisticsFacade.identifyStatus(testItem);
		} else {
			testItem.setStatus(actualStatus.get());
		}
		if (statisticsFacade.awareIssue(testItem)) {
			testItem = awareTestItemIssueTypeFromStatus(testItem, providedIssue, project, username);
		}
		try {
			testItem.setStatistics(null);
			testItemRepository.partialUpdate(testItem);

			testItem = statisticsFacade.updateExecutionStatistics(testItem);
			if (null != testItem.getIssue()) {
				statisticsFacade.updateIssueStatistics(testItem);
			}

		} catch (Exception e) {
			throw new ReportPortalException("Error during updating TestItem " + e.getMessage(), e);
		}

		return new OperationCompletionRS("TestItem with ID = '" + testItemId + "' successfully finished.");
	}

	/**
	 * Validation procedure for specified test item
	 *
	 * @param testItemId        ID of test item
	 * @param finishExecutionRQ Request data
	 * @param actualStatus      Actual status of item
	 * @return TestItem updated item
	 */
	private void verifyTestItem(TestItem testItem, final String testItemId, FinishTestItemRQ finishExecutionRQ,
			Optional<Status> actualStatus) {
		try {
			expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, testItemId);
			expect(testItem, not(Preconditions.TEST_ITEM_FINISHED)).verify(REPORTING_ITEM_ALREADY_FINISHED, testItem.getId());

			expect(!actualStatus.isPresent() && !testItem.hasChilds(), equalTo(Boolean.FALSE), formattedSupplier(
					"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
					testItemId
			)).verify();
			List<TestItem> descendants = Collections.emptyList();
			if (testItem.hasChilds()) {
				descendants = testItemRepository.findDescendants(testItem.getId());
			}
			expect(descendants, not(Preconditions.HAS_IN_PROGRESS_ITEMS)).verify(FINISH_ITEM_NOT_ALLOWED,
					formattedSupplier("Test item '{}' has descendants with '{}' status. All descendants '{}'", testItemId,
							IN_PROGRESS.name(), descendants
					)
			);
			expect(finishExecutionRQ, Preconditions.finishSameTimeOrLater(testItem.getStartTime())).verify(
					FINISH_TIME_EARLIER_THAN_START_TIME, finishExecutionRQ.getEndTime(), testItem.getStartTime(), testItemId);

			/*
			 * If there is issue provided we have to be sure issue type is
			 * correct
			 */
		} catch (BusinessRuleViolationException e) {
			fail().withError(AMBIGUOUS_TEST_ITEM_STATUS, e.getMessage());
		}
	}

	void verifyIssue(String testItemId, Issue issue, Project.Configuration projectSettings) {
		if (issue != null && !NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(issue.getIssueType())) {
			expect(projectSettings.getByLocator(issue.getIssueType()), notNull()).verify(AMBIGUOUS_TEST_ITEM_STATUS, formattedSupplier(
					"Invalid test item issue type definition '{}' is requested for item '{}'. Valid issue types locators are: {}",
					issue.getIssueType(), testItemId, projectSettings.getSubTypes()
							.values()
							.stream()
							.flatMap(Collection::stream)
							.map(StatisticSubType::getLocator)
							.collect(Collectors.toList())
			));
		}
	}

	/**
	 * Issue type recognition for specified test item from
	 *
	 * @param testItem      Test item
	 * @param providedIssue Issue
	 * @param project       Project
	 * @return TestItem
	 */
	TestItem awareTestItemIssueTypeFromStatus(TestItem testItem, final Issue providedIssue, final Project project, String submitter) {
		if (FAILED.equals(testItem.getStatus()) || SKIPPED.equals(testItem.getStatus())) {
			if (null != providedIssue) {
				verifyIssue(testItem.getId(), providedIssue, project.getConfiguration());
				String issueType = providedIssue.getIssueType();
				if (!issueType.equalsIgnoreCase(NOT_ISSUE_FLAG.getValue())) {
					TestItemIssue issue = new TestItemIssue(project.getConfiguration().getByLocator(issueType).getLocator(),
							providedIssue.getComment()
					);

					//set provided external issues if any present
					issue.setExternalSystemIssues(
							Optional.ofNullable(providedIssue.getExternalSystemIssues()).map(issues -> issues.stream().peek(it -> {
								//not sure if it propogates exception correctly
								expect(externalSystemRepository.exists(it.getExternalSystemId()), equalTo(true)).verify(
										EXTERNAL_SYSTEM_NOT_FOUND, it.getExternalSystemId());
							}).map(TestItemUtils.externalIssueDtoConverter(submitter)).collect(Collectors.toSet())).orElse(null));
					issue.setIgnoreAnalyzer(BooleanUtils.toBoolean(providedIssue.getIgnoreAnalyzer()));
					testItem.setIssue(issue);
				}
			} else {
				testItem.setIssue(new TestItemIssue());
				/* For AA saving reference initialization */
				//				Launch launch = launchRepository.findOne(testItem.getLaunchRef());
				//				expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, testItem.getLaunchRef());
				//				if (Mode.DEFAULT.equals(launch.getMode()) && project.getConfiguration().getIsAutoAnalyzerEnabled()) {
				//					testItem = analyzeItem(launch.getId(), testItem, project.getConfiguration().getAnalyzeOnTheFly());
				//				}
			}
		}
		return testItem;
	}

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
