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
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class FinishTestItemHandlerImplTest {

	@Mock
	private TestItemRepository repository;

	@InjectMocks
	private FinishTestItemHandlerImpl handler;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void finishNotExistedTestItem() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Test Item '1' not found. Did you use correct Test Item ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		when(repository.findById(1L)).thenReturn(Optional.empty());
		handler.finishTestItem(rpUser, extractProjectDetails(rpUser, "test_project"), 1L, new FinishTestItemRQ());
	}

	@Test
	public void finishTestItemUnderNotExistedLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Launch '' not found. Did you use correct Launch ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		TestItem item = new TestItem();
		when(repository.findById(1L)).thenReturn(Optional.of(item));

		handler.finishTestItem(rpUser, extractProjectDetails(rpUser, "test_project"), 1L, new FinishTestItemRQ());
	}

	@Test
	public void finishTestItemByNotLaunchOwner() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Finish test item is not allowed. You are not a launch owner.");

		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		TestItem item = new TestItem();
		Launch launch = new Launch();
		User user = new User();
		user.setLogin("owner");
		launch.setUser(user);
		item.setLaunch(launch);
		when(repository.findById(1L)).thenReturn(Optional.of(item));

		handler.finishTestItem(rpUser, extractProjectDetails(rpUser, "test_project"), 1L, new FinishTestItemRQ());
	}

	@Test
	public void finishAlreadyFinishedTestItem() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Reporting for item 1 already finished. Please, check item status.");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		TestItem item = new TestItem();
		item.setItemId(1L);
		TestItemResults results = new TestItemResults();
		results.setStatus(StatusEnum.FAILED);
		item.setItemResults(results);
		Launch launch = new Launch();
		User user = new User();
		user.setLogin("test");
		launch.setUser(user);
		item.setLaunch(launch);
		when(repository.findById(1L)).thenReturn(Optional.of(item));

		handler.finishTestItem(rpUser, extractProjectDetails(rpUser, "test_project"), 1L, new FinishTestItemRQ());
	}

	@Test
	public void finishStepItemWithoutProvidedStatus() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				"Test item status is ambiguous. There is no status provided from request and there are no descendants to check statistics for test item id '1'");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		TestItem item = new TestItem();
		item.setItemId(1L);
		TestItemResults results = new TestItemResults();
		results.setStatus(StatusEnum.IN_PROGRESS);
		item.setItemResults(results);
		Launch launch = new Launch();
		User user = new User();
		user.setLogin("test");
		launch.setUser(user);
		item.setLaunch(launch);
		item.setHasChildren(false);
		when(repository.findById(1L)).thenReturn(Optional.of(item));

		handler.finishTestItem(rpUser, extractProjectDetails(rpUser, "test_project"), 1L, new FinishTestItemRQ());
	}
}