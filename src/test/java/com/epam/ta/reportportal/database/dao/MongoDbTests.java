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
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.user.UserType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Calendar;
import java.util.Collections;

public class MongoDbTests extends BaseTest {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Test
	public void checkFindById() {

		final String DEFAULT_PROJECT = "default_project";

		Assert.assertNotNull(projectRepository);

		mongoTemplate.dropCollection(Project.class);
		mongoTemplate.dropCollection(User.class);

		User user = new User();
		user.setLogin("default");
		user.setPassword("3fde6bb0541387e4ebdadf7c2ff31123");
		user.setType(UserType.INTERNAL);
		user.setDefaultProject(DEFAULT_PROJECT);
		user.getMetaInfo().setLastLogin(Calendar.getInstance().getTime());
		user.setRole(UserRole.ADMINISTRATOR);
		user.setEmail("TesterUsername@epam.com");
		user.setFullName("RP Tester");
		userRepository.save(user);

		Project project = new Project();
		project.setCustomer("some customer");
		project.setName(DEFAULT_PROJECT);
		project.setAddInfo("some additional info");
		project.setUsers(Collections.singletonList(Project.UserConfig.newOne()
				.withProjectRole(ProjectRole.MEMBER)
				.withProposedRole(ProjectRole.MEMBER)
				.withLogin(user.getId())));
		projectRepository.save(project);

		Project savedProject = projectRepository.findOne(project.getName());
		Assert.assertNotNull(savedProject);

		Assert.assertTrue(projectRepository.isAssignedToProject(project.getName(), user.getLogin()));

	}

	@After
	public void dropDatabase() {
		// mongoTemplate.dropCollection(Project.class);
	}
}
