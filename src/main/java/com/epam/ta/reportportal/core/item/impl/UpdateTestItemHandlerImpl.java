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
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ItemIssueTypeDefinedEvent;
import com.epam.ta.reportportal.core.events.activity.LinkTicketEvent;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.item.ExternalTicketHandler;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.enums.LogLevel;
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
import com.epam.ta.reportportal.util.ItemInfoUtils;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter;
import com.epam.ta.reportportal.ws.model.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.ExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.util.ItemInfoUtils.extractAttribute;
import static com.epam.ta.reportportal.util.ItemInfoUtils.extractAttributeResource;
import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link UpdateTestItemHandler}
 *
 * @author Pavel Bortnik
 */
@Service
public class UpdateTestItemHandlerImpl implements UpdateTestItemHandler {

	public static final String INITIAL_STATUS_ATTRIBUTE_KEY = "initialStatus";
	private static final String MANUALLY_CHANGED_STATUS_ATTRIBUTE_KEY = "manually";

	private final TestItemService testItemService;

	private final ProjectRepository projectRepository;

	private final TestItemRepository testItemRepository;

	private final LogRepository logRepository;

	private final ExternalTicketHandler externalTicketHandler;

	private final IssueTypeHandler issueTypeHandler;

	private final MessageBus messageBus;

	private final LogIndexer logIndexer;

	private final IssueEntityRepository issueEntityRepository;

	private final Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

