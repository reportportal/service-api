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
import com.epam.ta.reportportal.core.project.settings.impl.DeleteProjectSettingsHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;

/**
 * @author Dzmitry_Kavalets
 */
public class CreateProjectHandlerTest extends BaseTest{

	@Autowired
	private ICreateProjectHandler createProjectHandler;

	@Autowired
	private ProjectRepository projectRepository;

	private final String CREATOR = "user";
    private final String PROJECT_NAME = "new_project";
    private final String ENTITY_TYPE = "INTERNAL";
    private final String ADDITIONAL_INFO = "test project";

    @Test
	public void testCreateProject() {
		CreateProjectRQ createProjectRQ = new CreateProjectRQ();
		createProjectRQ.setProjectName(PROJECT_NAME);
		createProjectRQ.setEntryType(ENTITY_TYPE);
		createProjectRQ.setAddInfo(ADDITIONAL_INFO);
		EntryCreatedRS projectRS = createProjectHandler.createProject(createProjectRQ, CREATOR);
		Project project = projectRepository.findOne(projectRS.getId());
		Assert.assertNotNull(project);
		Assert.assertEquals(PROJECT_NAME, projectRS.getId());
		Assert.assertNotNull(project.getUsers());
		Assert.assertTrue(project.getUsers().containsKey(CREATOR));
		Assert.assertEquals(ProjectRole.PROJECT_MANAGER, project.getUsers().get(CREATOR).getProjectRole());
	}

	@Test(expected = ReportPortalException.class)
	public void testExceptionCreateProject() {
		CreateProjectRQ createProjectRQ = new CreateProjectRQ();
		createProjectRQ.setProjectName(PROJECT_NAME);
		createProjectRQ.setEntryType(ENTITY_TYPE);
		createProjectRQ.setAddInfo(ADDITIONAL_INFO);
		createProjectHandler.createProject(createProjectRQ, CREATOR);
	}

}