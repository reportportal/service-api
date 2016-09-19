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

package com.epam.ta.reportportal.database.triggers;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.commons.Constants;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;

@SpringFixture("unitTestsProjectTriggers")
public class TriggerProjectTest extends BaseTest {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void testDeleteById() {
		Launch savedLaunch = launchRepository.findAll().get(0);
		String toDelete = projectRepository.findOne(savedLaunch.getProjectRef()).getId();
		List<Dashboard> dashboards = dashboardRepository.findByProject(toDelete);
		Assert.assertNotNull(dashboards);
		Assert.assertEquals(1, dashboards.size());
		projectRepository.delete(toDelete);
		dashboards = dashboardRepository.findByProject(toDelete);
		Assert.assertNotNull(dashboards);
		Assert.assertEquals(0, dashboards.size());
		Assert.assertNull(projectRepository.findOne(toDelete));
		Assert.assertNotNull(launchRepository.findOne(savedLaunch.getId()));
		Assert.assertTrue(testItemRepository.findIdsByLaunch(savedLaunch.getId()).isEmpty());
	}

	@Test
	public void testDeleteByProject() {
		Launch savedLaunch = launchRepository.findAll().get(1);
		Project toDelete = projectRepository.findOne(savedLaunch.getProjectRef());
		projectRepository.delete(toDelete);
		Assert.assertNull(projectRepository.findOne(toDelete.getId()));
		Assert.assertNotNull(launchRepository.findOne(savedLaunch.getId()));
		Assert.assertTrue(testItemRepository.findIdsByLaunch(savedLaunch.getId()).isEmpty());
	}

	@Test
	public void testDeleteByProjectList() {
		List<Launch> launchs = launchRepository.findAll();
		List<Project> projects = new ArrayList<>();

		Project project2 = projectRepository.findOne(launchs.get(2).getProjectRef());
		Project project3 = projectRepository.findOne(launchs.get(3).getProjectRef());

		projects.add(project2);
		projects.add(project3);

		projectRepository.delete(projects);
		Assert.assertNull(projectRepository.findOne(project2.getId()));
		Assert.assertNull(projectRepository.findOne(project3.getId()));
		Launch launch2 = launchRepository.findOne(launchs.get(2).getId());
		Launch launch3 = launchRepository.findOne(launchs.get(3).getId());
		Assert.assertNotNull(launch2);
		Assert.assertNotNull(launch3);
		Assert.assertTrue(testItemRepository.findIdsByLaunch(launch2.getId()).isEmpty());
		Assert.assertTrue(testItemRepository.findIdsByLaunch(launch3.getId()).isEmpty());
	}

	@Test
	public void testDeleteDefaultProject() {
		User user = userRepository.findOne("user1");
		Assert.assertEquals("test_123", user.getDefaultProject());
		projectRepository.delete("test_123");
		user = userRepository.findOne("user1");
		Assert.assertEquals(Constants.DEFAULT_PROJECT.toString(), user.getDefaultProject());
	}
}