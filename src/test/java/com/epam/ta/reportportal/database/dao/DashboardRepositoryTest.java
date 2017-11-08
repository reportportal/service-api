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
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.ws.converter.builders.BuilderTestsConstants;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.util.Date;
import java.util.List;

@SpringFixture("dashboardTriggerTest")
public class DashboardRepositoryTest extends BaseTest {

	public static final String DASHBOARD_1_ID = "520e1f3818127ca383339f34";
	public static final String DASHBOARD_2_ID = "520e1f3818127ca383339f35";

	@Autowired
	private DashboardRepository dashboardRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void testFindOneLoadId() {
		Dashboard dashboard = dashboardRepository.findOneLoadId(BuilderTestsConstants.USER, DASHBOARD_1_ID, BuilderTestsConstants.PROJECT);
		Assert.assertNotNull(dashboard);
		Assert.assertNull(dashboard.getName());

		Assert.assertNull(dashboard.getAcl());
		Assert.assertEquals(0, dashboard.getWidgets().size());
		Assert.assertNull(dashboardRepository.findOneLoadId(BuilderTestsConstants.USER, DASHBOARD_1_ID, "1"));
		Assert.assertNull(dashboardRepository.findOneLoadId("1", DASHBOARD_1_ID, BuilderTestsConstants.PROJECT));
	}

	@Test
	public void testFindByUserAndId() {
		Dashboard dashboard = dashboardRepository.findOne(BuilderTestsConstants.USER, DASHBOARD_2_ID, BuilderTestsConstants.PROJECT);
		Assert.assertNotNull(dashboard);
		Assert.assertEquals("testName", dashboard.getName());
		Assert.assertEquals(BuilderTestsConstants.USER, dashboard.getAcl().getOwnerUserId());
		Assert.assertNotNull(dashboard.getWidgets());
		Assert.assertNull(dashboardRepository.findOne(BuilderTestsConstants.USER, DASHBOARD_2_ID, "1"));
		Assert.assertNull(dashboardRepository.findOne("1", DASHBOARD_2_ID, BuilderTestsConstants.PROJECT));
	}

	@Test
	public void testFindAll() {
		Sort creationDateSort = new Sort(new Order(Direction.DESC, Dashboard.CREATION_DATE));
		List<Dashboard> dashboards = dashboardRepository.findAll(BuilderTestsConstants.USER, creationDateSort,
				BuilderTestsConstants.PROJECT
		);
		Assert.assertEquals(dashboards.size(), 4);
		Date currentDate = null;
		for (Dashboard dashboard : dashboards) {
			if (currentDate == null) {
				currentDate = dashboard.getCreationDate();
			} else {
				Assert.assertTrue(currentDate.after(dashboard.getCreationDate()));
				currentDate = dashboard.getCreationDate();
			}
			Assert.assertNotNull(dashboard.getAcl().getOwnerUserId());
			Assert.assertNotNull(dashboard.getWidgets());
		}

		Assert.assertTrue(dashboardRepository.findAll(BuilderTestsConstants.USER, creationDateSort, "1").size() == 0);
		Assert.assertTrue(dashboardRepository.findAll("1", creationDateSort, BuilderTestsConstants.PROJECT).size() == 0);
	}

	@Test
	public void testFindDashboardById() {
		Dashboard dashboard = dashboardRepository.findOne(DASHBOARD_2_ID);
		Assert.assertEquals(DASHBOARD_2_ID, dashboard.getId());
		Assert.assertNotNull(dashboard.getName());
		Assert.assertNotNull(dashboard.getAcl().getOwnerUserId());
		Assert.assertNotNull(dashboard.getWidgets());
	}

	@Test
	public void testFindEntryById() {
		Dashboard dashboard = dashboardRepository.findEntryById(DASHBOARD_2_ID);
		Assert.assertEquals(DASHBOARD_2_ID, dashboard.getId());
		Assert.assertNull(dashboard.getName());
		Assert.assertNull(dashboard.getAcl());
		Assert.assertEquals(0, dashboard.getWidgets().size());
	}

	@Test
	public void testNullFindByProject() {
		List<Dashboard> dashboards = dashboardRepository.findByProject(null);
		Assert.assertNotNull(dashboards);
		Assert.assertTrue(dashboards.isEmpty());
	}

	@Test
	public void testFindByProject() {
		List<Dashboard> dashboards = dashboardRepository.findByProject(BuilderTestsConstants.PROJECT);
		Assert.assertNotNull(dashboards);
		Assert.assertEquals(4, dashboards.size());
	}

	@Test
	public void testFindOneLoadACL() {
		Dashboard dashboard = dashboardRepository.findOneLoadACL(DASHBOARD_1_ID);
		Assert.assertNotNull(dashboard);
		Assert.assertNotNull(dashboard.getId());
		Assert.assertNotNull(dashboard.getAcl());
		Assert.assertNull(dashboard.getName());
		Assert.assertNotNull(dashboard.getProjectName());
	}
}