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
import com.epam.ta.reportportal.core.item.impl.status.ChangeStatusHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("finishTestItemHierarchyHandler")
public class FinishTestItemHierarchyHandler extends AbstractFinishHierarchyHandler<TestItem> {

	public FinishTestItemHierarchyHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ItemAttributeRepository itemAttributeRepository, IssueEntityRepository issueEntityRepository, IssueTypeHandler issueTypeHandler,
			ChangeStatusHandler changeStatusHandler) {
		super(launchRepository, testItemRepository, itemAttributeRepository, issueEntityRepository, issueTypeHandler, changeStatusHandler);
	}

	@Override
	protected boolean isIssueRequired(StatusEnum status, TestItem testItem) {
		return FAILED.equals(status) || ofNullable(testItem.getLaunchId()).map(launchId -> evaluateSkippedAttributeValue(status, launchId))
				.orElse(false);
	}

	@Override
	protected Function<Pageable, List<Long>> getItemIdsFunction(boolean hasChildren, TestItem testItem, StatusEnum status) {
		return hasChildren ?
				pageable -> testItemRepository.findIdsByHasChildrenAndParentPathAndStatusOrderedByPathLevel(testItem.getPath(),
						StatusEnum.IN_PROGRESS,
						pageable.getPageSize(),
						pageable.getOffset()
				) :
				pageable -> testItemRepository.findIdsByNotHasChildrenAndParentPathAndStatus(testItem.getPath(),
						status,
						pageable.getPageSize(),
						pageable.getOffset()
				);
	}

}
