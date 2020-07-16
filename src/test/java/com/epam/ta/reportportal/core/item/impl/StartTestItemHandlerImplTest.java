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
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
class StartTestItemHandlerImplTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private StartTestItemHandlerImpl handler;

	@Test
	void startRootItemUnderNotExistedLaunch() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(launchRepository.findByUuid("1")).thenReturn(Optional.empty());
		final StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid("1");

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), rq)
		);
		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void startRootItemUnderLaunchFromAnotherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchUuid("1");
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		final Launch launch = getLaunch(2L, StatusEnum.IN_PROGRESS);
		launch.setStartTime(LocalDateTime.now().minusHours(1));
		when(launchRepository.findByUuid("1")).thenReturn(Optional.of(launch));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ)
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	@Disabled
	void startRootItemUnderFinishedLaunch() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchUuid("1");

		when(launchRepository.findByUuid("1")).thenReturn(Optional.of(getLaunch(1L, StatusEnum.PASSED)));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ)
		);
		assertEquals("Start test item is not allowed. Launch '1' is not in progress", exception.getMessage());
	}

	@Test
	void startRootItemEarlierThanLaunch() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchUuid("1");
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		final Launch launch = getLaunch(1L, StatusEnum.IN_PROGRESS);
		launch.setStartTime(LocalDateTime.now().plusHours(1));
		when(launchRepository.findByUuid("1")).thenReturn(Optional.of(launch));

		assertThrows(ReportPortalException.class,
				() -> handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ)
		);
	}

	@Test
	void startChildItemUnderNotExistedParent() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid("1");

		when(launchRepository.findByUuid("1")).thenReturn(Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));
		when(testItemRepository.findByUuid("1")).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> {
			handler.startChildItem(rpUser, extractProjectDetails(rpUser, "test_project"), rq, "1");
		});
		assertEquals("Test Item '1' not found. Did you use correct Test Item ID?", exception.getMessage());
	}

	@Test
	void startChildItemEarlierThanParent() {

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchUuid("1");
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		TestItem item = new TestItem();
		item.setStartTime(LocalDateTime.now().plusHours(1));
		when(launchRepository.findByUuid("1")).thenReturn(Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));
		when(testItemRepository.findByUuid("1")).thenReturn(Optional.of(item));

		assertThrows(ReportPortalException.class,
				() -> handler.startChildItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ, "1")
		);
	}

	@Test
	void startChildItemUnderFinishedParent() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchUuid("1");
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		TestItem item = new TestItem();
		item.setItemId(1L);
		TestItemResults results = new TestItemResults();
		results.setStatus(StatusEnum.FAILED);
		item.setItemResults(results);
		item.setStartTime(LocalDateTime.now().minusHours(1));
		when(launchRepository.findByUuid("1")).thenReturn(Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));
		when(testItemRepository.findByUuid("1")).thenReturn(Optional.of(item));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.startChildItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ, "1")
		);
		assertEquals("Error in handled Request. Please, check specified parameters: "
				+ "'Unable to add a not nested step item, because parent item with ID = '1' is a nested step'", exception.getMessage());
	}

	@Test
	void startChildItemWithNotExistedLaunch() {
		ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchUuid("1");
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		startTestItemRQ.setLaunchUuid("1");

		when(launchRepository.findByUuid("1")).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.startChildItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ, "1")
		);

		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	private Launch getLaunch(Long projectId, StatusEnum status) {
		Launch launch = new Launch();
		launch.setProjectId(projectId);
		launch.setStatus(status);
		return launch;
	}
}