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

package com.epam.ta.reportportal.core.acl;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.sharing.AclPermissions;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringFixture("dashboardTriggerTest")
public class BaseSharingTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private SharingService sharingService;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private UserFilterRepository userFilterRepository;

	@Test
	public void testShareSingleFilter() {
		UserFilter userFilter = userFilterRepository.findOne("566e1f3818177ca344439d38");
		sharingService.modifySharing(Lists.newArrayList(userFilter), "user1", "default_project", true);
		Assert.assertTrue(isShared(userFilter, "default_project"));
	}

	@Test
	public void testMonopolizeSingleFilter() {
		UserFilter userFilter = userFilterRepository.findOne("566e1f3818177ca344439d38");
		sharingService.modifySharing(Lists.newArrayList(userFilter), "user1", "default_project", true);
		sharingService.modifySharing(Lists.newArrayList(userFilter), "user1", "default_project", false);
		Assert.assertFalse(isShared(userFilter, "default_project"));
	}

	@Test
	public void testShareSingleWidget() {
		Widget widget = widgetRepository.findOne("520e1f3818127ca383339f31");
		sharingService.modifySharing(Lists.newArrayList(widget), "user2", "default_project", true);
		Assert.assertTrue(isShared(widget, "default_project"));
	}

	@Test
	public void testMonopolizeSingleWidget() {
		Widget widget = widgetRepository.findOne("520e1f3818127ca383339f31");
		sharingService.modifySharing(Lists.newArrayList(widget), "user2", "default_project", true);
		sharingService.modifySharing(Lists.newArrayList(widget), "user2", "default_project", false);
		Assert.assertFalse(isShared(widget, "default_project"));
	}

	@Test
	public void testShareSingleDashboard() {
		Dashboard dashboard = dashboardRepository.findOne("520e1f3818127ca383334342");
		sharingService.modifySharing(Lists.newArrayList(dashboard), "default2", "default_project1", true);
		Assert.assertTrue(isShared(dashboard, "default_project1"));
	}

	@Test
	public void testMonopolizeSingleDashboard() {
		Dashboard dashboard = dashboardRepository.findOne("520e1f3818127ca383334342");
		sharingService.modifySharing(Lists.newArrayList(dashboard), "default2", "default_project1", true);
		sharingService.modifySharing(Lists.newArrayList(dashboard), "default2", "default_project1", false);
		Assert.assertFalse(isShared(dashboard, "default_project1"));
	}

	@Test
	public void testChainShareWidget() {
		Widget widget = widgetRepository.findOne("520e1f3818127ca383339f31");
		sharingService.modifySharing(Lists.newArrayList(widget), "user2", "default_project", true);
		UserFilter userFilter = userFilterRepository.findOne("520e1f3818177ca383339d37");
		Assert.assertTrue(isShared(widget, "default_project"));
		Assert.assertTrue(isShared(userFilter, "default_project"));
	}

	@Test
	public void testChainMonopolizeWidget() {
		Widget widget = widgetRepository.findOne("520e1f3818127ca383339f31");
		sharingService.modifySharing(Lists.newArrayList(widget), "user2", "default_project", true);
		sharingService.modifySharing(Lists.newArrayList(widget), "user2", "default_project", false);
		UserFilter userFilter = userFilterRepository.findOne("520e1f3818177ca383339d37");
		Assert.assertFalse(isShared(widget, "default_project"));
		Assert.assertTrue(isShared(userFilter, "default_project"));
	}

	@Test
	public void testChainShareDashboard() {
		Dashboard dashboard = dashboardRepository.findOne("520e1f3818127ca383339f34");
		sharingService.modifySharing(Lists.newArrayList(dashboard), "user2", "default_project", true);
		UserFilter userFilter = userFilterRepository.findOne("520e1f3818177ca383339d37");
		Widget widget = widgetRepository.findOne("520e1f3818127ca383339f31");
		Assert.assertTrue(isShared(dashboard, "default_project"));
		Assert.assertTrue(isShared(widget, "default_project"));
		Assert.assertTrue(isShared(userFilter, "default_project"));
	}

	@Test
	public void testChainMonopolizeDashboard() {
		Dashboard dashboard = dashboardRepository.findOne("520e1f3818127ca383339f34");
		sharingService.modifySharing(Lists.newArrayList(dashboard), "user2", "default_project", true);
		sharingService.modifySharing(Lists.newArrayList(dashboard), "user2", "default_project", false);

		UserFilter userFilter = userFilterRepository.findOne("520e1f3818177ca383339d37");
		Widget widget = widgetRepository.findOne("520e1f3818127ca383339f31");
		Assert.assertFalse(isShared(dashboard, "default_project"));
		Assert.assertFalse(isShared(widget, "default_project"));
		Assert.assertTrue(isShared(userFilter, "default_project"));
	}

	private boolean isShared(Shareable shareable, String projectName) {
		for (AclEntry entry : shareable.getAcl().getEntries()) {
			if (entry.getProjectId().equals(projectName) && entry.getPermissions().contains(AclPermissions.READ)) {
				return true;
			}
		}
		return false;
	}
}