	@Autowired
	public UpdateTestItemHandlerImpl(TestItemService testItemService, ProjectRepository projectRepository, LaunchRepository launchRepository,
			TestItemRepository testItemRepository, LogRepository logRepository, ExternalTicketHandler externalTicketHandler,
			IssueTypeHandler issueTypeHandler, MessageBus messageBus, LogIndexer logIndexer, IssueEntityRepository issueEntityRepository,
			Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping) {
		this.testItemService = testItemService;
		this.projectRepository = projectRepository;
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
		this.externalTicketHandler = externalTicketHandler;
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
						.orElseThrow(() -> new BusinessRuleViolationException(formattedSupplier(
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

				externalTicketHandler.updateLinking(user.getUsername(), issueEntity, issueDefinition.getIssue().getExternalSystemIssues());

				issueEntity.setTestItemResults(testItem.getItemResults());
				issueEntityRepository.save(issueEntity);
				testItem.getItemResults().setIssue(issueEntity);

				testItemRepository.save(testItem);

				if (ITEM_CAN_BE_INDEXED.test(testItem)) {
					Long launchId = testItem.getLaunchId();
					Long itemId = testItem.getItemId();
					if (logsToReindexMap.containsKey(launchId)) {
						logsToReindexMap.get(launchId).add(itemId);
					} else {
						List<Long> itemIds = Lists.newArrayList();
						itemIds.add(itemId);
						logsToReindexMap.put(launchId, itemIds);
					}
				} else {
					logIdsToCleanIndex.addAll(logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(testItem.getLaunchId(),
							Collections.singletonList(testItem.getItemId()),
							LogLevel.ERROR.toInt()
					));
				}

				updated.add(IssueConverter.TO_MODEL.apply(issueEntity));

				TestItemActivityResource after = TO_ACTIVITY_RESOURCE.apply(testItem, projectDetails.getProjectId());

				events.add(new ItemIssueTypeDefinedEvent(before, after, user.getUserId(), user.getUsername()));
			} catch (BusinessRuleViolationException e) {
				errors.add(e.getMessage());
			}
		});
		expect(errors.isEmpty(), equalTo(TRUE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());
		if (!logsToReindexMap.isEmpty()) {
			logsToReindexMap.forEach((key, value) -> logIndexer.indexItemsLogs(project.getId(), key, value, analyzerConfig));
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
		if (providedStatus.isPresent() && !providedStatus.get().equals(testItem.getItemResults().getStatus())) {
			expect(testItem.isHasChildren() && !testItem.getType().sameLevel(TestItemTypeEnum.STEP), equalTo(FALSE)).verify(
					INCORRECT_REQUEST,
					"Unable to change status on test item with children"
			);
			checkInitialStatusAttribute(testItem, rq);
			StatusChangingStrategy strategy = statusChangingStrategyMapping.get(providedStatus.get());

			expect(strategy, notNull()).verify(INCORRECT_REQUEST,
					formattedSupplier("Actual status: '{}' cannot be changed to '{}'.",
							testItem.getItemResults().getStatus(),
							providedStatus.get()
					)
			);
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectDetails.getProjectId());
			strategy.changeStatus(testItem, providedStatus.get(), user);
			messageBus.publishActivity(new TestItemStatusChangedEvent(before,
					TO_ACTIVITY_RESOURCE.apply(testItem, projectDetails.getProjectId()),
					user.getUserId(),
					user.getUsername()
			));
		}
		testItem = new TestItemBuilder(testItem).overwriteAttributes(rq.getAttributes()).addDescription(rq.getDescription()).get();
		testItemRepository.save(testItem);

		return COMPOSE_UPDATE_RESPONSE.apply(itemId);
	}

	@Override
	public List<OperationCompletionRS> processExternalIssues(ExternalIssueRQ request, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		List<String> errors = new ArrayList<>();

		List<TestItem> testItems = testItemRepository.findAllById(request.getTestItemIds());

		testItems.forEach(testItem -> {
			try {
				verifyTestItem(testItem, testItem.getItemId());
			} catch (Exception e) {
				errors.add(e.getMessage());
			}
		});
		expect(errors.isEmpty(), equalTo(TRUE)).verify(FAILED_TEST_ITEM_ISSUE_TYPE_DEFINITION, errors.toString());

		List<TestItemActivityResource> before = testItems.stream()
				.map(it -> TO_ACTIVITY_RESOURCE.apply(it, projectDetails.getProjectId()))
				.collect(Collectors.toList());

		if (LinkExternalIssueRQ.class.equals(request.getClass())) {
			LinkExternalIssueRQ linkRequest = (LinkExternalIssueRQ) request;
			externalTicketHandler.linkExternalTickets(user.getUsername(),
					testItems.stream().map(it -> it.getItemResults().getIssue()).collect(Collectors.toList()),
					linkRequest.getIssues()
			);
		}

		if (UnlinkExternalIssueRQ.class.equals(request.getClass())) {
			externalTicketHandler.unlinkExternalTickets(testItems, (UnlinkExternalIssueRQ) request);
		}
		testItemRepository.saveAll(testItems);
		List<TestItemActivityResource> after = testItems.stream()
				.map(it -> TO_ACTIVITY_RESOURCE.apply(it, projectDetails.getProjectId()))
				.collect(Collectors.toList());

		before.forEach(it -> messageBus.publishActivity(new LinkTicketEvent(it,
				after.stream().filter(t -> t.getId().equals(it.getId())).findFirst().get(),
				user.getUserId(),
				user.getUsername(),
				ActivityAction.LINK_ISSUE
		)));
		return testItems.stream().map(TestItem::getItemId).map(COMPOSE_UPDATE_RESPONSE).collect(toList());
	}

	private static final Function<Long, OperationCompletionRS> COMPOSE_UPDATE_RESPONSE = it -> {
		String message = formattedSupplier("TestItem with ID = '{}' successfully updated.", it).get();
		return new OperationCompletionRS(message);
	};

	private void checkInitialStatusAttribute(TestItem item, UpdateTestItemRQ request) {
		Runnable addInitialStatusAttribute = () -> {
			ItemAttribute initialStatusAttribute = new ItemAttribute(INITIAL_STATUS_ATTRIBUTE_KEY,
					item.getItemResults().getStatus().getExecutionCounterField(),
					true
			);
			initialStatusAttribute.setTestItem(item);
			item.getAttributes().add(initialStatusAttribute);
		};

		Consumer<ItemAttribute> removeManuallyStatusAttributeIfSameAsInitial = statusAttribute -> extractAttributeResource(request.getAttributes(),
				MANUALLY_CHANGED_STATUS_ATTRIBUTE_KEY
		).filter(it -> it.getValue().equalsIgnoreCase(statusAttribute.getValue())).ifPresent(it -> request.getAttributes().remove(it));

		extractAttribute(item.getAttributes(), INITIAL_STATUS_ATTRIBUTE_KEY).ifPresentOrElse(removeManuallyStatusAttributeIfSameAsInitial,
				addInitialStatusAttribute
		);
	}

	@Override
	public void resetItemsIssue(List<Long> itemIds, Long projectId, ReportPortalUser user) {
		itemIds.forEach(itemId -> {
			TestItem item = testItemRepository.findById(itemId).orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, itemId));
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(item, projectId);

			IssueType issueType = issueTypeHandler.defineIssueType(projectId, TestItemIssueGroup.TO_INVESTIGATE.getLocator());
			IssueEntity issueEntity = new IssueEntityBuilder(issueEntityRepository.findById(itemId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.ISSUE_TYPE_NOT_FOUND, itemId))).addIssueType(issueType)
					.addAutoAnalyzedFlag(false)
					.get();
			issueEntityRepository.save(issueEntity);
			item.getItemResults().setIssue(issueEntity);

			TestItemActivityResource after = TO_ACTIVITY_RESOURCE.apply(item, projectId);
			if (!StringUtils.equalsIgnoreCase(before.getIssueTypeLongName(), after.getIssueTypeLongName())) {
				ItemIssueTypeDefinedEvent event = new ItemIssueTypeDefinedEvent(before, after, user.getUserId(), user.getUsername());
				messageBus.publishActivity(event);
			}
		});
	}

	@Override
	public OperationCompletionRS bulkInfoUpdate(BulkInfoUpdateRQ bulkUpdateRq, ReportPortalUser.ProjectDetails projectDetails) {
		expect(projectRepository.existsById(projectDetails.getProjectId()), equalTo(TRUE)).verify(PROJECT_NOT_FOUND,
				projectDetails.getProjectId()
		);

		List<TestItem> items = testItemRepository.findAllById(bulkUpdateRq.getIds());
		items.forEach(it -> ItemInfoUtils.updateDescription(bulkUpdateRq.getDescription(), it.getDescription())
				.ifPresent(it::setDescription));

		bulkUpdateRq.getAttributes().forEach(it -> {
			switch (it.getAction()) {
				case DELETE: {
					items.forEach(item -> {
						ItemAttribute toDelete = ItemInfoUtils.findAttributeByResource(item.getAttributes(), it.getFrom());
						item.getAttributes().remove(toDelete);
					});
					break;
				}
				case UPDATE: {
					items.forEach(item -> ItemInfoUtils.updateAttribute(item.getAttributes(), it));
					break;
				}
				case CREATE: {
					items.stream().filter(item -> ItemInfoUtils.containsAttribute(item.getAttributes(), it.getTo())).forEach(item -> {
						ItemAttribute itemAttribute = ItemAttributeConverter.FROM_RESOURCE.apply(it.getTo());
						itemAttribute.setTestItem(item);
						item.getAttributes().add(itemAttribute);
					});
					break;
				}
			}
		});

		return new OperationCompletionRS("Attributes successfully updated");
	}

	/**
	 * Validates test item access ability.
	 *
	 * @param projectDetails Project
	 * @param user           User
	 * @param testItem       Test Item
	 */
	private void validate(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, TestItem testItem) {
		Launch launch = testItemService.getEffectiveLaunch(testItem);
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED,
					"Launch is not under the specified project."
			);
			if (projectDetails.getProjectRole().lowerThan(ProjectRole.PROJECT_MANAGER)) {
				expect(user.getUserId(), Predicate.isEqual(launch.getUserId())).verify(ACCESS_DENIED, "You are not a launch owner.");
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
				formattedSupplier("Test item results were not found for test item with id = '{}", item.getItemId())
		).verify();

		expect(item.getItemResults().getStatus(),
				not(status -> Stream.of(StatusEnum.values()).filter(StatusEnum::isPositive).anyMatch(s -> s == status)),
				formattedSupplier("Issue status update cannot be applied on {} test items, cause it is not allowed.",
						item.getItemResults().getStatus()
				)
		).verify();

		expect(item.isHasChildren(),
				equalTo(FALSE),
				formattedSupplier("It is not allowed to update issue type for items with descendants. Test item '{}' has descendants.", id)
		).verify();

		expect(item.getItemResults().getIssue(),
				notNull(),
				formattedSupplier("Cannot update issue type for test item '{}', cause there is no info about actual issue type value.", id)
		).verify();

		expect(item.getItemResults().getIssue().getIssueType(),
				notNull(),
				formattedSupplier("Cannot update issue type for test item {}, cause it's actual issue type value is not provided.", id)
		).verify();
	}
}
