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
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.ItemAttributePojo;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntityPojo;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy.SKIPPED_ISSUE_KEY;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.SKIPPED;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.SUITE;
import static com.epam.ta.reportportal.jooq.enums.JStatusEnum.FAILED;
import static com.epam.ta.reportportal.jooq.enums.JStatusEnum.PASSED;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractFinishHierarchyHandler<T> implements FinishHierarchyHandler<T> {

	public static final String ATTRIBUTE_KEY_STATUS = "status";
	public static final String ATTRIBUTE_VALUE_INTERRUPTED = "interrupted";

	public static final int INSERT_ATTRIBUTES_BATCH_SIZE = 75;
	public static final int INSERT_ISSUE_BATCH_SIZE = 75;

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

	protected abstract Stream<Long> retrieveItemIds(T entity, StatusEnum status, boolean hasChildren);

	@Override
	public void finishDescendants(T entity, StatusEnum status, Date endDate, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {

		expect(status, s -> s != IN_PROGRESS).verify(INCORRECT_REQUEST, "Unable to update current status to - " + IN_PROGRESS);

		LocalDateTime endTime = TO_LOCAL_DATE_TIME.apply(endDate);
		boolean isIssueRequired = isIssueRequired(status, entity);

		updateDescendantsWithoutChildren(entity, projectDetails.getProjectId(), status, endTime, isIssueRequired, user);
		updateDescendantsWithChildren(entity, endTime);
	}

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

	private void updateDescendantsWithoutChildren(T entity, Long projectId, StatusEnum status, LocalDateTime endTime,
			boolean isIssueRequired, ReportPortalUser user) {

		Optional<IssueType> issueType = getIssueType(isIssueRequired, projectId, TO_INVESTIGATE.getLocator());
		List<IssueEntityPojo> issueEntities = issueType.isPresent() ?
				Lists.newArrayListWithExpectedSize(INSERT_ISSUE_BATCH_SIZE) :
				Collections.emptyList();
		List<ItemAttributePojo> itemAttributes = Lists.newArrayListWithExpectedSize(INSERT_ATTRIBUTES_BATCH_SIZE);

		retrieveItemIds(entity, StatusEnum.IN_PROGRESS, false).forEach(itemId -> {
			testItemRepository.updateStatusAndEndTimeById(itemId, JStatusEnum.valueOf(status.name()), endTime);
			itemAttributes.add(new ItemAttributePojo(itemId, ATTRIBUTE_KEY_STATUS, ATTRIBUTE_VALUE_INTERRUPTED, false));
			if (itemAttributes.size() >= INSERT_ATTRIBUTES_BATCH_SIZE) {
				itemAttributeRepository.saveMultiple(itemAttributes);
				itemAttributes.clear();
			}
			issueType.ifPresent(it -> {
				TestItem testItem = testItemRepository.findById(itemId)
						.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
				if (!SUITE.sameLevel(testItem.getType()) && testItem.isHasStats()) {
					issueEntities.add(new IssueEntityPojo(itemId, it.getId(), null, false, false));
				}
				if (issueEntities.size() >= INSERT_ISSUE_BATCH_SIZE) {
					issueEntityRepository.saveMultiple(issueEntities);
					issueEntities.clear();
				}
			});

			changeStatusHandler.changeParentStatus(itemId, projectId, user);

		});

		if (!itemAttributes.isEmpty()) {
			itemAttributeRepository.saveMultiple(itemAttributes);
		}
		if (!issueEntities.isEmpty()) {
			issueEntityRepository.saveMultiple(issueEntities);
		}
	}

	private void updateDescendantsWithChildren(T entity, LocalDateTime endTime) {

		List<ItemAttributePojo> itemAttributes = Lists.newArrayListWithExpectedSize(INSERT_ATTRIBUTES_BATCH_SIZE);

		retrieveItemIds(entity, StatusEnum.IN_PROGRESS, true).forEach(itemId -> {
			boolean isFailed = testItemRepository.hasDescendantsWithStatusNotEqual(itemId, PASSED);
			testItemRepository.updateStatusAndEndTimeById(itemId, isFailed ? FAILED : PASSED, endTime);
			itemAttributes.add(new ItemAttributePojo(itemId, ATTRIBUTE_KEY_STATUS, ATTRIBUTE_VALUE_INTERRUPTED, false));
			if (itemAttributes.size() >= INSERT_ATTRIBUTES_BATCH_SIZE) {
				itemAttributeRepository.saveMultiple(itemAttributes);
				itemAttributes.clear();
			}
		});

		if (!itemAttributes.isEmpty()) {
			itemAttributeRepository.saveMultiple(itemAttributes);
		}
	}
}
