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
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.events.item.ItemFinishedEvent;
import com.epam.ta.reportportal.core.hierarchy.FinishHierarchyHandler;
import com.epam.ta.reportportal.core.item.ExternalTicketHandler;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.impl.retry.RetriesHandler;
import com.epam.ta.reportportal.core.item.impl.status.ChangeStatusHandler;
import com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.hierarchy.AbstractFinishHierarchyHandler.ATTRIBUTE_KEY_STATUS;
import static com.epam.ta.reportportal.core.hierarchy.AbstractFinishHierarchyHandler.ATTRIBUTE_VALUE_INTERRUPTED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link FinishTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
@Primary
@Transactional
class FinishTestItemHandlerImpl implements FinishTestItemHandler {

	private final TestItemRepository testItemRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final FinishHierarchyHandler<TestItem> finishHierarchyHandler;

	private final LogIndexer logIndexer;

	private final Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

	private final IssueEntityRepository issueEntityRepository;

	private final LogRepository logRepository;

	private final LaunchRepository launchRepository;

	private final ChangeStatusHandler changeStatusHandler;

	private final RetriesHandler retriesHandler;

	private final ApplicationEventPublisher eventPublisher;

	private final MessageBus messageBus;

	private final ExternalTicketHandler externalTicketHandler;

	@Autowired
	FinishTestItemHandlerImpl(TestItemRepository testItemRepository, IssueTypeHandler issueTypeHandler,
			@Qualifier("finishTestItemHierarchyHandler") FinishHierarchyHandler<TestItem> finishHierarchyHandler, LogIndexer logIndexer,
			Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping, IssueEntityRepository issueEntityRepository,
			LogRepository logRepository, ChangeStatusHandler changeStatusHandler, ApplicationEventPublisher eventPublisher,
			LaunchRepository launchRepository, RetriesHandler retriesHandler, MessageBus messageBus,
			ExternalTicketHandler externalTicketHandler) {
		this.testItemRepository = testItemRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.finishHierarchyHandler = finishHierarchyHandler;
		this.logIndexer = logIndexer;
		this.statusChangingStrategyMapping = statusChangingStrategyMapping;
		this.issueEntityRepository = issueEntityRepository;
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.changeStatusHandler = changeStatusHandler;
		this.eventPublisher = eventPublisher;
		this.retriesHandler = retriesHandler;
		this.messageBus = messageBus;
		this.externalTicketHandler = externalTicketHandler;
	}

