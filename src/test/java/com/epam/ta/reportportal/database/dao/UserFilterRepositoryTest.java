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
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.builders.BuilderTestsConstants;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.util.List;
import java.util.Set;

@SpringFixture("userFilterRepositoryTest")
public class UserFilterRepositoryTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	public static final String ID_1 = "520e1f3818177ca383339d37";
	public static final String ID_2 = "566e1f3818177ca344439d38";

	@Autowired
	private UserFilterRepository filterRepository;

	@Test
	public void testSaveLoadUserFilter() {
		UserFilter userFilter = new UserFilter();
		userFilter.setName("testName");
		Filter filter = new Filter(Launch.class, Condition.EQUALS, false, "test_value", "name");
		userFilter.setFilter(filter);
		filterRepository.save(userFilter);
		UserFilter loadedFilter = filterRepository.findOne(userFilter.getId());
		Assert.assertEquals(userFilter, loadedFilter);
	}

	@Test
	public void testFindOneLoadId() {
		UserFilter filter = filterRepository.findOneLoadId(BuilderTestsConstants.USER, ID_1, BuilderTestsConstants.PROJECT);
		Assert.assertNotNull(filter);
		Assert.assertEquals(ID_1, filter.getId());
		Assert.assertNull(filter.getName());

		Assert.assertNull(filterRepository.findOneLoadId("1", ID_1, BuilderTestsConstants.PROJECT));
		Assert.assertNull(filterRepository.findOneLoadId(BuilderTestsConstants.USER, ID_1, "3"));
	}

	@Test
	public void testFindOne() {
		UserFilter filter = filterRepository.findOne("user1", ID_2, "project1");
		Assert.assertNotNull(filter);
		Assert.assertEquals(ID_2, filter.getId());
		Assert.assertEquals("userFilter2", filter.getName());
		Assert.assertEquals("project1", filter.getProjectName());
		Assert.assertNull(filterRepository.findOne("user11", ID_2, "project1"));
		Assert.assertNull(filterRepository.findOne("user1", ID_2, "project11"));
	}

	@Test
	public void testFindAllNames() {
		Sort sort = new Sort(new Order(Direction.ASC, UserFilter.NAME));
		List<UserFilter> filters = filterRepository.findFilters(BuilderTestsConstants.USER, BuilderTestsConstants.PROJECT, sort, false);
		Assert.assertEquals(2, filters.size());
		for (UserFilter userFilter : filters) {
			Assert.assertNotNull(userFilter.getProjectName());
			Assert.assertNull(userFilter.getSelectionOptions());
			Assert.assertNull(userFilter.getFilter());
			Assert.assertNotNull(userFilter.getId());
			Assert.assertNotNull(userFilter.getName());
		}
		//check sorting
		Assert.assertEquals("userFilter1", filters.get(0).getName());
		Assert.assertEquals("userFilter3", filters.get(1).getName());
		// find shared filters names
		filters = filterRepository.findFilters(BuilderTestsConstants.USER, BuilderTestsConstants.PROJECT, sort, true);
		Assert.assertNotNull(filters);
		Assert.assertTrue(filters.isEmpty());
	}

	@Test
	public void testFindOneByName() {
		UserFilter userFilter = filterRepository.findOneByName(BuilderTestsConstants.USER, "userFilter1", BuilderTestsConstants.PROJECT);
		Assert.assertNotNull(userFilter);
		Assert.assertEquals(ID_1, userFilter.getId());
		Assert.assertNull(userFilter.getName());
	}

	@Test
	public void testFindByFilter() {
		FilterCondition secondCondition = new FilterCondition(Condition.EQUALS, false, "userFilter1", "name");
		Set<FilterCondition> conditions = Sets.newHashSet(secondCondition);
		Filter filter = new Filter(UserFilter.class, conditions);
		List<UserFilter> filters = filterRepository.findByFilter(filter);
		Assert.assertNotNull(filters);
		Assert.assertEquals(filters.size(), 1);
		Assert.assertEquals("userFilter1", filters.get(0).getName());
	}

	@Test
	public void findAvailableFilters() {
		String[] ids = { "520e1f3818177ca383339d37", "520e1f3818177ca383339d38", "566e1f3818177ca344439d38", "52991f3818177ca383239d37" };
		List<UserFilter> availableFilters = filterRepository.findAvailableFilters("default_project", ids, "user2");
		Assert.assertNotNull(availableFilters);
		Assert.assertEquals(2, availableFilters.size());
	}

}