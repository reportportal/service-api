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
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.ItemAttributePojo;
import com.epam.ta.reportportal.entity.item.issue.IssueEntityPojo;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy.SKIPPED_ISSUE_KEY;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.SKIPPED;
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

	public AbstractFinishHierarchyHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ItemAttributeRepository itemAttributeRepository, IssueEntityRepository issueEntityRepository,
			IssueTypeHandler issueTypeHandler) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.issueEntityRepository = issueEntityRepository;
		this.issueTypeHandler = issueTypeHandler;
	}

	protected abstract void updateDescendantsWithoutChildren(Long projectId, T entity, StatusEnum status, LocalDateTime endTime,
			boolean isIssueRequired, List<ItemAttributePojo> itemAttributes, List<IssueEntityPojo> issueEntities);

	protected abstract void updateDescendantsWithChildren(T entity, LocalDateTime endTime, List<ItemAttributePojo> itemAttributes);

	protected abstract boolean isIssueRequired(StatusEnum status, T entity);

	@Override
	public void finishDescendants(T entity, StatusEnum status, Date endDate, ReportPortalUser.ProjectDetails projectDetails) {

		expect(status, s -> s != IN_PROGRESS).verify(INCORRECT_REQUEST, "Unable to update current status to - " + IN_PROGRESS);

		LocalDateTime endTime = TO_LOCAL_DATE_TIME.apply(endDate);
		boolean isIssueRequired = isIssueRequired(status, entity);

		List<IssueEntityPojo> issueEntities = isIssueRequired ?
				Lists.newArrayListWithExpectedSize(INSERT_ISSUE_BATCH_SIZE) :
				Collections.emptyList();
		List<ItemAttributePojo> itemAttributes = Lists.newArrayListWithExpectedSize(INSERT_ATTRIBUTES_BATCH_SIZE);

		updateDescendantsWithoutChildren(projectDetails.getProjectId(),
				entity,
				status,
				endTime,
				isIssueRequired,
				itemAttributes,
				issueEntities
		);
		itemAttributes.clear();
		updateDescendantsWithChildren(entity, endTime, itemAttributes);
	}

	protected void updateDescendantWithoutChildren(Long itemId, StatusEnum status, LocalDateTime endTime, Optional<IssueType> issueType,
			List<ItemAttributePojo> itemAttributes, List<IssueEntityPojo> issueEntities) {
		testItemRepository.updateStatusAndEndTimeById(itemId, JStatusEnum.valueOf(status.name()), endTime);
		issueType.ifPresent(it -> {
			if (!SUITE.sameLevel(testItemRepository.getTypeByItemId(itemId))) {
				issueEntities.add(new IssueEntityPojo(itemId, it.getId(), null, false, false));
			}
		});
		itemAttributes.add(new ItemAttributePojo(itemId, ATTRIBUTE_KEY_STATUS, ATTRIBUTE_VALUE_INTERRUPTED, false));
		if (itemAttributes.size() >= INSERT_ATTRIBUTES_BATCH_SIZE) {
			itemAttributeRepository.saveMultiple(itemAttributes);
			itemAttributes.clear();
		}
		if (issueEntities.size() >= INSERT_ISSUE_BATCH_SIZE) {
			issueEntityRepository.saveMultiple(issueEntities);
			issueEntities.clear();
		}
	}

	protected void updateDescendantWithChildren(Long itemId, LocalDateTime endTime, List<ItemAttributePojo> itemAttributes) {
		boolean isFailed = testItemRepository.hasDescendantsWithStatusNotEqual(itemId, PASSED);
		testItemRepository.updateStatusAndEndTimeById(itemId, isFailed ? FAILED : PASSED, endTime);
		itemAttributes.add(new ItemAttributePojo(itemId, ATTRIBUTE_KEY_STATUS, ATTRIBUTE_VALUE_INTERRUPTED, false));
		if (itemAttributes.size() >= INSERT_ATTRIBUTES_BATCH_SIZE) {
			itemAttributeRepository.saveMultiple(itemAttributes);
			itemAttributes.clear();
		}
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
}
