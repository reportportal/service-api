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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.launch.impl.DeleteLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import static com.epam.ta.reportportal.database.entity.user.UserRole.USER;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteLaunchHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void deleteLaunchNotOwnedByUser() {
		final String launchId = "launchId";
		final String projectId = "project";
		final String member = "member";
		DeleteLaunchHandler deleteLaunchHandler = new DeleteLaunchHandler(Mockito.mock(ApplicationEventPublisher.class),
				launchRepositoryMock(launchId, projectId), projectRepositoryMock(projectId, member), userRepositoryMock(member)
		);
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(Suppliers.clearPlaceholders(ACCESS_DENIED.getDescription()));
		deleteLaunchHandler.deleteLaunch(launchId, projectId, member);
	}

	private UserRepository userRepositoryMock(String member) {
		UserRepository userRepository = mock(UserRepository.class);
		User user = new User();
		user.setLogin(member);
		user.setRole(USER);
		when(userRepository.findOne(member)).thenReturn(user);
		return userRepository;
	}

	@SuppressWarnings("serial")
	private ProjectRepository projectRepositoryMock(String projectId, final String member) {
		ProjectRepository projectRepository = mock(ProjectRepository.class);
		Project project = new Project();
		project.setName(projectId);
		project.setUsers(ImmutableList.<Project.UserConfig>builder().add(
				Project.UserConfig.newOne().withLogin(member).withProjectRole(ProjectRole.MEMBER)).build());
		when(projectRepository.findOne(projectId)).thenReturn(project);
		return projectRepository;
	}

	private LaunchRepository launchRepositoryMock(String launchId, String projectId) {
		LaunchRepository launchRepository = mock(LaunchRepository.class);
		Launch launch = new Launch();
		launch.setId(launchId);
		launch.setUserRef("random");
		launch.setStatus(Status.FAILED);
		launch.setProjectRef(projectId);
		when(launchRepository.findOne(launchId)).thenReturn(launch);
		return launchRepository;
	}
}