	@Override
	public OperationCompletionRS finishTestItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, String testItemId,
			FinishTestItemRQ finishExecutionRQ) {
		TestItem testItem = testItemRepository.findByUuid(testItemId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, testItemId));

		Launch launch = retrieveLaunch(testItem);

		TestItemResults testItemResults = processItemResults(user,
				projectDetails,
				launch,
				testItem,
				finishExecutionRQ,
				testItem.isHasChildren()
		);

		testItem = new TestItemBuilder(testItem).addDescription(finishExecutionRQ.getDescription())
				.addTestCaseId(finishExecutionRQ.getTestCaseId())
				.addAttributes(finishExecutionRQ.getAttributes())
				.addTestItemResults(testItemResults)
				.get();

		testItemRepository.save(testItem);

		boolean handleRetries = !testItem.isHasChildren() && (BooleanUtils.toBoolean(finishExecutionRQ.isRetry()) || StringUtils.isNotBlank(
				finishExecutionRQ.getRetryOf()));
		if (handleRetries) {
			retriesHandler.handleRetries(launch, testItem, finishExecutionRQ.getRetryOf());
		}

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
	private TestItemResults processItemResults(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, Launch launch,
			TestItem testItem, FinishTestItemRQ finishTestItemRQ, boolean hasChildren) {

		validateRoles(user, projectDetails, launch);
		verifyTestItem(testItem, fromValue(finishTestItemRQ.getStatus()), testItem.isHasChildren());

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
			return getLaunch(retryParent);
		}).orElseGet(() -> getLaunch(testItem)).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND));
	}

	private Optional<Launch> getLaunch(TestItem testItem) {
		return ofNullable(testItem.getLaunchId()).map(launchRepository::findByIdForUpdate)
				.orElseGet(() -> ofNullable(testItem.getParent()).map(TestItem::getLaunchId)
						.map(launchRepository::findByIdForUpdate)
						.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
	}

	/**
	 * Validation procedure for specified test item
	 *
	 * @param testItem     Test item
	 * @param actualStatus Actual status of item
	 * @param hasChildren  Does item contain children
	 */
	private void verifyTestItem(TestItem testItem, Optional<StatusEnum> actualStatus, boolean hasChildren) {
		expect(!actualStatus.isPresent() && !hasChildren, equalTo(Boolean.FALSE)).verify(AMBIGUOUS_TEST_ITEM_STATUS,
				formattedSupplier(
						"There is no status provided from request and there are no descendants to check statistics for test item id '{}'",
						testItem.getItemId()
				)
		);
	}

	private void validateRoles(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, Launch launch) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
			if (!launch.isRerun() && projectDetails.getProjectRole().lowerThan(PROJECT_MANAGER)) {
				expect(user.getUserId(), Predicate.isEqual(launch.getUserId())).verify(FINISH_ITEM_NOT_ALLOWED,
						"You are not a launch owner."
				);
			}
		}
	}

	private TestItemResults processParentItemResult(TestItem testItem, FinishTestItemRQ finishTestItemRQ, Launch launch,
			ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {

		TestItemResults testItemResults = testItem.getItemResults();
		Optional<StatusEnum> actualStatus = fromValue(finishTestItemRQ.getStatus());

		if (testItemRepository.hasItemsInStatusByParent(testItem.getItemId(), testItem.getPath(), StatusEnum.IN_PROGRESS.name())) {
			finishHierarchyHandler.finishDescendants(testItem, actualStatus.orElse(INTERRUPTED), finishTestItemRQ.getEndTime(), user, projectDetails);
			testItemResults.setStatus(resolveStatus(testItem.getItemId()));
		} else {
			testItemResults.setStatus(actualStatus.orElseGet(() -> resolveStatus(testItem.getItemId())));
		}

		testItem.getAttributes()
				.removeIf(attribute -> ATTRIBUTE_KEY_STATUS.equalsIgnoreCase(attribute.getKey())
						&& ATTRIBUTE_VALUE_INTERRUPTED.equalsIgnoreCase(attribute.getValue()));

		changeStatusHandler.changeParentStatus(testItem.getItemId(), projectDetails.getProjectId(), user);
		changeStatusHandler.changeLaunchStatus(launch);

		return testItemResults;
	}

	private TestItemResults processChildItemResult(TestItem testItem, FinishTestItemRQ finishTestItemRQ, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails, Launch launch) {
		TestItemResults testItemResults = testItem.getItemResults();
		Optional<StatusEnum> actualStatus = fromValue(finishTestItemRQ.getStatus());
		Optional<IssueEntity> resolvedIssue = resolveIssue(user,
				actualStatus.orElse(INTERRUPTED),
				testItem,
				finishTestItemRQ.getIssue(),
				projectDetails.getProjectId()
		);

		if (testItemResults.getStatus() == IN_PROGRESS) {
			testItemResults.setStatus(actualStatus.orElse(INTERRUPTED));
			resolvedIssue.ifPresent(issue -> updateItemIssue(testItemResults, issue));
			if (Objects.isNull(testItem.getRetryOf())) {
				changeStatusHandler.changeParentStatus(testItem.getItemId(), projectDetails.getProjectId(), user);
				changeStatusHandler.changeLaunchStatus(launch);
			}
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

	private StatusEnum resolveStatus(Long itemId) {
		return testItemRepository.hasDescendantsNotInStatus(itemId,
				StatusEnum.PASSED.name(),
				StatusEnum.INFO.name(),
				StatusEnum.WARN.name()
		) ? FAILED : PASSED;
	}

	private boolean isIssueRequired(TestItem testItem, StatusEnum status) {
		return Preconditions.statusIn(FAILED, SKIPPED).test(status) && !ofNullable(testItem.getRetryOf()).isPresent()
				&& testItem.isHasStats();
	}

	private Optional<IssueEntity> resolveIssue(ReportPortalUser user, StatusEnum status, TestItem testItem, @Nullable Issue issue,
			Long projectId) {

		if (isIssueRequired(testItem, status)) {
			return ofNullable(issue).map(is -> {
				//in provided issue should be locator id or NOT_ISSUE value
				String locator = is.getIssueType();
				if (!NOT_ISSUE_FLAG.getValue().equalsIgnoreCase(locator)) {
					IssueType issueType = issueTypeHandler.defineIssueType(projectId, locator);
					IssueEntity issueEntity = IssueConverter.TO_ISSUE.apply(is);
					issueEntity.setIssueType(issueType);
					if (!CollectionUtils.isEmpty(issue.getExternalSystemIssues())) {
						externalTicketHandler.linkExternalTickets(user.getUsername(),
								Lists.newArrayList(issueEntity),
								new ArrayList<>(issue.getExternalSystemIssues())
						);
					}
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
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);
			Optional<StatusChangingStrategy> statusChangingStrategy = ofNullable(statusChangingStrategyMapping.get(actualStatus));
			if (statusChangingStrategy.isPresent()) {
				statusChangingStrategy.get().changeStatus(testItem, actualStatus, user);
			} else {
				testItemResults.setStatus(actualStatus);
			}
			publishUpdateActivity(before, TO_ACTIVITY_RESOURCE.apply(testItem, projectId), user);
		}

		resolvedIssue.ifPresent(issue -> {
			updateItemIssue(testItemResults, issue);
			if (ITEM_CAN_BE_INDEXED.test(testItem)) {
				eventPublisher.publishEvent(new ItemFinishedEvent(testItem.getItemId(), testItem.getLaunchId(), projectId));
			}
		});
	}

	private void publishUpdateActivity(TestItemActivityResource before, TestItemActivityResource after, ReportPortalUser user) {
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, after, user.getUserId(), user.getUsername()));
	}

	private void deleteOldIssueIndex(StatusEnum actualStatus, TestItem testItem, TestItemResults testItemResults, Long projectId) {
		if (actualStatus == PASSED || ITEM_CAN_BE_INDEXED.test(testItem)) {
			ofNullable(testItemResults.getIssue()).ifPresent(issue -> logIndexer.cleanIndex(projectId,
					logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(testItem.getLaunchId(),
							Collections.singletonList(testItem.getItemId()),
							LogLevel.ERROR.toInt()
					)
			));
		}
	}

	private void updateItemIssue(TestItemResults testItemResults, IssueEntity resolvedIssue) {
		issueEntityRepository.findById(testItemResults.getItemId()).ifPresent(issueEntity -> {
			issueEntity.setTestItemResults(null);
			issueEntityRepository.delete(issueEntity);
			testItemResults.setIssue(null);
		});
		resolvedIssue.setTestItemResults(testItemResults);
		issueEntityRepository.save(resolvedIssue);
		testItemResults.setIssue(resolvedIssue);
	}
}
