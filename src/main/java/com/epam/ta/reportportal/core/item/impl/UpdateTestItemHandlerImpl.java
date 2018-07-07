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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemIssueConverter;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.EXTERNAL_SYSTEM_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION;
import static java.lang.Boolean.FALSE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * Default implementation of {@link UpdateTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
public class UpdateTestItemHandlerImpl implements UpdateTestItemHandler {

	private TestItemRepository testItemRepository;

	private BugTrackingSystemRepository bugTrackingSystemRepository;

	private TicketRepository ticketRepository;

	private IssueTypeHandler issueTypeHandler;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setIssueTypeHandler(IssueTypeHandler issueTypeHandler) {
		this.issueTypeHandler = issueTypeHandler;
	}

	@Autowired
	public void setBugTrackingSystemRepository(BugTrackingSystemRepository bugTrackingSystemRepository) {
		this.bugTrackingSystemRepository = bugTrackingSystemRepository;
	}

	@Autowired
	public void setTicketRepository(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Override
	public List<Issue> defineTestItemsIssues(String projectName, DefineIssueRQ defineIssue, ReportPortalUser user) {
		List<String> errors = new ArrayList<>();
		List<IssueDefinition> definitions = defineIssue.getIssues();
		expect(definitions.isEmpty(), equalTo(false)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION);
		List<Issue> updated = new ArrayList<>(defineIssue.getIssues().size());

		definitions.forEach(issueDefinition -> {
			try {
				TestItem testItem = testItemRepository.findById(issueDefinition.getId())
						.orElseThrow(() -> new BusinessRuleViolationException(
								Suppliers.formattedSupplier("Cannot update issue type for test item '{}', cause it is not found.",
										issueDefinition.getId()
								).get()));

				verifyTestItem(testItem, issueDefinition.getId());

				Issue issue = issueDefinition.getIssue();
				IssueType issueType = issueTypeHandler.defineIssueType(
						testItem.getItemId(), ProjectUtils.extractProjectDetails(user, projectName).getProjectId(), issue.getIssueType());

				IssueEntity issueEntity = new IssueEntityBuilder(testItem.getItemStructure().getItemResults().getIssue()).addIssueType(
						issueType)
						.addDescription(issue.getComment())
						.addIgnoreFlag(issue.getIgnoreAnalyzer())
						.addAutoAnalyzedFlag(false)
						.get();
				testItem.getItemStructure().getItemResults().setIssue(issueEntity);
				testItem.getItemStructure().getItemResults().getIssueStatistics().forEach(it -> it.setIssueType(issueType));

				//TODO EXTERNAL SYSTEM LOGIC, ANALYZER LOGIC
				testItemRepository.save(testItem);
				updated.add(IssueConverter.TO_MODEL.apply(issueEntity));
			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		});
		expect(!errors.isEmpty(), equalTo(false)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
		return updated;
	}

	@Override
	public OperationCompletionRS updateTestItem(String projectName, Long itemId, UpdateTestItemRQ rq, ReportPortalUser user) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		validate(projectName, user, testItem);
		testItem = new TestItemBuilder(testItem).addTags(rq.getTags()).addDescription(rq.getDescription()).get();
		testItemRepository.save(testItem);
		return new OperationCompletionRS("TestItem with ID = '" + testItem.getItemId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> linkExternalIssues(String projectName, LinkExternalIssueRQ rq, ReportPortalUser user) {
		List<String> errors = new ArrayList<>();

		Iterable<TestItem> testItems = testItemRepository.findAllById(rq.getTestItemIds());
		List<Ticket> existedTickets = collectExistedTickets(rq);
		Set<Ticket> ticketsFromRq = collectTickets(rq, user.getUserId());

		testItems.forEach(testItem -> {
			try {
				verifyTestItem(testItem, testItem.getItemId());
				IssueEntity issue = testItem.getItemStructure().getItemResults().getIssue();
				existedTickets.forEach(it -> it.getIssues().add(issue));
				ticketsFromRq.forEach(it -> it.getIssues().add(issue));
			} catch (Exception e) {
				errors.add(e.getMessage());
			}
		});
		expect(!errors.isEmpty(), equalTo(FALSE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
		ticketRepository.saveAll(existedTickets);
		ticketRepository.saveAll(ticketsFromRq);
		//eventPublisher.publishEvent(new TicketAttachedEvent(before, Lists.newArrayList(testItems), userName, projectName));
		return stream(testItems.spliterator(), false).map(
				testItem -> new OperationCompletionRS("TestItem with ID = '" + testItem.getItemId() + "' successfully updated."))
				.collect(toList());
	}

	@Override
	public List<OperationCompletionRS> unlinkExternalIssues(String projectName, UnlinkExternalIssueRq rq, ReportPortalUser user) {
		List<String> errors = new ArrayList<>();
		List<TestItem> testItems = testItemRepository.findAllById(rq.getTestItemIds());
		testItems.forEach(testItem -> {
			try {
				verifyTestItem(testItem, testItem.getItemId());
				testItem.getItemStructure()
						.getItemResults()
						.getIssue()
						.getTickets()
						.removeIf(it -> rq.getIssueIds().contains(it.getTicketId()));
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
		List<Ticket> existedTickets = ticketRepository.findByTicketIdIn(
				rq.getIssues().stream().map(Issue.ExternalSystemIssue::getTicketId).collect(toList()));
		List<String> existedTicketsIds = existedTickets.stream().map(Ticket::getTicketId).collect(toList());
		rq.getIssues().removeIf(it -> existedTicketsIds.contains(it.getTicketId()));
		return existedTickets;
	}

	private Set<Ticket> collectTickets(LinkExternalIssueRQ rq, Long userId) {
		return rq.getIssues().stream().map(it -> {
			Ticket apply = ExternalSystemIssueConverter.TO_TICKET.apply(it);
			apply.setSubmitterId(ofNullable(it.getSubmitter()).orElse(userId));
			apply.setSubmitDate(LocalDateTime.now());
			Optional<BugTrackingSystem> bts = bugTrackingSystemRepository.findById(it.getExternalSystemId());
			expect(bts, isPresent()).verify(EXTERNAL_SYSTEM_NOT_FOUND, it.getExternalSystemId());
			apply.setBugTrackingSystemId(it.getExternalSystemId());
			return apply;
		}).collect(toSet());
	}

	/**
	 * Validates test item access ability.
	 *
	 * @param projectName Project
	 * @param user        User
	 * @param testItem    Test Item
	 */
	private void validate(String projectName, ReportPortalUser user, TestItem testItem) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		Launch launch = testItem.getItemStructure().getLaunch();
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(
					ErrorType.ACCESS_DENIED, "Launch is not under the specified project.");
			if (projectDetails.getProjectRole().lowerThan(ProjectRole.PROJECT_MANAGER)) {
				expect(user.getUserId(), equalTo(launch.getUserId())).verify(ErrorType.ACCESS_DENIED, "You are not a launch owner.");
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
		// TODO possible npe
		expect(item.getItemStructure().getItemResults().getStatus(), not(equalTo(StatusEnum.PASSED)),
				Suppliers.formattedSupplier("Issue status update cannot be applied on {} test items, cause it is not allowed.",
						StatusEnum.PASSED.name()
				)
		).verify();
		expect(
				testItemRepository.hasChildren(item.getItemId()), equalTo(false), Suppliers.formattedSupplier(
						"It is not allowed to udpate issue type for items with descendants. Test item '{}' has descendants.", id)).verify();

		expect(item.getItemStructure().getItemResults().getIssue(), notNull(), Suppliers.formattedSupplier(
						"Cannot update issue type for test item '{}', cause there is no info about actual issue type value.", id)).verify();

		expect(item.getItemStructure().getItemResults().getIssue().getIssueType(), notNull(), Suppliers.formattedSupplier(
						"Cannot update issue type for test item {}, cause it's actual issue type value is not provided.", id)).verify();
	}

}
