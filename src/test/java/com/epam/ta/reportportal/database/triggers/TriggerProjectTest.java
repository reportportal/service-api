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
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.personal.PersonalProjectService;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.singletonList;

@SpringFixture("unitTestsProjectTriggers")
public class TriggerProjectTest extends BaseTest {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonalProjectService personalProjectService;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void testDeleteDefaultProject() {
		User user = userRepository.findOne("user1");

		projectRepository.save(personalProjectService.generatePersonalProject(user));

		Assert.assertEquals("test_123", user.getDefaultProject());
		projectRepository.delete(singletonList("test_123"));
		user = userRepository.findOne("user1");
		Assert.assertEquals(user.getId() + PersonalProjectService.PERSONAL_PROJECT_POSTFIX, user.getDefaultProject());
	}
}
