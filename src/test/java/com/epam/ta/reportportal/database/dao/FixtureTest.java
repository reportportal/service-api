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
import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@SpringFixture("authTests")
public class FixtureTest extends BaseTest {

	@Autowired
	public UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void checkUsers() {
		Long countOfImportedUsers = userRepository.count();
		Assert.assertThat(countOfImportedUsers.intValue(), greaterThan(0));
	}

	@Test
	public void checkProjects() {
		Long countOfImportedProjects = projectRepository.count();
		Assert.assertThat(countOfImportedProjects.intValue(), greaterThan(0));
	}

	@Test
	public void checkReference() {
		Assert.assertThat(projectRepository.isAssignedToProject(AuthConstants.USER_PROJECT, AuthConstants.TEST_USER), is(true));
	}
}