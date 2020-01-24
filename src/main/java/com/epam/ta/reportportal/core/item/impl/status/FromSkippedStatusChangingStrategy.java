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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class FromSkippedStatusChangingStrategy extends StatusChangingStrategy {

	private final LogIndexer logIndexer;

	private final LogRepository logRepository;

	@Autowired
	public FromSkippedStatusChangingStrategy(TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
			IssueTypeHandler issueTypeHandler, IssueEntityRepository issueEntityRepository, LaunchRepository launchRepository,
			MessageBus messageBus, LogIndexer logIndexer, LogRepository logRepository) {
		super(testItemRepository, itemAttributeRepository, issueTypeHandler, issueEntityRepository, launchRepository, messageBus);
		this.logIndexer = logIndexer;
		this.logRepository = logRepository;
	}

	@Override
	public void changeStatus(TestItem item, StatusEnum providedStatus, ReportPortalUser user, Long projectId) {
		expect(providedStatus, statusIn(PASSED, FAILED)).verify(INCORRECT_REQUEST,
				"Actual status: " + item.getItemResults().getStatus() + " can be switched only to: " + PASSED + " or " + FAILED
		);
		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(item, projectId);

		if (PASSED.equals(providedStatus) && item.getItemResults().getIssue() != null) {
			issueEntityRepository.delete(item.getItemResults().getIssue());
			item.getItemResults().setIssue(null);
			logIndexer.cleanIndex(projectId, logRepository.findIdsByTestItemId(item.getItemId()));
		}
		if (FAILED.equals(providedStatus) && item.getItemResults().getIssue() == null) {
			addToInvestigateIssue(item, projectId);
		}

		item.getItemResults().setStatus(providedStatus);
		messageBus.publishActivity(new TestItemStatusChangedEvent(before,
				TO_ACTIVITY_RESOURCE.apply(item, projectId),
				user.getUserId(),
				user.getUsername()
		));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(item, user, projectId);
			Launch launch = launchRepository.findById(item.getLaunchId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, item.getLaunchId()));
			if (launch.getStatus() != IN_PROGRESS) {
				launch.setStatus(launchRepository.hasRootItemsWithStatusNotEqual(launch.getId(), StatusEnum.PASSED) ? FAILED : PASSED);
			}
		}
	}
}
