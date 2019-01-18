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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
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
public class DeleteTestItemHandlerImplTest {

	@Mock
	private TestItemRepository repository;

	@InjectMocks
	private DeleteTestItemHandler handler = new DeleteTestItemHandlerImpl();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testItemNotFound() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Test Item '1' not found. Did you use correct Test Item ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(repository.findById(1L)).thenReturn(Optional.empty());

		handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void deleteInProgressItem() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unable to perform operation for non-finished test item. Unable to delete test item ['1'] in progress state");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(repository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.IN_PROGRESS, StatusEnum.IN_PROGRESS, 1L, "test")));

		handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void deleteTestItemWithInProgressLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				"Unable to perform operation for non-finished launch. Unable to delete test item ['1'] under launch ['null'] with 'In progress' state");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(repository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.PASSED, StatusEnum.IN_PROGRESS, 1L, "test")));

		handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void deleteTestItemFromAnotherProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. Deleting testItem '1' is not under specified project '1'");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(repository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.PASSED, StatusEnum.FAILED, 2L, "test")));

		handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void deleteNotOwnTestItem() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions");

		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(repository.findById(1L)).thenReturn(Optional.of(getTestItem(StatusEnum.PASSED, StatusEnum.FAILED, 1L, "owner")));

		handler.deleteTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	private TestItem getTestItem(StatusEnum itemStatus, StatusEnum launchStatus, Long projectId, String owner) {
		TestItem item = new TestItem();
		item.setItemId(1L);
		TestItemResults results = new TestItemResults();
		results.setStatus(itemStatus);
		item.setItemResults(results);
		Launch launch = new Launch();
		launch.setStatus(launchStatus);
		launch.setProjectId(projectId);
		User user = new User();
		user.setLogin(owner);
		launch.setUser(user);
		item.setLaunch(launch);
		return item;
	}
}