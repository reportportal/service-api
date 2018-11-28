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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ItemIssueTypeDefinedEvent;
import com.epam.ta.reportportal.core.events.activity.LinkTicketEvent;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IntegrationIssueConverter;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.launch.util.AttributesValidator.validateAttributes;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
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

	private static final String SKIPPED_ISSUE_KEY = "skippedIssue";

	private final TestItemRepository testItemRepository;

	private final ItemAttributeRepository itemAttributeRepository;

	private final IntegrationRepository integrationRepository;

	private final TicketRepository ticketRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final IssueEntityRepository issueEntityRepository;

	private final MessageBus messageBus;

	@Autowired
	public UpdateTestItemHandlerImpl(TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
			IntegrationRepository integrationRepository, TicketRepository ticketRepository, IssueTypeHandler issueTypeHandler,
			IssueEntityRepository issueEntityRepository, MessageBus messageBus) {
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.integrationRepository = integrationRepository;
		this.ticketRepository = ticketRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.issueEntityRepository = issueEntityRepository;
		this.messageBus = messageBus;
	}

	@Override
	public List<Issue> defineTestItemsIssues(ReportPortalUser.ProjectDetails projectDetails, DefineIssueRQ defineIssue,
			ReportPortalUser user) {
		List<String> errors = new ArrayList<>();
		List<IssueDefinition> definitions = defineIssue.getIssues();
		expect(CollectionUtils.isEmpty(definitions), equalTo(false)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION);
		List<Issue> updated = new ArrayList<>(defineIssue.getIssues().size());
		List<ItemIssueTypeDefinedEvent> events = new ArrayList<>();

		definitions.forEach(issueDefinition -> {
			try {
				TestItem testItem = testItemRepository.findById(issueDefinition.getId())
						.orElseThrow(() -> new BusinessRuleViolationException(Suppliers.formattedSupplier(
								"Cannot update issue type for test item '{}', cause it is not found.",
								issueDefinition.getId()
						).get()));

				verifyTestItem(testItem, issueDefinition.getId());
				TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem);

				Issue issue = issueDefinition.getIssue();
				IssueType issueType = issueTypeHandler.defineIssueType(testItem.getItemId(),
						projectDetails.getProjectId(),
						issue.getIssueType()
				);

				IssueEntity issueEntity = new IssueEntityBuilder(testItem.getItemResults().getIssue()).addIssueType(issueType)
						.addDescription(issue.getComment())
						.addIgnoreFlag(issue.getIgnoreAnalyzer())
						.addAutoAnalyzedFlag(false)
						.get();
				issueEntity.setIssueId(testItem.getItemId());
				testItem.getItemResults().setIssue(issueEntity);

				//TODO EXTERNAL SYSTEM LOGIC, ANALYZER LOGIC
				testItemRepository.save(testItem);
				updated.add(IssueConverter.TO_MODEL.apply(issueEntity));

				events.add(new ItemIssueTypeDefinedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem), user.getUserId()));
			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		});
		expect(!errors.isEmpty(), equalTo(false)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
		events.forEach(messageBus::publishActivity);
		return updated;
	}

	@Override
	public OperationCompletionRS updateTestItem(ReportPortalUser.ProjectDetails projectDetails, Long itemId, UpdateTestItemRQ rq,
			ReportPortalUser user) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));

		validateAttributes(rq.getAttributes());

		Optional<StatusEnum> statusEnum = StatusEnum.fromValue(rq.getStatus());

		if (statusEnum.isPresent()) {
			changeStatus(testItem, statusEnum.get(), user, projectDetails);
		}

		validate(projectDetails, user, testItem);
		testItem = new TestItemBuilder(testItem).addAttributes(rq.getAttributes()).addDescription(rq.getDescription()).get();
		testItemRepository.save(testItem);
		return new OperationCompletionRS("TestItem with ID = '" + testItem.getItemId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> linkExternalIssues(ReportPortalUser.ProjectDetails projectDetails, LinkExternalIssueRQ rq,
			ReportPortalUser user) {
		List<String> errors = new ArrayList<>();

		List<TestItem> testItems = testItemRepository.findAllById(rq.getTestItemIds());
		List<TestItemActivityResource> before = testItems.stream().map(TO_ACTIVITY_RESOURCE).collect(Collectors.toList());

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
		List<TestItemActivityResource> after = testItems.stream().map(TO_ACTIVITY_RESOURCE).collect(Collectors.toList());

		before.forEach(it -> new LinkTicketEvent(it,
				after.stream().filter(t -> t.getId().equals(it.getId())).findFirst().get(),
				user.getUserId()
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
			Ticket apply = IntegrationIssueConverter.TO_TICKET.apply(it);
			apply.setSubmitterId(ofNullable(it.getSubmitter()).orElse(userId));
			apply.setSubmitDate(LocalDateTime.now());
			Optional<Integration> bts = integrationRepository.findById(it.getExternalSystemId());
			expect(bts, isPresent()).verify(INTEGRATION_NOT_FOUND, it.getExternalSystemId());
			apply.setBugTrackingSystemId(it.getExternalSystemId());
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
		Launch launch = testItem.getLaunch();
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ErrorType.ACCESS_DENIED,
					"Launch is not under the specified project."
			);
			if (projectDetails.getProjectRole().lowerThan(ProjectRole.PROJECT_MANAGER)) {
				expect(user.getUsername(), equalTo(launch.getUser().getLogin())).verify(
						ErrorType.ACCESS_DENIED,
						"You are not a launch owner."
				);
			}
		}
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

	private void changeStatus(TestItem testItem, StatusEnum providedStatus, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		StatusEnum actualStatus = testItem.getItemResults().getStatus();
		switch (actualStatus) {
			case PASSED:
				changeStatusFromPassed(testItem, providedStatus, user.getUserId(), projectDetails.getProjectId());
				break;
			case FAILED:
				changeStatusFromFailed(testItem, providedStatus, user.getUserId(), projectDetails.getProjectId());
				break;
			case SKIPPED:
				changeStatusFromSkipped(testItem, providedStatus, user.getUserId(), projectDetails.getProjectId());
				break;
			case INTERRUPTED:
				changeStatusFromInterrupted(testItem, providedStatus, user.getUserId(), projectDetails.getProjectId());
				break;
			default:
				throw new ReportPortalException(INCORRECT_REQUEST,
						"actual status: " + actualStatus + " can not be changed to: " + providedStatus
				);
		}
	}

	private void changeStatusFromInterrupted(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, PASSED, FAILED)).verify(INCORRECT_REQUEST,
				"actual status: " + testItem.getItemResults().getStatus() + " can not be changed to: " + providedStatus
		);

		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem);
		testItem.getItemResults().setStatus(providedStatus);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findSystemAttributeByLaunchIdAndValue(testItem.getLaunch()
				.getId(), SKIPPED_ISSUE_KEY);

		if (FAILED.equals(providedStatus) || (SKIPPED.equals(providedStatus) && skippedIssueAttribute.isPresent()
				&& skippedIssueAttribute.get().getValue().equals("true"))) {
			setToInvestigateIssue(testItem, projectId);
		}

		testItemRepository.save(testItem);
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem), userId));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(testItem, userId);
		}
	}

	private void changeStatusFromPassed(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, FAILED)).verify(INCORRECT_REQUEST,
				"actual status: " + testItem.getItemResults().getStatus() + " can not be changed to: " + providedStatus
		);

		StatusEnum oldParentStatus = testItem.getParent().getItemResults().getStatus();
		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findSystemAttributeByLaunchIdAndValue(testItem.getLaunch()
				.getId(), SKIPPED_ISSUE_KEY);

		testItem.getItemResults().setStatus(providedStatus);
		if (FAILED.equals(providedStatus) || (SKIPPED.equals(providedStatus) && skippedIssueAttribute.isPresent()
				&& skippedIssueAttribute.get().getValue().equals("true"))) {
			setToInvestigateIssue(testItem, projectId);
		}

		testItemRepository.save(testItem);
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem), userId));

		changeParentsStatusesToFailed(testItem, oldParentStatus, userId);
	}

	private void changeStatusFromFailed(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, PASSED)).verify(INCORRECT_REQUEST,
				"actual status: " + testItem.getItemResults().getStatus() + " can not be changed to: " + providedStatus
		);

		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem);
		testItem.getItemResults().setStatus(providedStatus);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findSystemAttributeByLaunchIdAndValue(testItem.getLaunch()
				.getId(), SKIPPED_ISSUE_KEY);

		if (SKIPPED.equals(providedStatus) && skippedIssueAttribute.isPresent() && skippedIssueAttribute.get().getValue().equals("true")) {
			if (testItem.getItemResults().getIssue() == null) {
				setToInvestigateIssue(testItem, projectId);
			}
		} else {
			issueEntityRepository.delete(testItem.getItemResults().getIssue());
			testItem.getItemResults().setIssue(null);
		}

		if (PASSED.equals(providedStatus)) {
			issueEntityRepository.delete(testItem.getItemResults().getIssue());
			testItem.getItemResults().setIssue(null);
		}

		testItemRepository.save(testItem);
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem), userId));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(testItem, userId);
		}
	}

	private void changeStatusFromSkipped(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(PASSED, FAILED)).verify(INCORRECT_REQUEST,
				"actual status: " + testItem.getItemResults().getStatus() + " can not be changed to: " + providedStatus
		);

		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem);
		testItem.getItemResults().setStatus(providedStatus);

		if (PASSED.equals(providedStatus) && testItem.getItemResults().getIssue() != null) {
			issueEntityRepository.delete(testItem.getItemResults().getIssue());
			testItem.getItemResults().setIssue(null);
		}
		if (FAILED.equals(providedStatus) && testItem.getItemResults().getIssue() == null) {
			setToInvestigateIssue(testItem, projectId);
		}

		testItemRepository.save(testItem);
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem), userId));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(testItem, userId);
		}
	}

	private void setToInvestigateIssue(TestItem testItem, Long projectId) {
		IssueEntity issueEntity = new IssueEntity();
		IssueType toInvestigate = issueTypeHandler.defineIssueType(testItem.getItemId(), projectId, TO_INVESTIGATE.getLocator());
		issueEntity.setIssueType(toInvestigate);
		issueEntity.setIssueId(testItem.getItemId());

		issueEntity.setTestItemResults(testItem.getItemResults());
		testItem.getItemResults().setIssue(issueEntity);
	}

	private void changeStatusRecursively(TestItem testItem, Long userId) {
		TestItem parent = testItem.getParent();
		Hibernate.initialize(parent);
		if (parent != null) {
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent);
			StatusEnum newStatus = testItemRepository.hasFailedStatusWithoutStepItem(parent.getItemId(), testItem.getItemId()) ?
					StatusEnum.FAILED :
					StatusEnum.PASSED;
			if (!parent.getItemResults().getStatus().equals(newStatus)) {
				parent.getItemResults().setStatus(newStatus);
				testItemRepository.save(parent);
				messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(parent), userId));
				if (parent.getType().sameLevel(TestItemTypeEnum.SUITE)) {
					testItem.getLaunch().setStatus(newStatus);
				}
				changeStatusRecursively(parent, userId);
			}
		}
	}

	private void changeParentsStatusesToFailed(TestItem testItem, StatusEnum oldParentStatus, Long userId) {
		if (!oldParentStatus.equals(StatusEnum.FAILED)) {
			TestItem parent = testItem.getParent();
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent);
			while (parent != null) {
				parent.getItemResults().setStatus(StatusEnum.FAILED);
				testItemRepository.save(parent);
				messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(parent), userId));
				parent = parent.getParent();
			}
			testItem.getLaunch().setStatus(StatusEnum.FAILED);
		}
	}

}
