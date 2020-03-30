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
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class ToPassedStatusChangingStrategy extends AbstractStatusChangingStrategy {

	private final TestItemRepository testItemRepository;

	@Autowired
	protected ToPassedStatusChangingStrategy(ProjectRepository projectRepository, LaunchRepository launchRepository,
			IssueTypeHandler issueTypeHandler, MessageBus messageBus, IssueEntityRepository issueEntityRepository,
			LogRepository logRepository, LogIndexer logIndexer, TestItemRepository testItemRepository) {
		super(projectRepository, launchRepository, issueTypeHandler, messageBus, issueEntityRepository, logRepository, logIndexer);
		this.testItemRepository = testItemRepository;
	}

	@Override
	protected void updateStatus(Project project, Launch launch, TestItem testItem, StatusEnum providedStatus, ReportPortalUser user) {
		BusinessRule.expect(providedStatus, statusIn(StatusEnum.PASSED))
				.verify(INCORRECT_REQUEST,
						Suppliers.formattedSupplier("Incorrect status - '{}', only '{}' is allowed", providedStatus, StatusEnum.PASSED)
								.get()
				);

		testItem.getItemResults().setStatus(providedStatus);
		ofNullable(testItem.getItemResults().getIssue()).ifPresent(issue -> {
			issue.setTestItemResults(null);
			issueEntityRepository.delete(issue);
			testItem.getItemResults().setIssue(null);
			logIndexer.cleanIndex(project.getId(),
					logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(testItem.getLaunchId(),
							Collections.singletonList(testItem.getItemId()),
							LogLevel.ERROR.toInt()
					)
			);
		});

		changeParentsStatuses(testItem, launch, false, user);
	}

	@Override
	protected StatusEnum evaluateParentItemStatus(TestItem parentItem, TestItem childItem) {
		return testItemRepository.hasStatusNotEqualsWithoutStepItem(parentItem.getItemId(), childItem.getItemId(), StatusEnum.PASSED) ?
				StatusEnum.FAILED :
				StatusEnum.PASSED;
	}

}
