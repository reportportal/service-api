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
import com.epam.ta.reportportal.core.launch.impl.GetLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.LaunchResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Dzmitry_Kavalets
 */
public class GetLaunchHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void getDebugLaunchByCustomer() {
		String fromDebug = "fromDebug";
		String projectName = "project";
		String user = "customer";

		Launch launch = buildLaunch(fromDebug, projectName);
		LaunchRepository launchRepository = mock(LaunchRepository.class);
		when(launchRepository.findOne(fromDebug)).thenReturn(launch);

		ProjectRepository projectRepository = mock(ProjectRepository.class);
		Project project = buildProject(user);
		when(projectRepository.findOne(projectName)).thenReturn(project);

		GetLaunchHandler getLaunchHandler = new GetLaunchHandler(mock(LaunchResourceAssembler.class), launchRepository);
		getLaunchHandler.setProjectRepository(projectRepository);

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(Suppliers.clearPlaceholders(ErrorType.ACCESS_DENIED.getDescription()));
		getLaunchHandler.getLaunch(fromDebug, user, projectName);
	}

	private Project buildProject(String user) {
		Project project = new Project();
		List<Project.UserConfig> users = ImmutableList.<Project.UserConfig>builder()
				.add(Project.UserConfig.newOne().withProjectRole(ProjectRole.CUSTOMER)
				.withLogin(user)).build();
		project.setUsers(users);
		return project;
	}

	private Launch buildLaunch(String fromDebug, String projectName) {
		Launch launch = new Launch();
		launch.setId(fromDebug);
		launch.setProjectRef(projectName);
		launch.setMode(Mode.DEBUG);
		return launch;
	}
}
