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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.events.item.ItemFinishedEvent;
import com.epam.ta.reportportal.core.hierarchy.FinishHierarchyHandler;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.impl.status.ChangeStatusHandler;
import com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.hierarchy.AbstractFinishHierarchyHandler.ATTRIBUTE_KEY_STATUS;
import static com.epam.ta.reportportal.core.hierarchy.AbstractFinishHierarchyHandler.ATTRIBUTE_VALUE_INTERRUPTED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link FinishTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
@Primary
class FinishTestItemHandlerImpl implements FinishTestItemHandler {

	private final TestItemRepository testItemRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final FinishHierarchyHandler<TestItem> finishHierarchyHandler;

	private final LogIndexer logIndexer;

	private final Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

	private final IssueEntityRepository issueEntityRepository;

	private final LogRepository logRepository;

	private final ChangeStatusHandler changeStatusHandler;

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	FinishTestItemHandlerImpl(TestItemRepository testItemRepository, IssueTypeHandler issueTypeHandler,
			@Qualifier("finishTestItemHierarchyHandler") FinishHierarchyHandler<TestItem> finishHierarchyHandler, LogIndexer logIndexer,
			Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping, IssueEntityRepository issueEntityRepository,
			LogRepository logRepository, ChangeStatusHandler changeStatusHandler, ApplicationEventPublisher eventPublisher) {
		this.testItemRepository = testItemRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.finishHierarchyHandler = finishHierarchyHandler;
		this.logIndexer = logIndexer;
		this.statusChangingStrategyMapping = statusChangingStrategyMapping;
		this.issueEntityRepository = issueEntityRepository;
		this.logRepository = logRepository;
		this.changeStatusHandler = changeStatusHandler;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public OperationCompletionRS finishTestItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, String testItemId,
			FinishTestItemRQ finishExecutionRQ) {
		TestItem testItem = testItemRepository.findByUuid(testItemId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, testItemId));

		TestItemResults testItemResults = processItemResults(user, projectDetails, testItem, finishExecutionRQ, testItem.isHasChildren());

		testItem = new TestItemBuilder(testItem).addDescription(finishExecutionRQ.getDescription())
				.addAttributes(finishExecutionRQ.getAttributes())
				.addTestItemResults(testItemResults)
				.get();

