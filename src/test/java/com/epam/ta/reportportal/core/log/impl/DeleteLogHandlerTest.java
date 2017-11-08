/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.*;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.user.UserRole.USER;
import static com.epam.ta.reportportal.ws.model.ErrorType.LOG_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.TEST_ITEM_IS_NOT_FINISHED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteLogHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void logNotFound() {
		final String logId = "notFound";
		final String projectId = "test-project";
		final String userId = "user1";
		final DeleteLogHandler deleteLogHandler = new DeleteLogHandler();
		deleteLogHandler.setProjectRepository(projectRepositoryMock(projectId, userId));
		deleteLogHandler.setUserRepository(userRepositoryMock(userId));
		deleteLogHandler.setLaunchRepository(mock(LaunchRepository.class));
		deleteLogHandler.setTestItemRepository(mock(TestItemRepository.class));
		deleteLogHandler.setLogRepository(mock(LogRepository.class));

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(formattedSupplier(LOG_NOT_FOUND.getDescription(), logId).get());
		deleteLogHandler.deleteLog(logId, projectId, userId);
	}

	@Test
	public void testItemInProgress() {
		String logId = "logId";
		String testId = "testItem";
		String launchId = "launchRef";
		String projectId = "test-project";
		String owner = "user1";
		DeleteLogHandler deleteLogHandler = new DeleteLogHandler();
		deleteLogHandler.setLaunchRepository(launchRepositoryMock(launchId, projectId, owner));
		deleteLogHandler.setProjectRepository(projectRepositoryMock(projectId, owner));
		deleteLogHandler.setUserRepository(userRepositoryMock(owner));
		deleteLogHandler.setTestItemRepository(itemRepositoryMock(logId, launchId, testId));
		deleteLogHandler.setLogRepository(logRepositoryMock(logId, testId));

		String message = formattedSupplier("Unable to delete log '{}' when test item '{}' in progress state", logId, testId).get();
		String errorMessage = formattedSupplier(TEST_ITEM_IS_NOT_FINISHED.getDescription(), message).get();
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(errorMessage);
		deleteLogHandler.deleteLog(logId, projectId, owner);
	}

	static UserRepository userRepositoryMock(String member) {
		UserRepository userRepository = mock(UserRepository.class);
		User user = new User();
		user.setLogin(member);
		user.setRole(USER);
		when(userRepository.findOne(member)).thenReturn(user);
		return userRepository;
	}

	@SuppressWarnings("serial")
	static ProjectRepository projectRepositoryMock(String projectId, final String member) {
		ProjectRepository projectRepository = mock(ProjectRepository.class);
		Project project = new Project();
		project.setUsers(Lists.newArrayList(new Project.UserConfig().withLogin(member).withProjectRole(ProjectRole.MEMBER)));
		when(projectRepository.findOne(projectId)).thenReturn(project);
		return projectRepository;
	}

	static LaunchRepository launchRepositoryMock(String launchId, String projectId, String owner) {
		LaunchRepository launchRepository = mock(LaunchRepository.class);
		Launch launch = new Launch();
		launch.setId(launchId);
		launch.setUserRef("random");
		launch.setStatus(Status.FAILED);
		launch.setProjectRef(projectId);
		launch.setUserRef(owner);
		when(launchRepository.findOne(launchId)).thenReturn(launch);
		return launchRepository;
	}

	static TestItemRepository itemRepositoryMock(String logId, String launchId, String testId) {
		TestItemRepository testItemRepository = mock(TestItemRepository.class);
		TestItem testItem = new TestItem();
		testItem.setStatus(Status.IN_PROGRESS);
		testItem.setId(testId);
		testItem.setLaunchRef(launchId);
		when(testItemRepository.findOne(testId)).thenReturn(testItem);
		return testItemRepository;
	}

	static LogRepository logRepositoryMock(String logId, String testId) {
		LogRepository logRepository = mock(LogRepository.class);
		Log log = new Log();
		log.setId(logId);
		log.setTestItemRef(testId);
		when(logRepository.findOne(logId)).thenReturn(log);
		return logRepository;
	}
}
