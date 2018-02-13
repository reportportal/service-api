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

import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.impl.IssuesAnalyzerService;
import com.epam.ta.reportportal.core.analyzer.impl.LogIndexerService;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.events.ItemIssueTypeDefined;
import com.epam.ta.reportportal.events.TicketAttachedEvent;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.commons.EntityUtils.update;
import static com.epam.ta.reportportal.commons.Preconditions.NOT_EMPTY_COLLECTION;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.Status.PASSED;
import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.doesHaveUser;
import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Default implementation of {@link UpdateTestItemHandler}
 *
 * @author Dzianis Shlychkou
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateTestItemHandlerImpl implements UpdateTestItemHandler {

	private final ApplicationEventPublisher eventPublisher;
	private final TestItemRepository testItemRepository;
	private final StatisticsFacadeFactory statisticsFacadeFactory;
	private final UserRepository userRepository;
	private final ProjectRepository projectRepository;
	private final LaunchRepository launchRepository;
	private final ExternalSystemRepository externalSystemRepository;
	private final LogIndexerService logIndexer;
	private final IssuesAnalyzerService issuesAnalyzerService;

	@Autowired
	public UpdateTestItemHandlerImpl(TestItemRepository testItemRepository, StatisticsFacadeFactory statisticsFacadeFactory,
			UserRepository userRepository, ProjectRepository projectRepository, LaunchRepository launchRepository,
			ExternalSystemRepository externalSystemRepository, ApplicationEventPublisher eventPublisher, LogIndexerService logIndexer,
			IssuesAnalyzerService issuesAnalyzerService) {
		this.eventPublisher = eventPublisher;
		this.testItemRepository = testItemRepository;
		this.statisticsFacadeFactory = statisticsFacadeFactory;
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
		this.externalSystemRepository = externalSystemRepository;
		this.logIndexer = logIndexer;
		this.issuesAnalyzerService = issuesAnalyzerService;
	}

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

				//if item is updated then it is no longer auto analyzed
				issueDefinition.getIssue().setAutoAnalyzed(false);
				eventData.put(issueDefinition, testItem);

				final Launch launch = launchRepository.findOne(testItem.getLaunchRef());

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
					Set<TestItemIssue.ExternalSystemIssue> issuesFromDB =
							null == testItemIssue.getExternalSystemIssues() ? new HashSet<>() : testItemIssue.getExternalSystemIssues();
					Set<TestItemIssue.ExternalSystemIssue> issuesFromRequest = issue.getExternalSystemIssues()
							.stream()
							.map(TestItemUtils.externalIssueDtoConverter(userName))
							.collect(toSet());
					Set<TestItemIssue.ExternalSystemIssue> difference = newHashSet(Sets.difference(issuesFromRequest, issuesFromDB));
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
						issuesFromDB.removeAll(newHashSet(Sets.difference(issuesFromDB, issuesFromRequest)));
						testItemIssue.setExternalSystemIssues(issuesFromDB);
					}
				}

				testItemIssue.setIgnoreAnalyzer(issuesAnalyzerService.hasAnalyzers() && issue.getIgnoreAnalyzer());
				testItemIssue.setAutoAnalyzed(issue.getAutoAnalyzed());

				testItemIssue.setIssueDescription(comment);
				testItem.setIssue(testItemIssue);

				testItemRepository.save(testItem);
				indexLogs(projectName, testItem);

				testItem = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
						.updateIssueStatistics(testItem);
				updated.add(IssueConverter.TO_MODEL.apply(testItem.getIssue()));
			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		}

		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());

		eventPublisher.publishEvent(new ItemIssueTypeDefined(eventData.build(), userName, projectName));
		return updated;
	}

	@Override
	public OperationCompletionRS updateTestItem(String projectName, String item, UpdateTestItemRQ rq, String userName) {
		TestItem testItem = validate(projectName, userName, item);
		ofNullable(rq.getTags()).ifPresent(tags -> testItem.setTags(newHashSet(trimStrings(update(tags)))));
		ofNullable(rq.getDescription()).ifPresent(testItem::setItemDescription);
		testItemRepository.save(testItem);
		return new OperationCompletionRS("TestItem with ID = '" + item + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> addExternalIssues(String projectName, AddExternalIssueRQ rq, String userName) {
		List<String> errors = new ArrayList<>();
		ExternalSystem extSystem = externalSystemRepository.findOne(rq.getExternalSystemId());
		expect(extSystem, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, rq.getExternalSystemId());

		Iterable<TestItem> testItems = testItemRepository.findAll(rq.getTestItemIds());
		List<TestItem> before = SerializationUtils.clone(Lists.newArrayList(testItems));
		StreamSupport.stream(testItems.spliterator(), false).forEach(testItem -> {
			try {
				verifyTestItem(testItem, testItem.getId());
				Set<TestItemIssue.ExternalSystemIssue> tiIssues = rq.getIssues()
						.stream()
						.filter(issue -> !issue.getTicketId().trim().isEmpty())
						.map(TestItemUtils.externalIssueDtoConverter(rq.getExternalSystemId(), userName))
						.collect(toSet());
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

	private TestItem validate(String projectName, String userName, String id) {
		TestItem testItem = testItemRepository.findOne(id);
		expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, id);

		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		Project project = projectRepository.findOne(launch.getProjectRef());
		String launchOwner = launch.getUserRef();
		if (userRepository.findOne(userName).getRole() != UserRole.ADMINISTRATOR) {
			expect(projectName, equalTo(project.getName())).verify(ACCESS_DENIED);
			if (doesHaveUser(project, userName) && findUserConfigByLogin(project, userName).getProjectRole()
					.lowerThan(ProjectRole.PROJECT_MANAGER)) {
				expect(userName, equalTo(launchOwner)).verify(ACCESS_DENIED);
			}
		}
		return testItem;
	}

	/**
	 * Index logs if item is not ignored for analyzer
	 * Clean index logs if item is ignored for analyzer
	 *
	 * @param projectName Project name
	 * @param testItem    Test item to reindex
	 */
	private void indexLogs(String projectName, TestItem testItem) {
		if (ITEM_CAN_BE_INDEXED.test(testItem)) {
			logIndexer.indexLogs(testItem.getLaunchRef(), singletonList(testItem));
		} else {
			logIndexer.cleanIndex(projectName, singletonList(testItem.getId()));
		}
	}

	/**
	 * Verifies that provided test item issue type is valid, and test item
	 * domain object could be processed correctly
	 *
	 * @param type     - provided issue type
	 * @param settings - project settings
	 * @return verified issue type
	 */
	private String verifyTestItemDefinedIssueType(final String type, final Project.Configuration settings) {
		StatisticSubType defined = settings.getByLocator(type);
		expect(defined, notNull()).verify(AMBIGUOUS_TEST_ITEM_STATUS,
				formattedSupplier("Invalid test item issue type definition '{}'. Valid issue types locators are: {}", type,
						settings.getSubTypes()
								.values()
								.stream()
								.flatMap(Collection::stream)
								.map(StatisticSubType::getLocator)
								.collect(Collectors.toList())
				)
		);
		return defined.getLocator();
	}

	/**
	 * Complex of domain verification for test item. Verifies that test item
	 * domain object could be processed correctly.
	 *
	 * @param id - test item id
	 * @throws BusinessRuleViolationException when business rule violation
	 */
	private void verifyTestItem(TestItem testItem, String id) throws BusinessRuleViolationException {
		expect(
				testItem, notNull(), Suppliers.formattedSupplier("Cannot update issue type for test item '{}', cause it is not found.", id))
				.verify();

		expect(testItem.getStatus(), not(equalTo(PASSED)),
				Suppliers.formattedSupplier("Issue status update cannot be applied on {} test items, cause it is not allowed.",
						PASSED.name()
				)
		).verify();

		expect(
				testItem.hasChilds(), not(equalTo(TRUE)), Suppliers.formattedSupplier(
						"It is not allowed to udpate issue type for items with descendants. Test item '{}' has descendants.", id)).verify();

		expect(
				testItem.getIssue(), notNull(), Suppliers.formattedSupplier(
						"Cannot update issue type for test item '{}', cause there is no info about actual issue type value.", id)).verify();

		expect(
				testItem.getIssue().getIssueType(), notNull(), Suppliers.formattedSupplier(
						"Cannot update issue type for test item {}, cause it's actual issue type value is not provided.", id)).verify();
	}

}
