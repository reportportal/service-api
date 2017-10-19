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
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.sharing.AclPermissions;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringFixture("shareableRepositoryTest")
public class BaseACLTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Test
	public void testSaveReadAcl() {
		Dashboard dashboard = new Dashboard();
		dashboard.setName("test");
		dashboard.setOwner("default");
		AclEntry aclEntry = new AclEntry();
		aclEntry.setProjectId("default_proj");
		aclEntry.addPermission(AclPermissions.READ);
		dashboard.addAclEntry(aclEntry);
		dashboard = dashboardRepository.save(dashboard);
		Dashboard loadedDashboard = dashboardRepository.findOne(dashboard.getId());
		Assert.assertNotNull(loadedDashboard);
		Assert.assertNotNull(loadedDashboard.getAcl());
	}

}