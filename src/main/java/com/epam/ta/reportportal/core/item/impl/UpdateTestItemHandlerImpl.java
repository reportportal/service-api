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

import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.Predicates.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.AMBIGUOUS_TEST_ITEM_STATUS;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link UpdateTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
public class UpdateTestItemHandlerImpl implements UpdateTestItemHandler {

	private TestItemRepository testItemRepository;

	private IssueTypeHandler issueTypeHandler;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setIssueTypeHandler(IssueTypeHandler issueTypeHandler) {
		this.issueTypeHandler = issueTypeHandler;
	}

	//	private final ApplicationEventPublisher eventPublisher;
	//	private final StatisticsFacadeFactory statisticsFacadeFactory;
	//	private final UserRepository userRepository;
	//	private final ProjectRepository projectRepository;
	//	private final LaunchRepository launchRepository;
	//	private final ExternalSystemRepository externalSystemRepository;
	//	private final LogIndexerService logIndexer;
	//	private final IssuesAnalyzerService issuesAnalyzerService;
	//
	//	@Autowired
	//	public UpdateTestItemHandlerImpl(TestItemRepository testItemRepository, StatisticsFacadeFactory statisticsFacadeFactory,
	//			UserRepository userRepository, ProjectRepository projectRepository, LaunchRepository launchRepository,
	//			ExternalSystemRepository externalSystemRepository, ApplicationEventPublisher eventPublisher, LogIndexerService logIndexer,
	//			IssuesAnalyzerService issuesAnalyzerService) {
	//		this.eventPublisher = eventPublisher;
	//		this.testItemRepository = testItemRepository;
	//		this.statisticsFacadeFactory = statisticsFacadeFactory;
	//		this.userRepository = userRepository;
	//		this.projectRepository = projectRepository;
	//		this.launchRepository = launchRepository;
	//		this.externalSystemRepository = externalSystemRepository;
	//		this.logIndexer = logIndexer;
	//		this.issuesAnalyzerService = issuesAnalyzerService;
	//	}

