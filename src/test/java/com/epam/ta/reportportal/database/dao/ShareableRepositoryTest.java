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
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

@SpringFixture("shareableRepositoryTest")
public class ShareableRepositoryTest extends BaseTest {

	@Autowired
	private DashboardRepository dashboardRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void testNullFindSharedEntities() {
		Page<Dashboard> page = dashboardRepository.findSharedEntities(null, null, null, null);
		Assert.assertNotNull(page);
		Assert.assertTrue(page.getContent().isEmpty());
	}

	@Test
	public void testFindSharedEntities() {
		List<Dashboard> dashboards = dashboardRepository.findSharedEntities(
				"default_project", Lists.newArrayList("_id", "name"), Shareable.NAME_OWNER_SORT, new PageRequest(0, 10)).getContent();
		Assert.assertNotNull(dashboards);
		Assert.assertEquals(2, dashboards.size());
		Dashboard dashboard = dashboards.get(0);
		Assert.assertNotNull(dashboard.getId());
		Assert.assertNotNull(dashboard.getName());
		Assert.assertNull(dashboard.getProjectName());
		Assert.assertNull(dashboard.getAcl());
		Assert.assertTrue(dashboard.getWidgets().isEmpty());
		Assert.assertNull(dashboard.getCreationDate());
	}

	@Test
	public void testfindOnlyOwnedEntities() {
		List<Dashboard> dashboards = dashboardRepository.findOnlyOwnedEntities(
				Sets.newHashSet("520e1f3818127ca383339f39", "520e1f3818127cad83339f40", "520e1f3818127ca383339341"), "default1");
		Assert.assertNotNull(dashboards);
		Assert.assertEquals(2, dashboards.size());
		for (Dashboard dashboard : dashboards) {
			Assert.assertNotNull(dashboard.getId());
			Assert.assertNotNull(dashboard.getAcl());
			Assert.assertNotNull(dashboard.getName());
			Assert.assertNotNull(dashboard.getProjectName());
			Assert.assertNotNull(dashboard.getCreationDate());
		}
	}

	@Test
	public void testNullfindOnlyOwnedEntities() {
		List<Dashboard> dashboards = dashboardRepository.findOnlyOwnedEntities(null, null);
		Assert.assertNotNull(dashboards);
		Assert.assertTrue(dashboards.isEmpty());
	}

	@Test
	public void testNullfindAllByFilter() {
		Page<Dashboard> dashboards = dashboardRepository.findAllByFilter(null, null, null, null);
		Assert.assertNotNull(dashboards);
	}

	@Test
	public void testfindAllByFilter() {
		Set<FilterCondition> conditions = Sets.newHashSet();
		Filter filter = new Filter(Dashboard.class, conditions);
		Page<Dashboard> dashboards = dashboardRepository.findAllByFilter(filter, new PageRequest(0, 10), "default_project", "default1");
		Assert.assertNotNull(dashboards);
		Assert.assertEquals(3, dashboards.getContent().size());
		List<String> expectedIds = Lists.newArrayList("520e1f3818127ca383339f39", "520e1f3818127cad83339f40", "520e1f3818127ca383339341");
		for (Dashboard dashboard : dashboards) {
			Assert.assertTrue(expectedIds.contains(dashboard.getId()));
		}
	}
}