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
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.attachment.DeleteTestItemAttachmentsEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteTestItemHandlerImplTest {

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private LogRepository logRepository;

	@Mock
	private LogIndexer logIndexer;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private DeleteTestItemHandlerImpl handler;

	@Test
	void testItemNotFound() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Test Item '1' not found. Did you use correct Test Item ID?", exception.getMessage());
	}

	@Test
	void deleteInProgressItem() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		Launch launch = new Launch();
		launch.setStatus(StatusEnum.PASSED);
		launch.setProjectId(1L);
		launch.setUserId(1L);

		when(launchRepository.findById(any(Long.class))).thenReturn(Optional.of(launch));
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.IN_PROGRESS,
				StatusEnum.IN_PROGRESS,
				1L,
				"test"
		)));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Unable to perform operation for non-finished test item. Unable to delete test item ['1'] in progress state",
				exception.getMessage()
		);
	}

	@Test
	void deleteTestItemWithInProgressLaunch() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		Launch launch = new Launch();
		launch.setStatus(StatusEnum.IN_PROGRESS);
		launch.setProjectId(1L);
		launch.setUserId(1L);

		when(launchRepository.findById(any(Long.class))).thenReturn(Optional.of(launch));
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.PASSED, StatusEnum.IN_PROGRESS, 1L, "test")));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals(
				"Unable to perform operation for non-finished launch. Unable to delete test item ['1'] under launch ['null'] with 'In progress' state",
				exception.getMessage()
		);
	}

	@Test
	void deleteTestItemFromAnotherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		Launch launch = new Launch();
		launch.setStatus(StatusEnum.PASSED);
		launch.setProjectId(2L);
		launch.setUserId(1L);

		when(launchRepository.findById(any(Long.class))).thenReturn(Optional.of(launch));
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.PASSED, StatusEnum.FAILED, 2L, "test")));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Forbidden operation. Deleting testItem '1' is not under specified project '1'", exception.getMessage());
	}

	@Test
	void deleteNotOwnTestItem() {
		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		rpUser.setUserId(2L);

		Launch launch = new Launch();
		launch.setStatus(StatusEnum.PASSED);
		launch.setProjectId(1L);
		launch.setUserId(1L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.PASSED, StatusEnum.FAILED, 1L, "owner")));
		when(launchRepository.findById(any(Long.class))).thenReturn(Optional.of(launch));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("You do not have enough permissions. You are not a launch owner.", exception.getMessage());
	}

	@Test
	void deleteTestItemWithParent() {
		ReportPortalUser rpUser = getRpUser("owner", UserRole.ADMINISTRATOR, ProjectRole.MEMBER, 1L);

		TestItem item = getTestItem(StatusEnum.PASSED, StatusEnum.PASSED, 1L, "owner");
		item.setItemId(123123L);
		TestItem parent = new TestItem();
		long parentId = 35L;
		parent.setItemId(parentId);
		String path = "1.2.3";
		parent.setPath(path);
		item.setParent(parent);

		Launch launch = new Launch();
		launch.setStatus(StatusEnum.PASSED);
		launch.setProjectId(1L);
		launch.setUserId(1L);

		item.setLaunchId(launch.getId());
		when(launchRepository.findById(item.getLaunchId())).thenReturn(Optional.of(launch));
		when(logIndexer.cleanIndex(any(), any())).thenReturn(CompletableFuture.completedFuture(0L));
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(item.getLaunchId(),
				Collections.singletonList(item.getItemId()),
				LogLevel.ERROR.toInt()
		)).thenReturn(Collections.emptyList());
		when(testItemRepository.hasChildren(parentId, path)).thenReturn(false);
		when(launchRepository.hasRetries(any())).thenReturn(false);
		doNothing().when(eventPublisher).publishEvent(any(DeleteTestItemAttachmentsEvent.class));
		handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);

		assertFalse(parent.isHasChildren());
	}

	@Test
	void deleteItemPositive() {
		ReportPortalUser rpUser = getRpUser("owner", UserRole.ADMINISTRATOR, ProjectRole.MEMBER, 1L);
		TestItem item = getTestItem(StatusEnum.FAILED, StatusEnum.FAILED, 1L, "owner");

		Launch launch = new Launch();
		launch.setStatus(StatusEnum.FAILED);
		launch.setProjectId(1L);
		launch.setUserId(1L);

		when(testItemRepository.findById(item.getItemId())).thenReturn(Optional.of(item));
		when(launchRepository.findById(any(Long.class))).thenReturn(Optional.of(launch));

		OperationCompletionRS response = handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);

		assertEquals("Test Item with ID = '1' has been successfully deleted.", response.getResultMessage());

	}

	private TestItem getTestItem(StatusEnum itemStatus, StatusEnum launchStatus, Long projectId, String owner) {
		TestItem item = new TestItem();
		item.setItemId(1L);
		TestItemResults results = new TestItemResults();
		results.setStatus(itemStatus);
		item.setItemResults(results);
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setStatus(launchStatus);
		launch.setProjectId(projectId);
		User user = new User();
		user.setId(1L);
		user.setLogin(owner);
		launch.setUserId(user.getId());
		item.setLaunchId(launch.getId());
		return item;
	}
}