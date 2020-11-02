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
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("finishLaunchHierarchyHandler")
public class FinishLaunchHierarchyHandler extends AbstractFinishHierarchyHandler<Launch> {

	@Autowired
	public FinishLaunchHierarchyHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ItemAttributeRepository itemAttributeRepository, IssueTypeHandler issueTypeHandler, IssueEntityRepository issueEntityRepository,
			ChangeStatusHandler changeStatusHandler) {
		super(launchRepository, testItemRepository, itemAttributeRepository, issueEntityRepository, issueTypeHandler, changeStatusHandler);
	}

	@Override
	protected boolean isIssueRequired(StatusEnum status, Launch launch) {
		return FAILED.equals(status) || evaluateSkippedAttributeValue(status, launch.getId());
	}

	@Override
	protected Function<Pageable, List<Long>> getItemIdsFunction(boolean hasChildren, Launch launch, StatusEnum status) {
		return hasChildren ?
				pageable -> testItemRepository.findIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(launch.getId(),
						status,
						pageable.getPageSize(),
						pageable.getOffset()
				) :
				pageable -> testItemRepository.findIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(),
						status,
						pageable.getPageSize(),
						pageable.getOffset()
				);
	}

}
