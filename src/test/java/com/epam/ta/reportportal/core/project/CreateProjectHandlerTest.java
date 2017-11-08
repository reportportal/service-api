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

package com.epam.ta.reportportal.core.project;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Dzmitry_Kavalets
 */
public class CreateProjectHandlerTest extends BaseTest {

	@Autowired
	private ICreateProjectHandler createProjectHandler;

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	public void testCreateProject() {
		String creator = "user";
		CreateProjectRQ createProjectRQ = new CreateProjectRQ();
		createProjectRQ.setProjectName("new_project");
		createProjectRQ.setEntryType("INTERNAL");
		createProjectRQ.setAddInfo("test project");
		EntryCreatedRS projectRS = createProjectHandler.createProject(createProjectRQ, creator);
		Project project = projectRepository.findOne(projectRS.getId());
		Assert.assertNotNull(project);
		Assert.assertEquals("new_project", projectRS.getId());
		Assert.assertNotNull(project.getUsers());
		Project.UserConfig configByLogin = ProjectUtils.findUserConfigByLogin(project, creator);
		Assert.assertNotNull(configByLogin);
		Assert.assertEquals(ProjectRole.PROJECT_MANAGER, configByLogin.getProjectRole());
	}
}