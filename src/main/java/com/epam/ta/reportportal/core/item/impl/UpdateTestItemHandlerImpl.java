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
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.project.ProjectRole;
import com.epam.ta.reportportal.store.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
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
import static com.epam.ta.reportportal.store.commons.Predicates.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION;

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
						testItem.getItemId(), user.getProjectDetails().get(projectName).getProjectId(), issue.getIssueType());

				IssueEntity issueEntity = new IssueEntityBuilder(testItem.getTestItemResults().getIssue()).addIssueType(issueType)
						.addDescription(issue.getComment())
						.addIgnoreFlag(issue.getIgnoreAnalyzer())
						.addAutoAnalyzedFlag(false)
						.get();

				testItem.getTestItemResults().setIssue(issueEntity);

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

	/**
	 * Validates test item access ability.
	 *
	 * @param projectName Project
	 * @param user        User
	 * @param testItem    Test Item
	 */
	private void validate(String projectName, ReportPortalUser user, TestItem testItem) {
		Launch launch = testItem.getLaunch();
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(user.getProjectDetails().get(projectName).getProjectId())).verify(
					ErrorType.ACCESS_DENIED, "Launch is not under the specified project.");
			if (user.getProjectDetails().get(projectName).getProjectRole().lowerThan(ProjectRole.PROJECT_MANAGER)) {
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
