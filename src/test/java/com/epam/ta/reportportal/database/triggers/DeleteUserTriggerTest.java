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

package com.epam.ta.reportportal.database.triggers;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.findUserConfigByLogin;

@SpringFixture("deleteUserTriggerTests")
public class DeleteUserTriggerTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private DataStorage dataStorage;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private UserFilterRepository userFilterRepository;

	@Test
	@Ignore
	public void testUserTrigger() throws IOException {

		BinaryData data = new BinaryData("image/png", null, new ClassPathResource("doNotBeHesitated.png").getInputStream());
		String photoId = dataStorage.saveData(data, "some file name");

		User user = new User();
		user.setType(UserType.UPSA);
		user.setEmail("email@email.com");
		user.setDefaultProject("some_default_project");
		user.setLogin("some login");
		user.setPhotoId(photoId);

		userRepository.save(user);

		userRepository.delete(user);

		Assert.assertNull(userRepository.findOne("some login"));
		Assert.assertNull(dataStorage.fetchData(photoId));

	}

	@Test
	public void testDeleteUserTrigger() {
		User user = userRepository.findOne("user1");
		Project project = projectRepository.findOne("project1");
		Assert.assertNotNull(project);
		Assert.assertNotNull(user);
		Assert.assertNotNull(findUserConfigByLogin(project, user.getLogin()));
		userRepository.delete(user);
		project = projectRepository.findOne("project1");
		Assert.assertNotNull(project);
		Assert.assertNull(findUserConfigByLogin(project, user.getLogin()));
	}

	@Test
	public void testDeleteShareableData() {
		Assert.assertNotNull(widgetRepository.findOne("520e1f3818127ca383339f31"));
		Assert.assertNotNull(userFilterRepository.findOne("520e1f3818177ca383339d37"));
		Assert.assertNotNull(dashboardRepository.findOne("520e1f3818127ca383339f34"));
		User user = userRepository.findOne("user2");
		Assert.assertNotNull(user);
		userRepository.delete(user);
		Assert.assertNull(userRepository.findOne("user2"));
		// non shared entities
		Assert.assertNull(widgetRepository.findOne("520e1f3818127ca383339f31"));
		Assert.assertNull(userFilterRepository.findOne("520e1f3818177ca383339d37"));
		Assert.assertNull(dashboardRepository.findOne("520e1f3818127ca383339f34"));
		// shared entities
		Assert.assertNotNull(widgetRepository.findOne("520e1f3818127ca383339f32"));
		Assert.assertNotNull(userFilterRepository.findOne("520e1f3818177ca383339d38"));
		Assert.assertNotNull(dashboardRepository.findOne("520e1f3818127ca383339f35"));
	}
}
