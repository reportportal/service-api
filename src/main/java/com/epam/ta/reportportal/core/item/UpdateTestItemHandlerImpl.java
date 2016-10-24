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

import static com.epam.ta.reportportal.commons.Preconditions.NOT_EMPTY_COLLECTION;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.database.entity.Status.PASSED;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.*;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.*;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.events.ItemIssueTypeDefined;
import com.epam.ta.reportportal.events.TicketAttachedEvent;
import com.epam.ta.reportportal.util.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Default implementation of {@link UpdateTestItemHandler}
 * 
 * @author Dzianis Shlychkou
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateTestItemHandlerImpl implements UpdateTestItemHandler {

	@Autowired
	private IIssuesAnalyzer analyzerService;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Override
	public List<Issue> defineTestItemsIssues(String projectName, DefineIssueRQ defineIssue, String userName) {
		List<String> errors = new ArrayList<>();
		List<IssueDefinition> definitions = defineIssue.getIssues();

		expect(definitions, NOT_EMPTY_COLLECTION).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, "");

		List<Issue> updated = new ArrayList<>(defineIssue.getIssues().size());
		ImmutableMap.Builder<IssueDefinition, TestItem> eventData = ImmutableMap.builder();
		for (IssueDefinition issueDefinition : definitions) {
			try {
				TestItem testItem = testItemRepository.findOne(issueDefinition.getId());
				verifyTestItem(testItem, issueDefinition.getId());

				eventData.put(issueDefinition, testItem);

				final Launch launch = launchRepository.findOne(testItem.getLaunchRef());
				expect(analyzerService.isPossible(launch.getId()), equalTo(true)).verify(FORBIDDEN_OPERATION,
						Suppliers.formattedSupplier(
								"Cannot update specified '{}' Test Item cause target Launch '{}' is processing by Auto-Analyze",
								testItem.getId(), launch.getId()));

				final Project project = projectRepository.findOne(launch.getProjectRef());

				Issue issue = issueDefinition.getIssue();
				String issueType = verifyTestItemDefinedIssueType(issue.getIssueType(), project.getConfiguration());

				testItem = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
						.resetIssueStatistics(testItem);

				TestItemIssue testItemIssue = testItem.getIssue();
				testItemIssue.setIssueType(issueType);

				String comment = issueDefinition.getIssue().getComment();
				if (null != comment) {
					comment = comment.trim();
				}

				if (null != issue.getExternalSystemIssues()) {
					Set<TestItemIssue.ExternalSystemIssue> issuesFromDB = null == testItemIssue.getExternalSystemIssues() ? new HashSet<>()
							: testItemIssue.getExternalSystemIssues();
					Set<TestItemIssue.ExternalSystemIssue> issuesFromRequest = toDbExternalIssues(issue.getExternalSystemIssues(),
							userName);
					Set<TestItemIssue.ExternalSystemIssue> difference = Sets.newHashSet(Sets.difference(issuesFromRequest, issuesFromDB));
					if (!difference.isEmpty()) {
						for (TestItemIssue.ExternalSystemIssue externalSystemIssue : difference) {
							externalSystemIssue.setSubmitter(userName);
							externalSystemIssue.setSubmitDate(new Date().getTime());

						}
						Set<TestItemIssue.ExternalSystemIssue> externalSystemIssues;
						if (issuesFromRequest.size() < issuesFromDB.size()) {
							issuesFromRequest.removeAll(difference);
							issuesFromRequest.addAll(difference);
							externalSystemIssues = issuesFromRequest;
						} else {
							externalSystemIssues = issuesFromDB;
							externalSystemIssues.addAll(difference);
						}
						testItemIssue.setExternalSystemIssues(externalSystemIssues);
					} else {
						issuesFromDB.removeAll(Sets.newHashSet(Sets.difference(issuesFromDB, issuesFromRequest)));
						testItemIssue.setExternalSystemIssues(issuesFromDB);
					}
				}

				testItemIssue.setIssueDescription(comment);
				testItem.setIssue(testItemIssue);

				testItemRepository.save(testItem);
				testItem = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
						.updateIssueStatistics(testItem);
				updated.add(toIssue(testItem));

			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		}

		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());

		eventPublisher.publishEvent(new ItemIssueTypeDefined(eventData.build(), userName, projectName));
		return updated;
	}

	/**
	 * Complex of domain verification for test item. Verifies that test item
	 * domain object could be processed correctly.
	 * 
	 * @param id
	 *            - test item id
	 * @return verified test item
	 * @throws BusinessRuleViolationException
	 *             when business rule violation
	 */
	public void verifyTestItem(TestItem testItem, String id) throws BusinessRuleViolationException {
		expect(testItem, notNull(), Suppliers.formattedSupplier("Cannot update issue type for test item '{}', cause it is not found.", id))
				.verify();

		Status actualStatus = testItem.getStatus();
		expect(actualStatus, not(equalTo(PASSED)), Suppliers
				.formattedSupplier("Issue status update cannot be applied on {} test items, cause it is not allowed.", PASSED.name()))
						.verify();

		boolean hasDescendants = testItemRepository.hasDescendants(testItem.getId());
		expect(hasDescendants, not(equalTo(TRUE)), Suppliers.formattedSupplier(
				"It is not allowed to udpate issue type for items with descendants. Test item '{}' has descendants.", id)).verify();

		TestItemIssue actualItemIssue = testItem.getIssue();
		expect(actualItemIssue, notNull(), Suppliers.formattedSupplier(
				"Cannot update issue type for test item '{}', cause there is no info about actual issue type value.", id)).verify();

		String actualIssueType = actualItemIssue.getIssueType();
		expect(actualIssueType, notNull(), Suppliers
				.formattedSupplier("Cannot update issue type for test item {}, cause it's actual issue type value is not provided.", id))
						.verify();
	}

	/**
	 * 
	 * Verifies that provided test item issue type is valid, and test item
	 * domain object could be processed correctly
	 * 
	 * @param type
	 *            - provided issue type
	 * @param settings
	 *            - project settings
	 * @return verified issue type
	 */
	public String verifyTestItemDefinedIssueType(final String type, final Project.Configuration settings) {
		StatisticSubType defined = settings.getByLocator(type);
		expect(settings.getByLocator(type), notNull()).verify(ISSUE_TYPE_NOT_FOUND, type);
		return defined.getLocator();
	}

	@Override
	public OperationCompletionRS updateTestItem(String projectName, String item, UpdateTestItemRQ rq, String userName) {
		TestItem testItem = validate(projectName, userName, item);
		if (null != rq.getTags() || null != rq.getDescription()) {
			if (null != rq.getTags()) {
				testItem.setTags(Sets.newHashSet(EntityUtils.trimStrings(EntityUtils.update(rq.getTags()))));
			}
			if (null != rq.getDescription()) {
				testItem.setItemDescription(rq.getDescription().trim());
			}
			testItemRepository.save(testItem);
		}
		return new OperationCompletionRS("TestItem with ID = '" + item + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> addExternalIssues(String projectName, AddExternalIssueRQ rq, String userName) {
		List<String> errors = new ArrayList<>();
		ExternalSystem extSystem = externalSystemRepository.findOne(rq.getExternalSystemId());
		expect(extSystem, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, rq.getExternalSystemId());

		Iterable<TestItem> testItems = testItemRepository.findAll(rq.getTestItemIds());
		List<TestItem> before = Lists.newArrayList(testItems);
		StreamSupport.stream(testItems.spliterator(), false).forEach(testItem -> {
			try {
				verifyTestItem(testItem, testItem.getId());
				Set<TestItemIssue.ExternalSystemIssue> tiIssues = buildExternalSystemIssues(rq, userName);
				if (null == testItem.getIssue().getExternalSystemIssues()) {
					testItem.getIssue().setExternalSystemIssues(tiIssues);
				} else {
					tiIssues.addAll(testItem.getIssue().getExternalSystemIssues());
					testItem.getIssue().setExternalSystemIssues(tiIssues);
				}
			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		});

		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());

		testItemRepository.save(testItems);
		eventPublisher.publishEvent(new TicketAttachedEvent(before, Lists.newArrayList(testItems), userName, projectName));
		return StreamSupport.stream(testItems.spliterator(), false)
				.map(testItem -> new OperationCompletionRS("TestItem with ID = '" + testItem.getId() + "' successfully updated."))
				.collect(toList());
	}

	private Set<TestItemIssue.ExternalSystemIssue> buildExternalSystemIssues(AddExternalIssueRQ rq, String userName) {
		return rq.getIssues().stream().filter(issue -> !issue.getTicketId().trim().isEmpty()).map(issue -> {
			TestItemIssue.ExternalSystemIssue externalSystemIssue = new TestItemIssue.ExternalSystemIssue();
			externalSystemIssue.setTicketId(issue.getTicketId().trim());
			externalSystemIssue.setSubmitDate(new Date().getTime());
			externalSystemIssue.setSubmitter(userName);
			externalSystemIssue.setExternalSystemId(rq.getExternalSystemId());
			externalSystemIssue.setUrl(issue.getUrl());
			return externalSystemIssue;
		}).collect(toSet());
	}

	private Issue toIssue(TestItem item) {
		Issue issue = new Issue();
		TestItemIssue itemIssue = item.getIssue();
		issue.setComment(itemIssue.getIssueDescription());
		issue.setIssueType(itemIssue.getIssueType());
		if (null != item.getIssue().getExternalSystemIssues()) {
			issue.setExternalSystemIssues(toUiExternalIssues(item.getIssue().getExternalSystemIssues()));
		}
		return issue;
	}

	private Set<TestItemIssue.ExternalSystemIssue> toDbExternalIssues(Set<Issue.ExternalSystemIssue> issues, String userName) {
		return issues.stream().map(externalSystemIssue -> {
			TestItemIssue.ExternalSystemIssue dbExternalSystemIssue = new TestItemIssue.ExternalSystemIssue();
			dbExternalSystemIssue.setSubmitDate(new Date().getTime());
			dbExternalSystemIssue.setSubmitter(userName);
			dbExternalSystemIssue.setTicketId(externalSystemIssue.getTicketId());
			dbExternalSystemIssue.setExternalSystemId(externalSystemIssue.getExternalSystemId());
			dbExternalSystemIssue.setUrl(externalSystemIssue.getUrl());
			return dbExternalSystemIssue;
		}).collect(toSet());
	}

	private Set<Issue.ExternalSystemIssue> toUiExternalIssues(Set<TestItemIssue.ExternalSystemIssue> issues) {
		return issues.stream().map(externalSystemIssue -> {
			Issue.ExternalSystemIssue dbExternalSystemIssue = new Issue.ExternalSystemIssue();
			dbExternalSystemIssue.setSubmitDate(externalSystemIssue.getSubmitDate());
			dbExternalSystemIssue.setSubmitter(externalSystemIssue.getSubmitter());
			dbExternalSystemIssue.setTicketId(externalSystemIssue.getTicketId());
			dbExternalSystemIssue.setExternalSystemId(externalSystemIssue.getExternalSystemId());
			dbExternalSystemIssue.setUrl(externalSystemIssue.getUrl());
			return dbExternalSystemIssue;
		}).collect(toSet());
	}

	private TestItem validate(String projectName, String userName, String id) {
		TestItem testItem = testItemRepository.findOne(id);
		expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, id);

		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		Project project = projectRepository.findOne(launch.getProjectRef());
		String launchOwner = launch.getUserRef();
		if (userRepository.findOne(userName).getRole() != UserRole.ADMINISTRATOR) {
			expect(projectName, equalTo(project.getName())).verify(ACCESS_DENIED);
			if (project.getUsers().containsKey(userName) && project.getUsers().get(userName).getProjectRole().getRoleLevel() < 2) {
				expect(userName, equalTo(launchOwner)).verify(ACCESS_DENIED);
			}
		}
		return testItem;
	}
}