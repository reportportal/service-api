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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class StatusChangeTestItemHandlerImplTest {

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private ItemAttributeRepository itemAttributeRepository;

	@Mock
	private IssueTypeHandler issueTypeHandler;

	@Mock
	private IssueEntityRepository issueEntityRepository;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private MessageBus messageBus;

	@InjectMocks
	private StatusChangeTestItemHandlerImpl handler;

	@Test
	void changeNotStepItemStatus() {
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		TestItem item = new TestItem();
		item.setHasChildren(true);
		item.setType(TestItemTypeEnum.TEST);

		ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.changeStatus(item, StatusEnum.FAILED, user, extractProjectDetails(user, "test_project"))
		);
		assertEquals("Incorrect Request. Unable to change status on test item with children", exception.getMessage());
	}

	@Test
	void changeStatusNegative() {
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		TestItem item = new TestItem();
		item.setHasChildren(false);
		item.setType(TestItemTypeEnum.STEP);
		TestItemResults itemResults = new TestItemResults();
		itemResults.setStatus(StatusEnum.IN_PROGRESS);
		item.setItemResults(itemResults);

		ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.changeStatus(item, StatusEnum.FAILED, user, extractProjectDetails(user, "test_project"))
		);
		assertEquals("Incorrect Request. Actual status: IN_PROGRESS can not be changed to: FAILED", exception.getMessage());
	}
}