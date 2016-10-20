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

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.triggers.CascadeDeleteProjectsService;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.epam.ta.reportportal.database.personal.PersonalProjectUtils.personalProjectName;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@SpringFixture("unitTestsProjectTriggers")
public class TriggerProjectTest extends BaseTest {

	@Autowired
	private LaunchRepository launchRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TestItemRepository testItemRepository;
	@Autowired
	private CascadeDeleteProjectsService cascadeDeleteProjectsService;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void testDeleteByProjectList() {
		List<Launch> launches = launchRepository.findAll();
		List<Project> projects = new ArrayList<>();

		Project project2 = projectRepository.findOne(launches.get(2).getProjectRef());
		Project project3 = projectRepository.findOne(launches.get(3).getProjectRef());

		projects.add(project2);
		projects.add(project3);

		cascadeDeleteProjectsService.delete(projects.stream().map(Project::getId).collect(toList()));
		Assert.assertNull(projectRepository.findOne(project2.getId()));
		Assert.assertNull(projectRepository.findOne(project3.getId()));
		Launch launch2 = launchRepository.findOne(launches.get(2).getId());
		Launch launch3 = launchRepository.findOne(launches.get(3).getId());
		Assert.assertNull(launch2);
		Assert.assertNull(launch3);
		Assert.assertTrue(testItemRepository.findIdsByLaunch(launches.get(2).getId()).isEmpty());
		Assert.assertTrue(testItemRepository.findIdsByLaunch(launches.get(3).getId()).isEmpty());
	}

	@Test
	public void testDeleteDefaultProject() {
		User user = userRepository.findOne("user1");
		Assert.assertEquals("test_123", user.getDefaultProject());
		cascadeDeleteProjectsService.delete(singletonList("test_123"));
		user = userRepository.findOne("user1");
		Assert.assertEquals(personalProjectName(user.getId()), user.getDefaultProject());
	}
}