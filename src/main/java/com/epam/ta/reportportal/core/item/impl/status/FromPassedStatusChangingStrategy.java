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
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.SKIPPED;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class FromPassedStatusChangingStrategy extends StatusChangingStrategy {

	private final LogIndexer logIndexer;

	private final LogRepository logRepository;

	private final ProjectRepository projectRepository;

	@Autowired
	public FromPassedStatusChangingStrategy(TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
			IssueTypeHandler issueTypeHandler, IssueEntityRepository issueEntityRepository, LaunchRepository launchRepository,
			MessageBus messageBus, LogIndexer logIndexer, LogRepository logRepository, ProjectRepository projectRepository) {
		super(testItemRepository, itemAttributeRepository, issueTypeHandler, issueEntityRepository, launchRepository, messageBus);
		this.logIndexer = logIndexer;
		this.logRepository = logRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public void changeStatus(TestItem item, StatusEnum providedStatus, ReportPortalUser user, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, FAILED)).verify(INCORRECT_REQUEST,
				formattedSupplier("Actual status: '{}' can be switched only to: '{}' or '{}'.",
						item.getItemResults().getStatus(),
						SKIPPED,
						FAILED
				)
		);

		Project project = projectRepository.findById(projectId).orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));
		Launch launch = launchRepository.findById(item.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, item.getLaunchId()));

		StatusEnum oldParentStatus = item.getParent().getItemResults().getStatus();
		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(item, projectId);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findByLaunchIdAndKeyAndSystem(item.getLaunchId(),
				SKIPPED_ISSUE_KEY,
				true
		);

		item.getItemResults().setStatus(providedStatus);
		if (FAILED.equals(providedStatus) || (SKIPPED.equals(providedStatus) && skippedIssueAttribute.isPresent()
				&& skippedIssueAttribute.get().getValue().equals("true"))) {
			addToInvestigateIssue(item, projectId);
		}

		messageBus.publishActivity(new TestItemStatusChangedEvent(before,
				TO_ACTIVITY_RESOURCE.apply(item, projectId),
				user.getUserId(),
				user.getUsername()
		));

		ArrayList<Long> itemsToReindex = Lists.newArrayList(item.getItemId());
		itemsToReindex.add(item.getItemId());

		changeParentsStatusesToFailed(item, launch, oldParentStatus, user, itemsToReindex);
		logIndexer.cleanIndex(projectId,
				logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(item.getLaunchId(),
						itemsToReindex,
						LogLevel.ERROR.toInt()
				)
		);
		logIndexer.indexItemsLogs(projectId, launch.getId(), itemsToReindex, AnalyzerUtils.getAnalyzerConfig(project));
	}
}
