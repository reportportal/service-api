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

package com.epam.ta.reportportal.core.hierarchy;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.core.item.impl.status.ChangeStatusHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.job.PageUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.item.impl.status.ToSkippedStatusChangingStrategy.SKIPPED_ISSUE_KEY;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.SUITE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractFinishHierarchyHandler<T> implements FinishHierarchyHandler<T> {

	public static final int ITEM_PAGE_SIZE = 50;

	public static final String ATTRIBUTE_KEY_STATUS = "status";
	public static final String ATTRIBUTE_VALUE_INTERRUPTED = "interrupted";

	protected final LaunchRepository launchRepository;
	protected final TestItemRepository testItemRepository;
	protected final ItemAttributeRepository itemAttributeRepository;
	protected final IssueEntityRepository issueEntityRepository;
	private final IssueTypeHandler issueTypeHandler;
	private final ChangeStatusHandler changeStatusHandler;

	public AbstractFinishHierarchyHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ItemAttributeRepository itemAttributeRepository, IssueEntityRepository issueEntityRepository, IssueTypeHandler issueTypeHandler,
			ChangeStatusHandler changeStatusHandler) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.issueEntityRepository = issueEntityRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.changeStatusHandler = changeStatusHandler;
	}

	protected abstract boolean isIssueRequired(StatusEnum status, T entity);

	protected abstract Function<Pageable, List<Long>> getItemIdsFunction(boolean hasChildren, T entity, StatusEnum status);

	protected boolean evaluateSkippedAttributeValue(StatusEnum status, Long launchId) {
		if (SKIPPED.equals(status)) {
			return itemAttributeRepository.findByLaunchIdAndKeyAndSystem(launchId, SKIPPED_ISSUE_KEY, true)
					.map(attribute -> BooleanUtils.toBoolean(attribute.getValue()))
					.orElse(false);
		} else {
			return false;
		}
	}

	protected Optional<IssueType> getIssueType(boolean isIssueRequired, Long projectId, String locator) {
		if (isIssueRequired) {
			return Optional.of(issueTypeHandler.defineIssueType(projectId, locator));
		}
		return Optional.empty();
	}

	@Override
	public int finishDescendants(T parentEntity, StatusEnum status, Date endDate, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {

		expect(status, s -> s != IN_PROGRESS).verify(INCORRECT_REQUEST, "Unable to update current status to - " + IN_PROGRESS);

		LocalDateTime endTime = TO_LOCAL_DATE_TIME.apply(endDate);

		final int withoutChildren = updateDescendantsWithoutChildren(parentEntity, projectDetails.getProjectId(), status, endTime, user);
		final int withChildren = updateDescendantsWithChildren(parentEntity, endTime);
		return withoutChildren + withChildren;
	}

	private int updateDescendantsWithoutChildren(T entity, Long projectId, StatusEnum status, LocalDateTime endTime, ReportPortalUser user) {
		AtomicInteger updatedCount = new AtomicInteger(0);
		getIssueType(isIssueRequired(status, entity),
				projectId,
				TO_INVESTIGATE.getLocator()
		).ifPresentOrElse(issueType -> PageUtil.iterateOverContent(ITEM_PAGE_SIZE,
				getItemIdsFunction(false, entity, IN_PROGRESS),
				itemIdsWithoutChildrenHandler(issueType, status, endTime, projectId, user, updatedCount)
				),
				() -> PageUtil.iterateOverContent(ITEM_PAGE_SIZE,
						getItemIdsFunction(false, entity, IN_PROGRESS),
						itemIdsWithoutChildrenHandler(status, endTime, projectId, user, updatedCount)
				)
		);
		return updatedCount.get();
	}

	private Consumer<List<Long>> itemIdsWithoutChildrenHandler(IssueType issueType, StatusEnum status, LocalDateTime endTime,
			Long projectId, ReportPortalUser user, AtomicInteger updatedCount) {
		return itemIds -> {
			Map<Long, TestItem> itemMapping = getItemMapping(itemIds);
			itemIds.forEach(itemId -> ofNullable(itemMapping.get(itemId)).ifPresent(testItem -> {
				finishItem(testItem, status, endTime);
				attachIssue(testItem, issueType);
				changeStatusHandler.changeParentStatus(testItem, projectId, user);
			}));
			updatedCount.addAndGet(itemIds.size());
		};
	}

	private Consumer<List<Long>> itemIdsWithoutChildrenHandler(StatusEnum status, LocalDateTime endTime, Long projectId,
			ReportPortalUser user, AtomicInteger updatedCount) {
		return itemIds -> {
			Map<Long, TestItem> itemMapping = getItemMapping(itemIds);
			itemIds.forEach(itemId -> ofNullable(itemMapping.get(itemId)).ifPresent(testItem -> {
				finishItem(testItem, status, endTime);
				changeStatusHandler.changeParentStatus(testItem, projectId, user);
			}));
			updatedCount.addAndGet(itemIds.size());
		};
	}

	/**
	 * Attach default issue to the item only if it wasn't already created
	 *
	 * @param testItem  {@link TestItem}
	 * @param issueType {@link IssueType}
	 */
	private void attachIssue(TestItem testItem, IssueType issueType) {
		if (!SUITE.sameLevel(testItem.getType()) && testItem.isHasStats()) {
			issueEntityRepository.findById(testItem.getItemId()).ifPresentOrElse(issue -> {
			}, () -> {
				IssueEntity issueEntity = new IssueEntity();
				issueEntity.setIssueType(issueType);
				issueEntity.setTestItemResults(testItem.getItemResults());
				issueEntityRepository.save(issueEntity);
				testItem.getItemResults().setIssue(issueEntity);
			});
		}
	}

	private int updateDescendantsWithChildren(T entity, LocalDateTime endTime) {
		AtomicInteger updatedCount = new AtomicInteger(0);
		PageUtil.iterateOverContent(ITEM_PAGE_SIZE,
				getItemIdsFunction(true, entity, IN_PROGRESS),
				itemIdsWithChildrenHandler(endTime, updatedCount)
		);
		return updatedCount.get();
	}

	private Consumer<List<Long>> itemIdsWithChildrenHandler(LocalDateTime endTime, AtomicInteger updatedCount) {
		return itemIds -> {
			Map<Long, TestItem> itemMapping = getItemMapping(itemIds);
			itemIds.forEach(itemId -> ofNullable(itemMapping.get(itemId)).ifPresent(testItem -> {
				boolean isFailed = testItemRepository.hasDescendantsNotInStatus(testItem.getItemId(),
						StatusEnum.PASSED.name(),
						StatusEnum.INFO.name(),
						StatusEnum.WARN.name()
				);
				finishItem(testItem, isFailed ? FAILED : PASSED, endTime);
			}));
			updatedCount.addAndGet(itemIds.size());
		};
	}

	private Map<Long, TestItem> getItemMapping(List<Long> itemIds) {
		return testItemRepository.findAllById(itemIds).stream().collect(Collectors.toMap(TestItem::getItemId, i -> i));
	}

	private void finishItem(TestItem testItem, StatusEnum status, LocalDateTime endTime) {
		testItem.getItemResults().setStatus(status);
		testItem.getItemResults().setEndTime(endTime);
		ItemAttribute interruptedAttribute = new ItemAttribute(ATTRIBUTE_KEY_STATUS, ATTRIBUTE_VALUE_INTERRUPTED, false);
		interruptedAttribute.setTestItem(testItem);
		testItem.getAttributes().add(interruptedAttribute);
	}
}
