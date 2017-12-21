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
import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * @author Dzmitry_Kavalets
 */
@SpringFixture("userPreferenceTest")
public class UserPreferenceRepositoryTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule rule;

	@Autowired
	private UserPreferenceRepository userPreferenceRepository;

	@Test
	public void findByNameAndProjectPositive() {
		String userName = "default";
		String projectName = "default_project";
		UserPreference preference = userPreferenceRepository.findByProjectAndUserName(projectName, userName);
		Assert.assertNotNull(preference);
		Assert.assertEquals(preference.getUserRef(), userName);
		Assert.assertEquals(preference.getProjectRef(), projectName);
		Assert.assertEquals(preference.getLaunchTabs().getActive(), "activeId");
		Assert.assertEquals(preference.getLaunchTabs().getFilters(), Arrays.asList("activeId", "firstFilter", "secondFilter"));
	}

	@Test
	public void deletePreference() {
		String userName = "default1";
		String project1 = "default_project1";
		String project2 = "default_project";
		userPreferenceRepository.deleteByUserName(userName);
		UserPreference prj1Preference = userPreferenceRepository.findByProjectAndUserName(project1, userName);
		UserPreference prj2Preference = userPreferenceRepository.findByProjectAndUserName(project2, userName);
		Assert.assertNull(prj1Preference);
		Assert.assertNull(prj2Preference);
	}

}