	@Override
	public List<Issue> defineTestItemsIssues(String project, DefineIssueRQ defineIssue, String userName) {
		List<String> errors = new ArrayList<>();
		List<IssueDefinition> definitions = defineIssue.getIssues();
		expect(definitions.isEmpty(), equalTo(false)).verify(ErrorType.FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION);

		List<IssueEntity> updated = new ArrayList<>(defineIssue.getIssues().size());

		definitions.forEach(issueDefinition -> {
			try {
				TestItem testItem = testItemRepository.findById(issueDefinition.getId())
						.orElseThrow(() -> new BusinessRuleViolationException(
								Suppliers.formattedSupplier("Cannot update issue type for test item '{}', cause it is not found.",
										issueDefinition.getId()
								).get()));
				verifyTestItem(testItem, issueDefinition.getId());

				Issue issue = issueDefinition.getIssue();
				IssueType issueType = issueTypeHandler.defineIssueType(testItem.getItemId(), 1L, issue.getIssueType());

				IssueEntity itemsIssue = testItem.getTestItemResults().getIssue();
				itemsIssue.setIssueType(issueType);
				if (null != issue.getComment()) {
					itemsIssue.setIssueDescription(issue.getComment().trim());
				}
				itemsIssue.setIgnoreAnalyzer(issue.getIgnoreAnalyzer());
				itemsIssue.setAutoAnalyzed(false);
				testItemRepository.save(testItem);

				//TODO EXTERNAL SYSTEM LOGIC, ANALYZER LOGIC

			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		});
		return null;
	}

	//	@Override
	//	public List<Issue> defineTestItemsIssues(String projectName, DefineIssueRQ defineIssue, String userName) {
	//		List<String> errors = new ArrayList<>();
	//		List<IssueDefinition> definitions = defineIssue.getIssues();
	//
	//		expect(definitions, NOT_EMPTY_COLLECTION).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, "");
	//
	//		List<Issue> updated = new ArrayList<>(defineIssue.getIssues().size());
	//		ImmutableMap.Builder<IssueDefinition, TestItem> eventData = ImmutableMap.builder();
	//		for (IssueDefinition issueDefinition : definitions) {
	//			try {
	//				TestItem testItem = testItemRepository.findOne(issueDefinition.getId());
	//				verifyTestItem(testItem, issueDefinition.getId());
	//				//if item is updated then it is no longer auto analyzed
	//				issueDefinition.getIssue().setAutoAnalyzed(false);
	//				eventData.put(issueDefinition, testItem);
	//
	//				final Launch launch = launchRepository.findOne(testItem.getLaunchRef());
	//
	//				final Project project = projectRepository.findOne(launch.getProjectRef());
	//
	//				Issue issue = issueDefinition.getIssue();
	//				String issueType = verifyTestItemDefinedIssueType(issue.getIssueType(), project.getConfiguration());
	//
	//				testItem = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
	//						.resetIssueStatistics(testItem);
	//
	//				TestItemIssue testItemIssue = testItem.getIssue();
	//				testItemIssue.setIssueType(issueType);
	//
	//				String comment = issueDefinition.getIssue().getComment();
	//				if (null != comment) {
	//					comment = comment.trim();
	//				}
	//
	//				if (null != issue.getExternalSystemIssues()) {
	//					Set<TestItemIssue.ExternalSystemIssue> issuesFromDB =
	//							null == testItemIssue.getExternalSystemIssues() ? new HashSet<>() : testItemIssue.getExternalSystemIssues();
	//					Set<TestItemIssue.ExternalSystemIssue> issuesFromRequest = issue.getExternalSystemIssues()
	//							.stream()
	//							.map(TestItemUtils.externalIssueDtoConverter(userName))
	//							.collect(toSet());
	//					Set<TestItemIssue.ExternalSystemIssue> difference = newHashSet(Sets.difference(issuesFromRequest, issuesFromDB));
	//					if (!difference.isEmpty()) {
	//						for (TestItemIssue.ExternalSystemIssue externalSystemIssue : difference) {
	//							externalSystemIssue.setSubmitter(userName);
	//							externalSystemIssue.setSubmitDate(new Date().getTime());
	//
	//						}
	//						Set<TestItemIssue.ExternalSystemIssue> externalSystemIssues;
	//						if (issuesFromRequest.size() < issuesFromDB.size()) {
	//							issuesFromRequest.removeAll(difference);
	//							issuesFromRequest.addAll(difference);
	//							externalSystemIssues = issuesFromRequest;
	//						} else {
	//							externalSystemIssues = issuesFromDB;
	//							externalSystemIssues.addAll(difference);
	//						}
	//						testItemIssue.setExternalSystemIssues(externalSystemIssues);
	//					} else {
	//						issuesFromDB.removeAll(newHashSet(Sets.difference(issuesFromDB, issuesFromRequest)));
	//						testItemIssue.setExternalSystemIssues(issuesFromDB);
	//					}
	//				}
	//
	//				ofNullable(issue.getIgnoreAnalyzer()).ifPresent(
	//						it -> testItemIssue.setIgnoreAnalyzer(issuesAnalyzerService.hasAnalyzers() && it));
	//				ofNullable(issue.getAutoAnalyzed()).ifPresent(testItemIssue::setAutoAnalyzed);
	//
	//				testItemIssue.setIssueDescription(comment);
	//				testItem.setIssue(testItemIssue);
	//
	//				testItemRepository.save(testItem);
	//				indexLogs(projectName, testItem);
	//
	//				testItem = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
	//						.updateIssueStatistics(testItem);
	//				updated.add(IssueConverter.TO_MODEL.apply(testItem.getIssue()));
	//			} catch (BusinessRuleViolationException e) {
	//				errors.add(e.getMessage());
	//			}
	//		}
	//
	//		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
	//
	//		eventPublisher.publishEvent(new ItemIssueTypeDefined(eventData.build(), userName, projectName));
	//		return updated;
	//	}
	//
	@Override
	public OperationCompletionRS updateTestItem(String projectName, Long itemId, UpdateTestItemRQ rq, String userName) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		validate(projectName, userName, testItem);
		testItem = new TestItemBuilder(testItem).addTags(rq.getTags()).addDescription(rq.getDescription()).get();
		testItemRepository.save(testItem);
		return new OperationCompletionRS("TestItem with ID = '" + testItem.getItemId() + "' successfully updated.");
	}

