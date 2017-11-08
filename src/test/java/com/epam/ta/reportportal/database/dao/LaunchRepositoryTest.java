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
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;

@SpringFixture("unitTestsProjectTriggers")
public class LaunchRepositoryTest extends BaseTest {

	public static final String LAUNCH_ID = "51824cc1553de743b3e5aa2c";

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private LaunchRepository launchRepository;

	@Test
	public void testFindNameAndNumberById() {
		Launch launch = launchRepository.findNameNumberAndModeById("51824cc1553de743b3e5aa2c");
		Assert.assertNotNull(launch);
		Assert.assertNotNull(launch.getName());
		Assert.assertNotNull(launch.getNumber());
		Assert.assertNull(launch.getProjectRef());
		Assert.assertNull(launch.getUserRef());
		Assert.assertNull(launch.getLastModified());
		Assert.assertNotNull(launch.getStatus());
		Assert.assertNull(launch.getTags());
	}

	@Test
	public void testFindIdsByFilter() {
		FilterCondition nameCondition = new FilterCondition(Condition.CONTAINS, false, "Demo", "name");
		FilterCondition numberCondition = new FilterCondition(Condition.LOWER_THAN_OR_EQUALS, false, "2", "number");
		Filter filter = new Filter(Launch.class, Sets.newHashSet(nameCondition, numberCondition));
		Sort sort = new Sort(Direction.DESC, "number");
		List<Launch> launches = launchRepository.findIdsByFilter(filter, sort, 2);
		Assert.assertNotNull(launches);
		Assert.assertEquals(2, launches.size());
		Assert.assertTrue(launches.get(0).getNumber().equals(2L));
		Assert.assertTrue(launches.get(1).getNumber().equals(1L));
	}

	@Test
	public void testFindIdsByFilterWithoutLimit() {
		Filter filter = new Filter(Launch.class, Condition.CONTAINS, false, "Demo", "name");
		List<Launch> launches = launchRepository.findIdsByFilter(filter);
		Assert.assertNotNull(launches);
		Assert.assertEquals(2, launches.size());
		for (Launch launch : launches) {
			Assert.assertNotNull(launch.getId());
			Assert.assertNull(launch.getDescription());
			Assert.assertNull(launch.getName());
			Assert.assertNull(launch.getUserRef());
			Assert.assertNull(launch.getEndTime());
			Assert.assertNull(launch.getLastModified());
			Assert.assertEquals(Mode.DEFAULT, launch.getMode());
		}
	}

	@Test
	public void testFindByUserRef() {
		List<Launch> launches = launchRepository.findByUserRef("user1");
		Assert.assertNotNull(launches);
		Assert.assertEquals(4, launches.size());
	}
}