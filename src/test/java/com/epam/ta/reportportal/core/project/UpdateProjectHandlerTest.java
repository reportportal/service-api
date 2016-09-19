/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.core.project.impl.UpdateProjectHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;

/**
 * @author Dzmitry_Kavalets
 */
@SpringFixture("unitTestsProjectTriggers")
public class UpdateProjectHandlerTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private UpdateProjectHandler updateProjectHandler;

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	public void checkEmptyEmailOptions() {
		String userName = "user1";
		UpdateProjectRQ updateProjectRQ = new UpdateProjectRQ();
		ProjectConfiguration configuration = new ProjectConfiguration();
		configuration.setEntry("INTERNAL");
		configuration.setStatisticCalculationStrategy("TEST_BASED");
		configuration.setProjectSpecific("DEFAULT");
		configuration.setEmailConfig(new ProjectEmailConfig());
		updateProjectRQ.setConfiguration(configuration);
		String project1 = "project1";
		updateProjectHandler.updateProject(project1, updateProjectRQ, userName);
		Project one = projectRepository.findOne(project1);
		Assert.assertNotNull(one.getConfiguration().getEmailConfig());
	}
}