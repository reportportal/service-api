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

package com.epam.ta.reportportal.database.dao;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.doesHaveUser;

@SpringFixture("removeUserTest")
public class ProjectRepositoryTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	public void checkRemoveUserFromProject() {
		projectRepository.removeUserFromProjects("user1");
		Project project1 = projectRepository.findOne("project1");
		Project project2 = projectRepository.findOne("project2");

		Assert.assertFalse(doesHaveUser(project1, "user1"));
		Assert.assertFalse(doesHaveUser(project2, "user1"));
	}
}