	//
	//	@Override
	//	public List<OperationCompletionRS> addExternalIssues(String projectName, AddExternalIssueRQ rq, String userName) {
	//		List<String> errors = new ArrayList<>();
	//		ExternalSystem extSystem = externalSystemRepository.findOne(rq.getExternalSystemId());
	//		expect(extSystem, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, rq.getExternalSystemId());
	//
	//		Iterable<TestItem> testItems = testItemRepository.findAll(rq.getTestItemIds());
	//		List<TestItem> before = SerializationUtils.clone(Lists.newArrayList(testItems));
	//		StreamSupport.stream(testItems.spliterator(), false).forEach(testItem -> {
	//			try {
	//				verifyTestItem(testItem, testItem.getId());
	//				Set<TestItemIssue.ExternalSystemIssue> tiIssues = rq.getIssues()
	//						.stream()
	//						.filter(issue -> !issue.getTicketId().trim().isEmpty())
	//						.map(TestItemUtils.externalIssueDtoConverter(rq.getExternalSystemId(), userName))
	//						.collect(toSet());
	//				if (null == testItem.getIssue().getExternalSystemIssues()) {
	//					testItem.getIssue().setExternalSystemIssues(tiIssues);
	//				} else {
	//					tiIssues.addAll(testItem.getIssue().getExternalSystemIssues());
	//					testItem.getIssue().setExternalSystemIssues(tiIssues);
	//				}
	//			} catch (BusinessRuleViolationException e) {
	//				errors.add(e.getMessage());
	//			}
	//		});
	//
	//		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
	//
	//		testItemRepository.save(testItems);
	//		eventPublisher.publishEvent(new TicketAttachedEvent(before, Lists.newArrayList(testItems), userName, projectName));
	//		return StreamSupport.stream(testItems.spliterator(), false)
	//				.map(testItem -> new OperationCompletionRS("TestItem with ID = '" + testItem.getId() + "' successfully updated."))
	//				.collect(toList());
	//	}
	//
	private void validate(String projectName, String userName, TestItem testItem) {
		//
		//
		//		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		//		Project project = projectRepository.findOne(launch.getProjectRef());
		//		String launchOwner = launch.getUserRef();
		//		if (userRepository.findOne(userName).getRole() != UserRole.ADMINISTRATOR) {
		//			expect(projectName, equalTo(project.getName())).verify(ACCESS_DENIED);
		//			if (doesHaveUser(project, userName) && findUserConfigByLogin(project, userName).getProjectRole()
		//					.lowerThan(ProjectRole.PROJECT_MANAGER)) {
		//				expect(userName, equalTo(launchOwner)).verify(ACCESS_DENIED);
		//			}
		//		}
		//		return testItem;
	}
	//
	//	/**
	//	 * Index logs if item is not ignored for analyzer
	//	 * Clean index logs if item is ignored for analyzer
	//	 *
	//	 * @param projectName Project name
	//	 * @param testItem    Test item to reindex
	//	 */
	//	private void indexLogs(String projectName, TestItem testItem) {
	//		if (CAN_BE_INDEXED.test(testItem)) {
	//			logIndexer.indexLogs(testItem.getLaunchRef(), singletonList(testItem));
	//		} else {
	//			logIndexer.cleanIndex(projectName, singletonList(testItem.getId()));
	//		}
	//	}
	//

	private IssueType verifyTestItemDefinedIssueType(String locator, Long testItemId, Long projectId) {
		List<IssueType> projectIssueTypes = testItemRepository.selectIssueLocatorsByProject(projectId);
		return projectIssueTypes.stream()
				.filter(it -> it.getTestItemIssueType().getLocator().equalsIgnoreCase(locator))
				.findAny()
				.orElseThrow(() -> new ReportPortalException(
						AMBIGUOUS_TEST_ITEM_STATUS, formattedSupplier(
						"Invalid test item issue type definition '{}' is requested for item '{}'. Valid issue types locators are: {}",
						locator, testItemId, projectIssueTypes.stream().map(IssueType::getLocator).collect(toList())
				)));
	}

	/**
	 * Complex of domain verification for test item. Verifies that test item
	 * domain object could be processed correctly.
	 *
	 * @param id - test item id
	 * @throws BusinessRuleViolationException when business rule violation
	 */
	private void verifyTestItem(TestItem item, Long id) throws BusinessRuleViolationException {
		expect(item.getTestItemResults().getStatus(), not(equalTo(StatusEnum.PASSED)),
				Suppliers.formattedSupplier("Issue status update cannot be applied on {} test items, cause it is not allowed.",
						StatusEnum.PASSED.name()
				)
		).verify();
		expect(
				testItemRepository.hasChildren(item.getItemId()), equalTo(false), Suppliers.formattedSupplier(
						"It is not allowed to udpate issue type for items with descendants. Test item '{}' has descendants.", id)).verify();

		expect(
				item.getTestItemResults().getIssue(), notNull(), Suppliers.formattedSupplier(
						"Cannot update issue type for test item '{}', cause there is no info about actual issue type value.", id)).verify();

		expect(
				item.getTestItemResults().getIssue().getIssueType(), notNull(), Suppliers.formattedSupplier(
						"Cannot update issue type for test item {}, cause it's actual issue type value is not provided.", id)).verify();
	}

}