		testItemRepository.save(testItem);
		return new OperationCompletionRS("TestItem with ID = '" + testItemId + "' successfully finished.");
	}

	/**
	 * If test item has descendants, it's status is resolved from statistics
	 * When status provided, no matter test item has or not descendants, test
	 * item status is resolved to provided
	 *
	 * @param testItem         {@link TestItem}
	 * @param finishTestItemRQ {@link FinishTestItemRQ}
	 * @return TestItemResults {@link TestItemResults}
	 */
	private TestItemResults processItemResults(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, TestItem testItem,
			FinishTestItemRQ finishTestItemRQ, boolean hasChildren) {

		Launch launch = retrieveLaunch(testItem);

		verifyTestItem(launch, user, testItem, fromValue(finishTestItemRQ.getStatus()), testItem.isHasChildren());

		TestItemResults testItemResults;
		if (hasChildren) {
			testItemResults = processParentItemResult(testItem, finishTestItemRQ, launch, user, projectDetails);
		} else {
			testItemResults = processChildItemResult(testItem, finishTestItemRQ, user, projectDetails, launch);
		}
		testItemResults.setEndTime(TO_LOCAL_DATE_TIME.apply(finishTestItemRQ.getEndTime()));
		return testItemResults;
	}

	private Launch retrieveLaunch(TestItem testItem) {
		return ofNullable(testItem.getRetryOf()).map(retryParentId -> {
			TestItem retryParent = testItemRepository.findById(retryParentId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItem.getRetryOf()));

			return ofNullable(retryParent.getLaunch()).orElseGet(() -> ofNullable(retryParent.getParent()).map(TestItem::getLaunch)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
		})
				.orElseGet(() -> ofNullable(testItem.getLaunch()).orElseGet(() -> ofNullable(testItem.getParent()).map(TestItem::getLaunch)
						.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND))));
	}

	/**
	 * Validation procedure for specified test item
	 *
	 * @param launch       {@link Launch}
	 * @param user         Report portal user
	 * @param testItem     Test item
	 * @param actualStatus Actual status of item
	 * @param hasChildren  Does item contain children
	 */
	private void verifyTestItem(Launch launch, ReportPortalUser user, TestItem testItem, Optional<StatusEnum> actualStatus,
			boolean hasChildren) {

		expect(user.getUsername(), equalTo(launch.getUser().getLogin())).verify(FINISH_ITEM_NOT_ALLOWED, "You are not a launch owner.");

		expect(!actualStatus.isPresent() && !hasChildren, equalTo(Boolean.FALSE)).verify(AMBIGUOUS_TEST_ITEM_STATUS,
				formattedSupplier(
						"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
						testItem.getItemId()
				)
		);
	}

	private TestItemResults processParentItemResult(TestItem testItem, FinishTestItemRQ finishTestItemRQ, Launch launch,
			ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {

		TestItemResults testItemResults = testItem.getItemResults();
		Optional<StatusEnum> actualStatus = fromValue(finishTestItemRQ.getStatus());

		if (testItemRepository.hasItemsInStatusByParent(testItem.getItemId(), testItem.getPath(), StatusEnum.IN_PROGRESS)) {
			ItemAttribute itemAttribute = new ItemAttribute(ATTRIBUTE_KEY_STATUS, ATTRIBUTE_VALUE_INTERRUPTED, false);
			itemAttribute.setTestItem(testItem);
			testItem.getAttributes().add(itemAttribute);

			finishDescendants(testItem, actualStatus.orElse(INTERRUPTED), finishTestItemRQ.getEndTime(), user, projectDetails);
			testItemResults.setStatus(resolveStatus(testItem.getItemId()));
		} else {
			testItem.getAttributes()
					.removeIf(attribute -> ATTRIBUTE_KEY_STATUS.equalsIgnoreCase(attribute.getKey())
							&& ATTRIBUTE_VALUE_INTERRUPTED.equalsIgnoreCase(attribute.getValue()));
			testItemResults.setStatus(actualStatus.orElseGet(() -> resolveStatus(testItem.getItemId())));
		}

		changeStatusHandler.changeParentStatus(testItem.getItemId(), projectDetails.getProjectId(), user);
		changeStatusHandler.changeLaunchStatus(launch);

		return testItemResults;
	}

	private TestItemResults processChildItemResult(TestItem testItem, FinishTestItemRQ finishTestItemRQ, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails, Launch launch) {
		TestItemResults testItemResults = testItem.getItemResults();
		Optional<StatusEnum> actualStatus = fromValue(finishTestItemRQ.getStatus());
		Optional<IssueEntity> resolvedIssue = resolveIssue(actualStatus.orElse(INTERRUPTED),
				testItem,
				finishTestItemRQ.getIssue(),
				projectDetails.getProjectId()
		);

		if (testItemResults.getStatus() == IN_PROGRESS) {
			testItemResults.setStatus(actualStatus.orElse(INTERRUPTED));
			resolvedIssue.ifPresent(issue -> {
				issue.setTestItemResults(testItemResults);
				issueEntityRepository.save(issue);
				testItemResults.setIssue(issue);
			});
			changeStatusHandler.changeParentStatus(testItem.getItemId(), projectDetails.getProjectId(), user);
			changeStatusHandler.changeLaunchStatus(launch);
		} else {
			updateFinishedItem(testItemResults,
					actualStatus.orElse(INTERRUPTED),
					resolvedIssue,
					testItem,
					user,
					projectDetails.getProjectId()
			);
		}

		testItem.getAttributes()
				.removeIf(attribute -> ATTRIBUTE_KEY_STATUS.equalsIgnoreCase(attribute.getKey())
						&& ATTRIBUTE_VALUE_INTERRUPTED.equalsIgnoreCase(attribute.getValue()));

		return testItemResults;
	}

	private void finishDescendants(TestItem testItem, StatusEnum status, Date endtime, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		if (testItemRepository.hasItemsInStatusByParent(testItem.getItemId(), testItem.getPath(), StatusEnum.IN_PROGRESS)) {
			finishHierarchyHandler.finishDescendants(testItem, status, endtime, user, projectDetails);
		}
	}

	private StatusEnum resolveStatus(Long itemId) {
		return testItemRepository.hasDescendantsWithStatusNotEqual(itemId, JStatusEnum.PASSED) ? FAILED : PASSED;
	}

	private boolean isIssueRequired(TestItem testItem, StatusEnum status) {
		return Preconditions.statusIn(FAILED, SKIPPED).test(status) && !ofNullable(testItem.getRetryOf()).isPresent()
				&& testItem.isHasStats();
	}

	private Optional<IssueEntity> resolveIssue(StatusEnum status, TestItem testItem, @Nullable Issue issue, Long projectId) {

		if (isIssueRequired(testItem, status)) {
			return ofNullable(issue).map(is -> {
				//in provided issue should be locator id or NOT_ISSUE value
				String locator = is.getIssueType();
				if (!NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(locator)) {
					IssueType issueType = issueTypeHandler.defineIssueType(projectId, locator);
					IssueEntity issueEntity = IssueConverter.TO_ISSUE.apply(is);
					issueEntity.setIssueType(issueType);
					return Optional.of(issueEntity);
				}
				return Optional.<IssueEntity>empty();
			}).orElseGet(() -> {
				IssueEntity issueEntity = new IssueEntity();
				IssueType toInvestigate = issueTypeHandler.defineIssueType(projectId, TO_INVESTIGATE.getLocator());
				issueEntity.setIssueType(toInvestigate);
				return Optional.of(issueEntity);
			});
		}
		return Optional.empty();
	}

	private void updateFinishedItem(TestItemResults testItemResults, StatusEnum actualStatus, Optional<IssueEntity> resolvedIssue,
			TestItem testItem, ReportPortalUser user, Long projectId) {

		resolvedIssue.ifPresent(issue -> deleteOldIssueIndex(actualStatus, testItem, testItemResults, projectId));

		if (testItemResults.getStatus() != actualStatus) {

			Optional<StatusChangingStrategy> statusChangingStrategy = ofNullable(statusChangingStrategyMapping.get(testItemResults.getStatus()));
			if (statusChangingStrategy.isPresent()) {
				statusChangingStrategy.get().changeStatus(testItem, actualStatus, user, projectId);
			} else {
				testItemResults.setStatus(actualStatus);
			}

		}

		resolvedIssue.ifPresent(issue -> {
			updateItemIssue(testItemResults, issue);
			if (ITEM_CAN_BE_INDEXED.test(testItem)) {
				eventPublisher.publishEvent(new ItemFinishedEvent(testItem.getItemId(), testItem.getLaunch().getId(), projectId));

			}
		});
	}

	private void deleteOldIssueIndex(StatusEnum actualStatus, TestItem testItem, TestItemResults testItemResults, Long projectId) {
		if (actualStatus == PASSED || ITEM_CAN_BE_INDEXED.test(testItem)) {
			ofNullable(testItemResults.getIssue()).ifPresent(issue -> logIndexer.cleanIndex(projectId,
					logRepository.findIdsByTestItemId(testItem.getItemId())
			));
		}
	}

	private void updateItemIssue(TestItemResults testItemResults, IssueEntity resolvedIssue) {
		ofNullable(testItemResults.getIssue()).map(IssueEntity::getIssueId).ifPresent(issueEntityRepository::deleteById);
		resolvedIssue.setTestItemResults(testItemResults);
		issueEntityRepository.save(resolvedIssue);
		testItemResults.setIssue(resolvedIssue);
	}
}
