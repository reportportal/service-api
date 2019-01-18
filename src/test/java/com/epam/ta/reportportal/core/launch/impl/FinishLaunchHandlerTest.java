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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.launch.AfterLaunchFinishedHandler;
import com.epam.ta.reportportal.core.launch.util.LaunchLinkGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.FinishLaunchRS;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class FinishLaunchHandlerTest {

	private LaunchRepository launchRepository = mock(LaunchRepository.class);
	private TestItemRepository testItemRepository = mock(TestItemRepository.class);
	private AfterLaunchFinishedHandler afterLaunchFinishedHandler = mock(AfterLaunchFinishedHandler.class);
	private ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
	private MessageBus messageBus = mock(MessageBus.class);

	private FinishLaunchHandler handler = new FinishLaunchHandler(launchRepository,
			testItemRepository,
			messageBus,
			applicationEventPublisher,
			afterLaunchFinishedHandler
	);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void finishLaunch() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		final Launch launch = handler.finishLaunch(1L, finishExecutionRQ, extractProjectDetails(rpUser, "test_project"), rpUser);

		assertNotNull(launch);
		assertNotEquals(StatusEnum.IN_PROGRESS, launch.getStatus());
	}

	@Test
	public void finishLaunchWithLink() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		final FinishLaunchRS finishLaunchRS = handler.finishLaunch(1L,
				finishExecutionRQ,
				extractProjectDetails(rpUser, "test_project"),
				rpUser,
				LaunchLinkGenerator.LinkParams.of("http", "example.com", "test_project")
		);

		assertNotNull(finishLaunchRS);
		assertEquals("http://example.com/ui/#test_project/launches/all/1", finishLaunchRS.getLink());
	}

	@Test
	public void stopLaunch() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		final OperationCompletionRS response = handler.stopLaunch(1L, finishExecutionRQ, extractProjectDetails(rpUser, "test_project"),
				rpUser
		);
		assertNotNull(response);
		assertEquals("Launch with ID = '1' successfully stopped.", response.getResultMessage());
	}

	@Test
	public void bulkStopLaunch() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		Map<Long, FinishExecutionRQ> entities = new HashMap<>();
		entities.put(1L, finishExecutionRQ);

		BulkRQ<FinishExecutionRQ> bulkRq = new BulkRQ<>();
		bulkRq.setEntities(entities);

		ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		final List<OperationCompletionRS> response = handler.stopLaunch(bulkRq, extractProjectDetails(rpUser, "test_project"), rpUser);
		assertNotNull(response);
		assertEquals(1, response.size());
	}

	@Test
	public void finishWithIncorrectStatus() {
		thrown.expect(ReportPortalException.class);

		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

		handler.finishLaunch(1L, finishExecutionRQ, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void finishWithIncorrectEndTime() {
		thrown.expect(ReportPortalException.class);

		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().minusHours(5).atZone(ZoneId.of("UTC")).toInstant()));

		final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		handler.finishLaunch(1L, finishExecutionRQ, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void finishNotOwnLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions.");

		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		final ReportPortalUser rpUser = getRpUser("now owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		handler.finishLaunch(1L, finishExecutionRQ, extractProjectDetails(rpUser, "test_project"), rpUser);
	}
}