/*
 * Copyright 2020 EPAM Systems
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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public abstract class AbstractStatusChangingStrategy implements StatusChangingStrategy {

	private final TestItemService testItemService;

	private final ProjectRepository projectRepository;
	private final LaunchRepository launchRepository;
	private final IssueTypeHandler issueTypeHandler;
	private final MessageBus messageBus;

	protected final IssueEntityRepository issueEntityRepository;
	protected final LogRepository logRepository;
	protected final LogIndexer logIndexer;

	protected AbstractStatusChangingStrategy(TestItemService testItemService, ProjectRepository projectRepository,
			LaunchRepository launchRepository, IssueTypeHandler issueTypeHandler, MessageBus messageBus,
			IssueEntityRepository issueEntityRepository, LogRepository logRepository, LogIndexer logIndexer) {
		this.testItemService = testItemService;
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.messageBus = messageBus;
		this.issueEntityRepository = issueEntityRepository;
		this.logRepository = logRepository;
		this.logIndexer = logIndexer;
	}

	protected abstract void updateStatus(Project project, Launch launch, TestItem testItem, StatusEnum providedStatus,
			ReportPortalUser user);

	protected abstract StatusEnum evaluateParentItemStatus(TestItem parentItem, TestItem childItem);

	@Override
	public void changeStatus(TestItem testItem, StatusEnum providedStatus, ReportPortalUser user) {
		BusinessRule.expect(testItem.getItemResults().getStatus(), currentStatus -> !IN_PROGRESS.equals(currentStatus))
				.verify(INCORRECT_REQUEST,
						Suppliers.formattedSupplier("Unable to update status of test item = '{}' because of '{}' status",
								testItem.getItemId(),
								testItem.getItemResults().getStatus()
						).get()
				);
		if (providedStatus == testItem.getItemResults().getStatus()) {
			return;
		}

		Launch launch = testItemService.getEffectiveLaunch(testItem);
		Project project = projectRepository.findById(launch.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, launch.getProjectId()));

		updateStatus(project, launch, testItem, providedStatus, user);
	}

	protected void addToInvestigateIssue(TestItem testItem, Long projectId) {
		IssueEntity issueEntity = new IssueEntity();
		IssueType toInvestigate = issueTypeHandler.defineIssueType(projectId, TO_INVESTIGATE.getLocator());
		issueEntity.setIssueType(toInvestigate);
		issueEntity.setTestItemResults(testItem.getItemResults());
		issueEntityRepository.save(issueEntity);
		testItem.getItemResults().setIssue(issueEntity);
	}

	protected List<Long> changeParentsStatuses(TestItem testItem, Launch launch, boolean issueRequired, ReportPortalUser user) {
		List<Long> updatedParents = Lists.newArrayList();

		TestItem parent = testItem.getParent();
		while (parent != null) {

			StatusEnum currentParentStatus = parent.getItemResults().getStatus();
			if (!StatusEnum.IN_PROGRESS.equals(currentParentStatus)) {

				StatusEnum newParentStatus = evaluateParentItemStatus(parent, testItem);
				if (!currentParentStatus.equals(newParentStatus)) {
					TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, launch.getProjectId());
					parent.getItemResults().setStatus(newParentStatus);
					updateItem(parent, launch.getProjectId(), issueRequired).ifPresent(updatedParents::add);
					publishUpdateActivity(before, TO_ACTIVITY_RESOURCE.apply(parent, launch.getProjectId()), user);
				} else {
					return updatedParents;
				}

			} else {
				return updatedParents;
			}

			parent = parent.getParent();
		}

		if (launch.getStatus() != IN_PROGRESS) {
			launch.setStatus(launchRepository.hasRootItemsWithStatusNotEqual(launch.getId(),
					StatusEnum.PASSED.name(),
					INFO.name(),
					WARN.name()
			) ? FAILED : PASSED);
		}

		return updatedParents;
	}

	private Optional<Long> updateItem(TestItem parent, Long projectId, boolean issueRequired) {
		if (parent.isHasStats() && !parent.isHasChildren()) {
			updateIssue(parent, projectId, issueRequired);
			return Optional.of(parent.getItemId());
		}
		return Optional.empty();
	}

	private void updateIssue(TestItem parent, Long projectId, boolean issueRequired) {
		if (issueRequired) {
			if (ofNullable(parent.getItemResults().getIssue()).isEmpty()) {
				addToInvestigateIssue(parent, projectId);
			}
		} else {
			ofNullable(parent.getItemResults().getIssue()).ifPresent(issue -> {
				issue.setTestItemResults(null);
				issueEntityRepository.delete(issue);
				parent.getItemResults().setIssue(null);
				logIndexer.cleanIndex(projectId,
						logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(parent.getLaunchId(),
								Collections.singletonList(parent.getItemId()),
								LogLevel.ERROR.toInt()
						)
				);
			});
		}
	}

	private void publishUpdateActivity(TestItemActivityResource before, TestItemActivityResource after, ReportPortalUser user) {
		messageBus.publishActivity(new TestItemStatusChangedEvent(before, after, user.getUserId(), user.getUsername()));
	}

}
