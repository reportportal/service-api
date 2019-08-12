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
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import org.hibernate.Hibernate;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public abstract class StatusChangingStrategy {

	public static final String SKIPPED_ISSUE_KEY = "skippedIssue";

	protected final TestItemRepository testItemRepository;

	final ItemAttributeRepository itemAttributeRepository;

	protected final IssueTypeHandler issueTypeHandler;

	final IssueEntityRepository issueEntityRepository;

	protected final LaunchRepository launchRepository;

	protected final MessageBus messageBus;

	StatusChangingStrategy(TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
			IssueTypeHandler issueTypeHandler, IssueEntityRepository issueEntityRepository, LaunchRepository launchRepository,
			MessageBus messageBus) {
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.issueEntityRepository = issueEntityRepository;
		this.launchRepository = launchRepository;
		this.messageBus = messageBus;
	}

	abstract public void changeStatus(TestItem item, StatusEnum providedStatus, ReportPortalUser user, Long projectId);

	void addToInvestigateIssue(TestItem testItem, Long projectId) {
		IssueEntity issueEntity = new IssueEntity();
		IssueType toInvestigate = issueTypeHandler.defineIssueType(projectId, TO_INVESTIGATE.getLocator());
		issueEntity.setIssueType(toInvestigate);
		issueEntity.setTestItemResults(testItem.getItemResults());
		issueEntityRepository.save(issueEntity);
		testItem.getItemResults().setIssue(issueEntity);
	}

	void changeStatusRecursively(TestItem testItem, ReportPortalUser user, Long projectId) {
		TestItem parent = testItem.getParent();
		Hibernate.initialize(parent);
		if (parent != null) {
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, projectId);
			StatusEnum newStatus = testItemRepository.hasStatusNotEqualsWithoutStepItem(parent.getItemId(),
					testItem.getItemId(),
					StatusEnum.PASSED
			) ? StatusEnum.FAILED : StatusEnum.PASSED;
			if (!parent.getItemResults().getStatus().equals(newStatus) && parent.getItemResults().getStatus() != StatusEnum.IN_PROGRESS) {
				parent.getItemResults().setStatus(newStatus);
				messageBus.publishActivity(new TestItemStatusChangedEvent(before,
						TO_ACTIVITY_RESOURCE.apply(parent, projectId),
						user.getUserId(),
						user.getUsername()
				));
				changeStatusRecursively(parent, user, projectId);
			}
		}
	}

	void changeParentsStatusesToFailed(TestItem testItem, StatusEnum oldParentStatus, ReportPortalUser user, Long projectId) {
		if (!oldParentStatus.equals(StatusEnum.FAILED) || oldParentStatus != StatusEnum.IN_PROGRESS) {
			TestItem parent = testItem.getParent();
			TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, projectId);
			while (parent != null) {
				parent.getItemResults().setStatus(StatusEnum.FAILED);
				messageBus.publishActivity(new TestItemStatusChangedEvent(before,
						TO_ACTIVITY_RESOURCE.apply(parent, projectId),
						user.getUserId(),
						user.getUsername()
				));
				parent = parent.getParent();
			}
			Launch launch = launchRepository.findById(testItem.getLaunchId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, testItem.getLaunchId()));
			if (launch.getStatus() != IN_PROGRESS) {
				launch.setStatus(FAILED);
			}
		}
	}
}
