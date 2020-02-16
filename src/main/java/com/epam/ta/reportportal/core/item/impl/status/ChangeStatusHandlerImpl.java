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

package com.epam.ta.reportportal.core.item.impl.status;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.PASSED;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ChangeStatusHandlerImpl implements ChangeStatusHandler {

	private final TestItemRepository testItemRepository;
	private final IssueEntityRepository issueEntityRepository;
	private final MessageBus messageBus;
	private final LaunchRepository launchRepository;
	private final Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

	@Autowired
	public ChangeStatusHandlerImpl(TestItemRepository testItemRepository, IssueEntityRepository issueEntityRepository,
			MessageBus messageBus, LaunchRepository launchRepository,
			Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping) {
		this.testItemRepository = testItemRepository;
		this.issueEntityRepository = issueEntityRepository;
		this.messageBus = messageBus;
		this.launchRepository = launchRepository;
		this.statusChangingStrategyMapping = statusChangingStrategyMapping;
	}

	@Override
	public void changeParentStatus(Long childId, Long projectId, ReportPortalUser user) {
		testItemRepository.findParentByChildId(childId).ifPresent(parent -> {
			if (parent.isHasChildren()) {
				ofNullable(parent.getItemResults().getIssue()).map(IssueEntity::getIssueId).ifPresent(issueEntityRepository::deleteById);
			}
			if (isParentStatusUpdateRequired(parent)) {
				StatusEnum resolvedStatus = resolveStatus(parent.getItemId());
				if (parent.getItemResults().getStatus() != resolvedStatus) {
					TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, projectId);
					changeStatus(parent, resolvedStatus, user);
					messageBus.publishActivity(new TestItemStatusChangedEvent(before,
							TO_ACTIVITY_RESOURCE.apply(parent, projectId),
							user.getUserId(),
							user.getUsername()
					));
					changeParentStatus(parent.getItemId(), projectId, user);
				}

			}
		});
	}

	private boolean isParentStatusUpdateRequired(TestItem parent) {
		return parent.getItemResults().getStatus() != StatusEnum.IN_PROGRESS
				&& !testItemRepository.hasItemsInStatusByParent(parent.getItemId(), parent.getPath(), StatusEnum.IN_PROGRESS.name());
	}

	private StatusEnum resolveStatus(Long itemId) {
		return testItemRepository.hasDescendantsWithStatusNotEqual(itemId, StatusEnum.PASSED) ? FAILED : PASSED;
	}

	private void changeStatus(TestItem parent, StatusEnum resolvedStatus, ReportPortalUser user) {
		if (parent.isHasChildren() || !parent.isHasStats()) {
			parent.getItemResults().setStatus(resolvedStatus);
		} else {
			Optional<StatusChangingStrategy> statusChangingStrategy = ofNullable(statusChangingStrategyMapping.get(resolvedStatus));
			if (statusChangingStrategy.isPresent()) {
				statusChangingStrategy.get().changeStatus(parent, resolvedStatus, user);
			} else {
				parent.getItemResults().setStatus(resolvedStatus);
			}
		}

	}

	@Override
	public void changeLaunchStatus(Launch launch) {
		if (launch.getStatus() != StatusEnum.IN_PROGRESS) {
			if (!launchRepository.hasItemsInStatuses(launch.getId(), Lists.newArrayList(JStatusEnum.IN_PROGRESS))) {
				StatusEnum launchStatus = launchRepository.hasRootItemsWithStatusNotEqual(launch.getId(), StatusEnum.PASSED) ?
						FAILED :
						PASSED;
				launch.setStatus(launchStatus);
			}
		}
	}
}
