/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class UpdateTestItemHandlerImplTest {

	@Mock
	private TestItemRepository repository;

	@InjectMocks
	private UpdateTestItemHandlerImpl handler;

	@Test
	void updateNotExistedTestItem() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
		when(repository.findById(1L)).thenReturn(Optional.empty());
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateTestItem(extractProjectDetails(rpUser, "test_project"), 1L, new UpdateTestItemRQ(), rpUser)
		);
		assertEquals("Test Item '1' not found. Did you use correct Test Item ID?", exception.getMessage());
	}

	@Test
	void updateTestItemWithSystemAttributes() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
		when(repository.findById(1L)).thenReturn(Optional.of(new TestItem()));
		final UpdateTestItemRQ updateTestItemRQ = new UpdateTestItemRQ();
		updateTestItemRQ.setAttributes(Sets.newHashSet(new ItemAttributeResource("key", "value", true),
				new ItemAttributeResource("key", "value", false)
		));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateTestItem(extractProjectDetails(rpUser, "test_project"), 1L, updateTestItemRQ, rpUser)
		);
		assertEquals("Forbidden operation. System attributes is not applicable here", exception.getMessage());
	}

	@Test
	void updateTestItemUnderNotExistedLaunch() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		when(repository.findById(1L)).thenReturn(Optional.of(new TestItem()));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateTestItem(extractProjectDetails(rpUser, "test_project"), 1L, new UpdateTestItemRQ(), rpUser)
		);
		assertEquals("Launch '' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void updateTestItemUnderNotOwnLaunch() {
		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		User user = new User();
		user.setLogin("owner");
		launch.setUser(user);
		launch.setProjectId(1L);
		item.setLaunch(launch);
		when(repository.findById(1L)).thenReturn(Optional.of(item));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateTestItem(extractProjectDetails(rpUser, "test_project"), 1L, new UpdateTestItemRQ(), rpUser)
		);
		assertEquals("You do not have enough permissions. You are not a launch owner.", exception.getMessage());
	}

	@Test
	void updateTestItemFromAnotherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		TestItem item = new TestItem();
		Launch launch = new Launch();
		User user = new User();
		user.setLogin("test");
		launch.setUser(user);
		launch.setProjectId(2L);
		item.setLaunch(launch);
		when(repository.findById(1L)).thenReturn(Optional.of(item));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateTestItem(extractProjectDetails(rpUser, "test_project"), 1L, new UpdateTestItemRQ(), rpUser)
		);
		assertEquals("You do not have enough permissions. Launch is not under the specified project.", exception.getMessage());
	}

	@Test
	void updateStatusOnNotFinishedTestItem() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		TestItem item = new TestItem();
		TestItemResults results = new TestItemResults();
		item.setItemResults(results);
		results.setStatus(StatusEnum.IN_PROGRESS);
		Launch launch = new Launch();
		User user = new User();
		user.setLogin("test");
		launch.setUser(user);
		launch.setProjectId(1L);
		item.setLaunch(launch);
		when(repository.findById(1L)).thenReturn(Optional.of(item));
		final UpdateTestItemRQ rq = new UpdateTestItemRQ();
		rq.setStatus(StatusEnum.PASSED.name());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateTestItem(extractProjectDetails(rpUser, "test_project"), 1L, rq, rpUser)
		);
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Unable to change status on not finished test item'",
				exception.getMessage()
		);
	}
}