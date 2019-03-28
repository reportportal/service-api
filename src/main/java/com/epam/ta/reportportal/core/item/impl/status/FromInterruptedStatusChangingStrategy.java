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

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class FromInterruptedStatusChangingStrategy extends StatusChangingStrategy {

	@Autowired
	public FromInterruptedStatusChangingStrategy(TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
			IssueTypeHandler issueTypeHandler, IssueEntityRepository issueEntityRepository, LaunchRepository launchRepository,
			MessageBus messageBus) {
		super(testItemRepository, itemAttributeRepository, issueTypeHandler, issueEntityRepository, launchRepository, messageBus);
	}

	@Override
	public void changeStatus(TestItem item, StatusEnum providedStatus, Long userId, Long projectId) {
		expect(providedStatus, statusIn(SKIPPED, PASSED, FAILED)).verify(INCORRECT_REQUEST,
				"Actual status: " + item.getItemResults().getStatus() + " can be switched only to: " + SKIPPED + ", " + PASSED + " or "
						+ FAILED
		);

		TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(item, projectId);
		item.getItemResults().setStatus(providedStatus);

		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findByLaunchIdAndKeyAndSystem(item.getLaunch().getId(),
				SKIPPED_ISSUE_KEY,
				true
		);

		if (FAILED.equals(providedStatus) || (SKIPPED.equals(providedStatus) && skippedIssueAttribute.isPresent()
				&& skippedIssueAttribute.get().getValue().equals("true"))) {
			addToInvestigateIssue(item, projectId);
		}

		messageBus.publishActivity(new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(item, projectId), userId));

		if (PASSED.equals(providedStatus)) {
			changeStatusRecursively(item, userId, projectId);
			if (item.getLaunch().getStatus() != IN_PROGRESS) {
				item.getLaunch()
						.setStatus(launchRepository.hasItemsWithStatusNotEqual(item.getLaunch().getId(), JStatusEnum.PASSED) ?
								FAILED :
								PASSED);
			}
		}
	}
}
