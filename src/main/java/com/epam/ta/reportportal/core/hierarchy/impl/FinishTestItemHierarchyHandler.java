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

package com.epam.ta.reportportal.core.hierarchy.impl;

import com.epam.ta.reportportal.core.hierarchy.AbstractFinishHierarchyHandler;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.ItemAttributePojo;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntityPojo;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("finishTestItemHierarchyHandler")
public class FinishTestItemHierarchyHandler extends AbstractFinishHierarchyHandler<TestItem> {

	private static final List<StatusEnum> ANCESTOR_PROPOGATED_STATUSES = Arrays.asList(FAILED, STOPPED, INTERRUPTED, CANCELLED);

	public FinishTestItemHierarchyHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
										  IssueEntityRepository issueEntityRepository, IssueTypeHandler issueTypeHandler) {
		super(launchRepository, testItemRepository, itemAttributeRepository, issueEntityRepository, issueTypeHandler);
	}

	@Override
	protected void updateDescendantsWithoutChildren(Long projectId, TestItem testItem, StatusEnum status, LocalDateTime endTime,
			boolean isIssueRequired, List<ItemAttributePojo> itemAttributes, List<IssueEntityPojo> issueEntities) {

		Optional<IssueType> issueType = getIssueType(isIssueRequired, projectId, TO_INVESTIGATE.getLocator());

		testItemRepository.streamIdsByNotHasChildrenAndParentPathAndStatus(testItem.getPath(), StatusEnum.IN_PROGRESS)
				.forEach(itemId -> updateDescendantWithoutChildren(itemId.longValue(),
						status,
						endTime,
						issueType,
						itemAttributes,
						issueEntities
				));

		if (!itemAttributes.isEmpty()) {
			itemAttributeRepository.saveMultiple(itemAttributes);
		}
		if (!issueEntities.isEmpty()) {
			issueEntityRepository.saveMultiple(issueEntities);
		}

	}

	@Override
	protected void updateDescendantsWithChildren(TestItem testItem, LocalDateTime endTime) {
		testItemRepository.streamIdsByHasChildrenAndParentPathAndStatusOrderedByPathLevel(testItem.getPath(), StatusEnum.IN_PROGRESS)
				.forEach(itemId -> updateDescendantWithChildren(itemId.longValue(), endTime));
	}

	@Override
	protected boolean isIssueRequired(StatusEnum status, TestItem testItem) {
		return FAILED.equals(status) || ofNullable(testItem.getLaunch()).map(l -> evaluateSkippedAttributeValue(status, l.getId()))
				.orElse(false);
	}

	@Override
	public void setAncestorsStatus(TestItem entity, StatusEnum status, Date endDate) {
		if (ANCESTOR_PROPOGATED_STATUSES.contains(status)) {
			LocalDateTime localDateTime = TO_LOCAL_DATE_TIME.apply(endDate);

			// update status of items bottom-up till the first in-progress item
			for (Map.Entry<Long, StatusEnum> entry : testItemRepository.selectPathStatusesAscending(entity.getPath()).entrySet()) {
				if (StatusEnum.IN_PROGRESS.equals(entry.getValue())) {
					break;
				}
				testItemRepository.updateStatusAndEndTimeById(entry.getKey(), JStatusEnum.valueOf(status.name()), localDateTime);
			}

			Launch launch = entity.getLaunch();
			if (launch.getStatus() != IN_PROGRESS) {
				launch.setStatus(status);
				launch.setEndTime(localDateTime);
				launchRepository.save(launch);
			}
		}
	}
}
