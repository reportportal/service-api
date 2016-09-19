/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.validValues;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.FailReferenceResource;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.builders.FailReferenceResourceBuilder;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.launch.Mode;

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

	private ProjectSettingsRepository settingsRepository;
	private ProjectRepository projectRepository;
	private LaunchRepository launchRepository;
	private TestItemRepository testItemRepository;
	private StatisticsFacadeFactory statisticsFacadeFactory;
	private FailReferenceResourceRepository issuesRepository;
	private LazyReference<FailReferenceResourceBuilder> failReferenceResourceBuilder;

	@Autowired
	public void setProjectSettingsRepository(ProjectSettingsRepository settingsRepo) {
		this.settingsRepository = settingsRepo;
	}

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
	public void setFailReferenceResourceRepository(FailReferenceResourceRepository issuesRepository) {
		this.issuesRepository = issuesRepository;
	}

	@Autowired
	@Qualifier("failReferenceResourceBuilder.reference")
	public void setFailReferenceResourceBuilder(LazyReference<FailReferenceResourceBuilder> failReferenceResourceBuilder) {
		this.failReferenceResourceBuilder = failReferenceResourceBuilder;
	}

	@Override
	public OperationCompletionRS finishTestItem(String testItemId, FinishTestItemRQ finishExecutionRQ, String username) {
		TestItem testItem = verifyTestItem(testItemId, finishExecutionRQ, fromValue(finishExecutionRQ.getStatus()));
		testItem.setEndTime(finishExecutionRQ.getEndTime());

		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, testItem.getLaunchRef());
		if (!launch.getUserRef().equalsIgnoreCase(username))
			fail().withError(FINISH_ITEM_NOT_ALLOWED, "You are not launch owner.");
		final ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		final Project project = projectRepository.findOne(launch.getProjectRef());

		Optional<Status> actualStatus = fromValue(finishExecutionRQ.getStatus());
		Issue providedIssue = finishExecutionRQ.getIssue();
		boolean statusProvided = actualStatus.isPresent();
		boolean hasDescendants = testItemRepository.hasDescendants(testItem.getId());

		/*
		 * If test item has descendants, it's status is resolved from statistics
		 * When status provided, no meter test item has or not descendants, test
		 * item status is resolved to provided
		 */
		if (!statusProvided && hasDescendants) {
			testItem.setStatus(StatisticsHelper.getStatusFromStatistics(testItem.getStatistics()));
		} else {
			testItem.setStatus(actualStatus.get());
		}

		/*
		 * Updates ancestors issue statistics, bases on status. Only for test
		 * items that does not have descendants
		 */
		if (!hasDescendants) {
			testItem = awareTestItemIssueTypeFromStatus(testItem, providedIssue, settings, project);
		}
		try {
			testItemRepository.save(testItem);
			if (!hasDescendants) {
				testItem = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
						.updateExecutionStatistics(testItem);
				if (null != testItem.getIssue()) {
					statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
							.updateIssueStatistics(testItem);
				}
			}
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating TestItem " + e.getMessage(), e);
		}

		return new OperationCompletionRS("TestItem with ID = '" + testItemId + "' successfully finished.");
	}

	/**
	 * Validation procedure for specified test item
	 *
	 * @param testItemId
	 * @param finishExecutionRQ
	 * @param actualStatus
	 * @return TestItem
	 */
	private TestItem verifyTestItem(final String testItemId, FinishTestItemRQ finishExecutionRQ, Optional<Status> actualStatus) {
		TestItem testItem = testItemRepository.findOne(testItemId);
		try {
			expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, testItemId);
			expect(testItem, not(Preconditions.TEST_ITEM_FINISHED)).verify(REPORTING_ITEM_ALREADY_FINISHED, testItem.getId());

			boolean statusProvided = actualStatus.isPresent();

			List<TestItem> descendants = testItemRepository.findDescendants(testItem.getId());
			boolean hasDescendants = !descendants.isEmpty();

			expect(!statusProvided && !hasDescendants, Predicates.equalTo(Boolean.FALSE),
					formattedSupplier(
							"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
							testItemId)).verify();

			expect(descendants, not(Preconditions.HAS_IN_PROGRESS_ITEMS)).verify(FINISH_ITEM_NOT_ALLOWED, formattedSupplier(
					"Test item '{}' has descendants with '{}' status. All descendants '{}'", testItemId, IN_PROGRESS.name(), descendants));

			expect(finishExecutionRQ, Preconditions.finishSameTimeOrLater(testItem.getStartTime()))
					.verify(FINISH_TIME_EARLIER_THAN_START_TIME, finishExecutionRQ.getEndTime(), testItem.getStartTime(), testItemId);

			/*
			 * If there is issue provided we have to be sure issue type is
			 * correct
			 */
		} catch (BusinessRuleViolationException e) {
			fail().withError(AMBIGUOUS_TEST_ITEM_STATUS, e.getMessage());
		}
		return testItem;
	}

	void verifyIssue(String testItemId, Issue issue, ProjectSettings projectSettings) {
		if (issue != null && !NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(issue.getIssueType())) {
			expect(projectSettings.getByLocator(issue.getIssueType()), notNull()).verify(AMBIGUOUS_TEST_ITEM_STATUS,
					formattedSupplier("Invalid test item issue type definition '{}' is requested for item '{}'. Valid issue types are: {}",
							issue.getIssueType(), testItemId, validValues()));
		}
	}

	/**
	 * Issue type recognition for specified test item from
	 *
	 * @param testItem
	 * @param providedIssue
	 * @return TestItem
	 */
	TestItem awareTestItemIssueTypeFromStatus(final TestItem testItem, final Issue providedIssue, final ProjectSettings settings,
			final Project project) {
		if (FAILED.equals(testItem.getStatus()) || SKIPPED.equals(testItem.getStatus())) {
			if (null != providedIssue) {
				verifyIssue(testItem.getId(), providedIssue, settings);
				String issueType = providedIssue.getIssueType();
				if (!issueType.equalsIgnoreCase(NOT_ISSUE_FLAG.getValue())) {
					testItem.setIssue(new TestItemIssue(settings.getByLocator(issueType).getLocator(), providedIssue.getComment()));
				}
			} else {
				testItem.setIssue(new TestItemIssue());
				/* For AA saving reference initialization */
				Launch launch = launchRepository.findOne(testItem.getLaunchRef());
				expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, testItem.getLaunchRef());
				if (Mode.DEFAULT.equals(launch.getMode()) && project.getConfiguration().getIsAutoAnalyzerEnabled())
					finalizeFailed(testItem);
			}
		}
		return testItem;
	}

	/**
	 * Add test item reference into specific repository for AA processing after.
	 *
	 * @param testItem
	 */
	private void finalizeFailed(final TestItem testItem) {
		FailReferenceResource resource = failReferenceResourceBuilder.get().addLaunchRef(testItem.getLaunchRef())
				.addTestItemRef(testItem.getId()).build();
		issuesRepository.save(resource);
	}
}