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
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class StartTestItemHandlerImplTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private StartTestItemHandler handler = new StartTestItemHandlerImpl();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void startRootItemUnderNotExistedLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Launch '1' not found. Did you use correct Launch ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(launchRepository.findById(1L)).thenReturn(Optional.empty());
		final StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(1L);

		handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), rq);
	}

	@Test
	public void startRootItemUnderLaunchFromAnotherProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions.");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchId(1L);
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		final Launch launch = getLaunch(2L, StatusEnum.IN_PROGRESS);
		launch.setStartTime(LocalDateTime.now().minusHours(1));
		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));


		handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ);
	}

	@Test
	public void startRootItemUnderFinishedLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Start test item is not allowed. Launch '1' is not in progress");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchId(1L);

		when(launchRepository.findById(1L)).thenReturn(Optional.of(getLaunch(1L, StatusEnum.PASSED)));

		handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ);
	}

	@Test
	public void startRootItemEarlierThanLaunch() {
		thrown.expect(ReportPortalException.class);

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchId(1L);
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		final Launch launch = getLaunch(1L, StatusEnum.IN_PROGRESS);
		launch.setStartTime(LocalDateTime.now().plusHours(1));
		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

		handler.startRootItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ);
	}

	@Test
	public void startChildItemUnderNotExistedParent() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Test Item '1' not found. Did you use correct Test Item ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());

		handler.startChildItem(rpUser, extractProjectDetails(rpUser, "test_project"), new StartTestItemRQ(), 1L);
	}

	@Test
	public void startChildItemEarlierThanParent() {
		thrown.expect(ReportPortalException.class);

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchId(1L);
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		TestItem item = new TestItem();
		item.setStartTime(LocalDateTime.now().plusHours(1));
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(launchRepository.findById(1L)).thenReturn(Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));

		handler.startChildItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ, 1L);
	}

	@Test
	public void startChildItemUnderFinishedParent() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Start test item is not allowed. Parent Item '1' is not in progress");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		startTestItemRQ.setLaunchId(1L);
		startTestItemRQ.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));

		TestItem item = new TestItem();
		item.setItemId(1L);
		TestItemResults results = new TestItemResults();
		results.setStatus(StatusEnum.FAILED);
		item.setItemResults(results);
		item.setStartTime(LocalDateTime.now().minusHours(1));
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(launchRepository.findById(1L)).thenReturn(Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));

		handler.startChildItem(rpUser, extractProjectDetails(rpUser, "test_project"), startTestItemRQ, 1L);
	}

	private Launch getLaunch(Long projectId, StatusEnum status) {
		Launch launch = new Launch();
		launch.setProjectId(projectId);
		launch.setStatus(status);
		return launch;
	}
}