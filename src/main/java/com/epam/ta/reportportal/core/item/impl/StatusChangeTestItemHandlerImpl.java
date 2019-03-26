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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.item.StatusChangeTestItemHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class StatusChangeTestItemHandlerImpl implements StatusChangeTestItemHandler {

	public static final String SKIPPED_ISSUE_KEY = "skippedIssue";

	private final TestItemRepository testItemRepository;

	private final ItemAttributeRepository itemAttributeRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final IssueEntityRepository issueEntityRepository;

	private final LaunchRepository launchRepository;

	private final MessageBus messageBus;

	@Autowired
	public StatusChangeTestItemHandlerImpl(TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
			IssueTypeHandler issueTypeHandler, IssueEntityRepository issueEntityRepository, LaunchRepository launchRepository,
			MessageBus messageBus) {
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.issueEntityRepository = issueEntityRepository;
		this.launchRepository = launchRepository;
		this.messageBus = messageBus;
	}

	@Override
	public void changeStatus(TestItem testItem, StatusEnum providedStatus, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		expect(testItem.isHasChildren() && !testItem.getType().sameLevel(TestItemTypeEnum.STEP), Predicate.isEqual(false)).verify(INCORRECT_REQUEST,
				"Unable to change status on test item with children"
		);
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
						"Actual status: " + actualStatus + " can not be changed to: " + providedStatus
				);
		}
	}

	private void changeStatusFromInterrupted(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, PASSED, FAILED)).verify(INCORRECT_REQUEST,
				"Actual status: " + testItem.getItemResults().getStatus() + " can be switched only to: " + SKIPPED + ", " + PASSED + " or "
						+ FAILED
		);

		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);
		testItem.getItemResults().setStatus(providedStatus);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findByLaunchIdAndKeyAndSystem(testItem.getLaunch().getId(),
				SKIPPED_ISSUE_KEY,
				true
		);

		if (FAILED.equals(providedStatus) || (SKIPPED.equals(providedStatus) && skippedIssueAttribute.isPresent()
				&& skippedIssueAttribute.get().getValue().equals("true"))) {
			setToInvestigateIssue(testItem, projectId);
		}

		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem, projectId), userId));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(testItem, userId, projectId);
			testItem.getLaunch()
					.setStatus(launchRepository.hasItemsWithStatusNotEqual(testItem.getLaunch().getId(), JStatusEnum.PASSED) ?
							PASSED :
							FAILED);
		}
	}

	private void changeStatusFromPassed(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, FAILED)).verify(INCORRECT_REQUEST,
				"Actual status: " + testItem.getItemResults().getStatus() + " can be switched only to: " + SKIPPED + " or " + FAILED
		);

		StatusEnum oldParentStatus = testItem.getParent().getItemResults().getStatus();
		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findByLaunchIdAndKeyAndSystem(testItem.getLaunch().getId(),
				SKIPPED_ISSUE_KEY,
				true
		);

		testItem.getItemResults().setStatus(providedStatus);
		if (FAILED.equals(providedStatus) || (SKIPPED.equals(providedStatus) && skippedIssueAttribute.isPresent()
				&& skippedIssueAttribute.get().getValue().equals("true"))) {
			setToInvestigateIssue(testItem, projectId);
		}

		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem, projectId), userId));

		changeParentsStatusesToFailed(testItem, oldParentStatus, userId, projectId);
	}

	private void changeStatusFromFailed(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, PASSED)).verify(INCORRECT_REQUEST,
				"Actual status: " + testItem.getItemResults().getStatus() + " can be switched only to: " + SKIPPED + " or " + PASSED
		);
		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findByLaunchIdAndKeyAndSystem(testItem.getLaunch().getId(),
				SKIPPED_ISSUE_KEY,
				true
		);

		if (SKIPPED.equals(providedStatus)) {
			if (skippedIssueAttribute.isPresent() && skippedIssueAttribute.get().getValue().equals("true")) {
				if (testItem.getItemResults().getIssue() == null) {
					setToInvestigateIssue(testItem, projectId);
				}
			} else {
				issueEntityRepository.delete(testItem.getItemResults().getIssue());
				testItem.getItemResults().setIssue(null);
			}
		}

		if (PASSED.equals(providedStatus)) {
			issueEntityRepository.delete(testItem.getItemResults().getIssue());
			testItem.getItemResults().setIssue(null);
		}

		testItem.getItemResults().setStatus(providedStatus);
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem, projectId), userId));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(testItem, userId, projectId);
			testItem.getLaunch()
					.setStatus(launchRepository.hasItemsWithStatusNotEqual(testItem.getLaunch().getId(), JStatusEnum.PASSED) ?
							PASSED :
							FAILED);
		}
	}

	private void changeStatusFromSkipped(TestItem testItem, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(PASSED, FAILED)).verify(INCORRECT_REQUEST,
				"Actual status: " + testItem.getItemResults().getStatus() + " can be switched only to: " + PASSED + " or " + FAILED
		);
		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);

		if (PASSED.equals(providedStatus) && testItem.getItemResults().getIssue() != null) {
			issueEntityRepository.delete(testItem.getItemResults().getIssue());
			testItem.getItemResults().setIssue(null);
		}
		if (FAILED.equals(providedStatus) && testItem.getItemResults().getIssue() == null) {
			setToInvestigateIssue(testItem, projectId);
		}

		testItem.getItemResults().setStatus(providedStatus);
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(testItem, projectId), userId));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(testItem, userId, projectId);
			testItem.getLaunch()
					.setStatus(launchRepository.hasItemsWithStatusNotEqual(testItem.getLaunch().getId(), JStatusEnum.PASSED) ?
							PASSED :
							FAILED);
		}
	}

	private void setToInvestigateIssue(TestItem testItem, Long projectId) {
		IssueEntity issueEntity = new IssueEntity();
		IssueType toInvestigate = issueTypeHandler.defineIssueType(projectId, TO_INVESTIGATE.getLocator());
		issueEntity.setIssueType(toInvestigate);
		issueEntity.setIssueId(testItem.getItemId());
		issueEntity.setTestItemResults(testItem.getItemResults());
		testItem.getItemResults().setIssue(issueEntity);
	}

	private void changeStatusRecursively(TestItem testItem, Long userId, Long projectId) {
		TestItem parent = testItem.getParent();
		Hibernate.initialize(parent);
		if (parent != null) {
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, projectId);
			StatusEnum newStatus = testItemRepository.hasStatusNotEqualsWithoutStepItem(parent.getItemId(),
					testItem.getItemId(),
					StatusEnum.PASSED.name()
			) ? StatusEnum.FAILED : StatusEnum.PASSED;
			if (!parent.getItemResults().getStatus().equals(newStatus)) {
				parent.getItemResults().setStatus(newStatus);
				messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(parent, projectId), userId));
				changeStatusRecursively(parent, userId, projectId);
			}
		}
	}

	private void changeParentsStatusesToFailed(TestItem testItem, StatusEnum oldParentStatus, Long userId, Long projectId) {
		if (!oldParentStatus.equals(StatusEnum.FAILED)) {
			TestItem parent = testItem.getParent();
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, projectId);
			while (parent != null) {
				parent.getItemResults().setStatus(StatusEnum.FAILED);
				messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(parent, projectId), userId));
				parent = parent.getParent();
			}
			testItem.getLaunch().setStatus(StatusEnum.FAILED);
		}
	}
}
