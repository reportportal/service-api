/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ItemIssueTypeDefinedEvent;
import com.epam.ta.reportportal.core.events.activity.LinkTicketEvent;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.converter.converters.TicketConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.lang.Boolean.FALSE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Default implementation of {@link UpdateTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
public class UpdateTestItemHandlerImpl implements UpdateTestItemHandler {

	private final ProjectRepository projectRepository;

	private final TestItemRepository testItemRepository;

	private final LogRepository logRepository;

	private final TicketRepository ticketRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final MessageBus messageBus;

	private final LogIndexer logIndexer;

	private final IssueEntityRepository issueEntityRepository;

	private final Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

	@Autowired
	public UpdateTestItemHandlerImpl(ProjectRepository projectRepository, TestItemRepository testItemRepository,
			LogRepository logRepository, TicketRepository ticketRepository, IssueTypeHandler issueTypeHandler, MessageBus messageBus,
			LogIndexer logIndexer, IssueEntityRepository issueEntityRepository,
			Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping) {
		this.projectRepository = projectRepository;
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
		this.ticketRepository = ticketRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.messageBus = messageBus;
		this.logIndexer = logIndexer;
		this.issueEntityRepository = issueEntityRepository;
		this.statusChangingStrategyMapping = statusChangingStrategyMapping;
	}

	@Override
	public List<Issue> defineTestItemsIssues(ReportPortalUser.ProjectDetails projectDetails, DefineIssueRQ defineIssue,
			ReportPortalUser user) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(project);

		List<String> errors = new ArrayList<>();
		List<IssueDefinition> definitions = defineIssue.getIssues();
		expect(CollectionUtils.isEmpty(definitions), equalTo(false)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION);
		List<Issue> updated = new ArrayList<>(defineIssue.getIssues().size());
		List<ItemIssueTypeDefinedEvent> events = new ArrayList<>();

		// key - launch id, value - list of item ids
		Map<Long, List<Long>> logsToReindexMap = new HashMap<>();
		List<Long> logIdsToCleanIndex = new ArrayList<>();

		definitions.forEach(issueDefinition -> {
			try {
				TestItem testItem = testItemRepository.findById(issueDefinition.getId())
						.orElseThrow(() -> new BusinessRuleViolationException(Suppliers.formattedSupplier(
								"Cannot update issue type for test item '{}', cause it is not found.",
								issueDefinition.getId()
						).get()));

				verifyTestItem(testItem, issueDefinition.getId());
				TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectDetails.getProjectId());

				Issue issue = issueDefinition.getIssue();
				IssueType issueType = issueTypeHandler.defineIssueType(projectDetails.getProjectId(), issue.getIssueType());

				IssueEntity issueEntity = new IssueEntityBuilder(testItem.getItemResults().getIssue()).addIssueType(issueType)
						.addDescription(issue.getComment())
						.addIgnoreFlag(issue.getIgnoreAnalyzer())
						.addAutoAnalyzedFlag(issue.getAutoAnalyzed())
						.get();
				issueEntity.setTestItemResults(testItem.getItemResults());
				issueEntityRepository.save(issueEntity);
				testItem.getItemResults().setIssue(issueEntity);

				testItemRepository.save(testItem);

				if (ITEM_CAN_BE_INDEXED.test(testItem)) {
					Long launchId = testItem.getLaunch().getId();
					Long itemId = testItem.getItemId();
					if (logsToReindexMap.containsKey(launchId)) {
						logsToReindexMap.get(launchId).add(itemId);
					} else {
						List<Long> itemIds = Lists.newArrayList();
						itemIds.add(itemId);
						logsToReindexMap.put(launchId, itemIds);
					}
				} else {
					logIdsToCleanIndex.addAll(logRepository.findIdsByTestItemId(testItem.getItemId()));
				}

				updated.add(IssueConverter.TO_MODEL.apply(issueEntity));

				TestItemActivityResource after = TO_ACTIVITY_RESOURCE.apply(testItem, projectDetails.getProjectId());

				events.add(new ItemIssueTypeDefinedEvent(before, after, user.getUserId(), user.getUsername()));
			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		});
		expect(errors.isEmpty(), equalTo(true)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
		if (!logsToReindexMap.isEmpty()) {
			logsToReindexMap.forEach((key, value) -> logIndexer.indexLogs(project.getId(), key, value, analyzerConfig));
		}
		if (!logIdsToCleanIndex.isEmpty()) {
			logIndexer.cleanIndex(project.getId(), logIdsToCleanIndex);
		}
		events.forEach(messageBus::publishActivity);
		return updated;
	}

	@Override
	public OperationCompletionRS updateTestItem(ReportPortalUser.ProjectDetails projectDetails, Long itemId, UpdateTestItemRQ rq,
			ReportPortalUser user) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));

		validate(projectDetails, user, testItem);

		Optional<StatusEnum> providedStatus = StatusEnum.fromValue(rq.getStatus());
		if (providedStatus.isPresent()) {
			expect(testItem.isHasChildren() && !testItem.getType().sameLevel(TestItemTypeEnum.STEP), Predicate.isEqual(false)).verify(
					INCORRECT_REQUEST,
					"Unable to change status on test item with children"
			);
			StatusEnum actualStatus = testItem.getItemResults().getStatus();
			StatusChangingStrategy strategy = statusChangingStrategyMapping.get(actualStatus);
			expect(strategy, notNull()).verify(INCORRECT_REQUEST,
					"Actual status: " + actualStatus + " can not be changed to: " + providedStatus.get()
			);
			strategy.changeStatus(testItem, providedStatus.get(), user, projectDetails.getProjectId());
		}
		testItem = new TestItemBuilder(testItem).overwriteAttributes(rq.getAttributes()).addDescription(rq.getDescription()).get();
		testItemRepository.save(testItem);
		return new OperationCompletionRS("TestItem with ID = '" + testItem.getItemId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> linkExternalIssues(ReportPortalUser.ProjectDetails projectDetails, LinkExternalIssueRQ rq,
			ReportPortalUser user) {
		List<String> errors = new ArrayList<>();

		List<TestItem> testItems = testItemRepository.findAllById(rq.getTestItemIds());
		List<TestItemActivityResource> before = testItems.stream()
				.map(it -> TO_ACTIVITY_RESOURCE.apply(it, projectDetails.getProjectId()))
				.collect(Collectors.toList());

		List<Ticket> existedTickets = collectExistedTickets(rq);
		Set<Ticket> ticketsFromRq = collectTickets(rq, user.getUserId());

		testItems.forEach(testItem -> {
			try {
				verifyTestItem(testItem, testItem.getItemId());
				IssueEntity issue = testItem.getItemResults().getIssue();
				issue.getTickets().addAll(existedTickets);
				issue.getTickets().addAll(ticketsFromRq);
			} catch (Exception e) {
				errors.add(e.getMessage());
			}
		});
		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
		testItemRepository.saveAll(testItems);
		List<TestItemActivityResource> after = testItems.stream()
				.map(it -> TO_ACTIVITY_RESOURCE.apply(it, projectDetails.getProjectId()))
				.collect(Collectors.toList());

		before.forEach(it -> new LinkTicketEvent(it,
				after.stream().filter(t -> t.getId().equals(it.getId())).findFirst().get(),
				user.getUserId(),
				user.getUsername()
		));
		return testItems.stream()
				.map(testItem -> new OperationCompletionRS("TestItem with ID = '" + testItem.getItemId() + "' successfully updated."))
				.collect(toList());
	}

	@Override
	public List<OperationCompletionRS> unlinkExternalIssues(ReportPortalUser.ProjectDetails projectDetails, UnlinkExternalIssueRq rq,
			ReportPortalUser user) {
		List<String> errors = new ArrayList<>();
		List<TestItem> testItems = testItemRepository.findAllById(rq.getTestItemIds());
		testItems.forEach(testItem -> {
			try {
				verifyTestItem(testItem, testItem.getItemId());
				testItem.getItemResults().getIssue().getTickets().removeIf(it -> rq.getIssueIds().contains(it.getTicketId()));
			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		});
		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
		testItemRepository.saveAll(testItems);
		return testItems.stream()
				.map(testItem -> new OperationCompletionRS("TestItem with ID = '" + testItem.getItemId() + "' successfully updated."))
				.collect(toList());
	}

	@Override
	public void resetItemsIssue(List<Long> itemIds, Long projectId) {
		itemIds.forEach(itemId -> {
			IssueType issueType = issueTypeHandler.defineIssueType(projectId, TestItemIssueGroup.TO_INVESTIGATE.getLocator());
			IssueEntity issueEntity = new IssueEntityBuilder(issueEntityRepository.findById(itemId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.ISSUE_TYPE_NOT_FOUND, itemId))).addIssueType(issueType)
					.addAutoAnalyzedFlag(true)
					.get();
			issueEntityRepository.save(issueEntity);
		});
	}

	/**
	 * Finds tickets that are existed in db and removes them from request.
	 *
	 * @param rq Request
	 * @return List of existed tickets in db.
	 */
	private List<Ticket> collectExistedTickets(LinkExternalIssueRQ rq) {
		List<Ticket> existedTickets = ticketRepository.findByTicketIdIn(rq.getIssues()
				.stream()
				.map(Issue.ExternalSystemIssue::getTicketId)
				.collect(toList()));
		List<String> existedTicketsIds = existedTickets.stream().map(Ticket::getTicketId).collect(toList());
		rq.getIssues().removeIf(it -> existedTicketsIds.contains(it.getTicketId()));
		return existedTickets;
	}

	/**
	 * TODO document this
	 *
	 * @param rq     {@link LinkExternalIssueRQ}
	 * @param userId {@link ReportPortalUser#userId}
	 * @return {@link Set} of the {@link Ticket}
	 */
	private Set<Ticket> collectTickets(LinkExternalIssueRQ rq, Long userId) {
		return rq.getIssues().stream().map(it -> {
			Ticket apply = TicketConverter.TO_TICKET.apply(it);
			apply.setSubmitterId(ofNullable(it.getSubmitter()).orElse(userId));
			apply.setSubmitDate(LocalDateTime.now());
			apply.setBtsUrl(it.getBtsUrl());
			apply.setBtsProject(it.getBtsProject());
			return apply;
		}).collect(toSet());
	}

	/**
	 * Validates test item access ability.
	 *
	 * @param projectDetails Project
	 * @param user           User
	 * @param testItem       Test Item
	 */
	private void validate(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, TestItem testItem) {
		Launch launch = ofNullable(testItem.getLaunch()).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND));
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED,
					"Launch is not under the specified project."
			);
			if (projectDetails.getProjectRole().lowerThan(ProjectRole.PROJECT_MANAGER)) {
				expect(user.getUsername(), Predicate.isEqual(launch.getUser().getLogin())).verify(ACCESS_DENIED,
						"You are not a launch owner."
				);
			}
		}
	}

	/**
	 * Complex of domain verification for test item. Verifies that test item
	 * domain object could be processed correctly.
	 *
	 * @param id - test item id
	 * @throws BusinessRuleViolationException when business rule violation
	 */
	private void verifyTestItem(TestItem item, Long id) throws BusinessRuleViolationException {
		expect(item.getItemResults(),
				notNull(),
				Suppliers.formattedSupplier("Test item results were not found for test item with id = '{}", item.getItemId())
		).verify();

		expect(item.getItemResults().getStatus(), not(equalTo(StatusEnum.PASSED)), Suppliers.formattedSupplier(
				"Issue status update cannot be applied on {} test items, cause it is not allowed.",
				StatusEnum.PASSED.name()
		)).verify();

		expect(testItemRepository.hasChildren(item.getItemId(), item.getPath()),
				equalTo(false),
				Suppliers.formattedSupplier(
						"It is not allowed to udpate issue type for items with descendants. Test item '{}' has descendants.",
						id
				)
		).verify();

		expect(item.getItemResults().getIssue(),
				notNull(),
				Suppliers.formattedSupplier(
						"Cannot update issue type for test item '{}', cause there is no info about actual issue type value.",
						id
				)
		).verify();

		expect(item.getItemResults().getIssue().getIssueType(),
				notNull(),
				Suppliers.formattedSupplier("Cannot update issue type for test item {}, cause it's actual issue type value is not provided.",
						id
				)
		).verify();
	}
}
