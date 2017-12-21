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
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Dashboard.WidgetObject;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@SpringFixture("dashboardTriggerTest")
public class DashboardTriggerTest extends BaseTest {

	public static final String DASHBOARD_1_ID = "520e1f3818127ca383339f34";
	public static final String DASHBOARD_2_ID = "520e1f3818127ca383339f35";
	public static final String WIDGET_2_ID = "520e1f3818127ca383339f32";
	public static final String DASHBOARD_3_ID = "520e1f3818127ca383339f36";
	public static final String DASHBOARD_4_ID = "520e1f3818127ca383339f37";

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Test
	public void testDeleteAsObject() {
		Dashboard dashboard = dashboardRepository.findOne(DASHBOARD_1_ID);
		WidgetObject widget = dashboard.getWidgets().iterator().next();

		Assert.assertNotNull(widgetRepository.findOne(widget.getWidgetId()));
		dashboardRepository.delete(dashboard);
		Assert.assertNull(dashboardRepository.findOne(dashboard.getId()));
		Assert.assertNull(widgetRepository.findOne(widget.getWidgetId()));
		Assert.assertNotNull(widgetRepository.findOne("520e1f3815337ca383339f38"));
		Assert.assertNull(widgetRepository.findOne(widget.getWidgetId()));
	}

	@Test
	public void testDeleteById() {
		Assert.assertNotNull(widgetRepository.findOne(WIDGET_2_ID));
		dashboardRepository.delete(DASHBOARD_2_ID);
		Assert.assertNull(dashboardRepository.findOne(DASHBOARD_2_ID));
		Assert.assertNull(widgetRepository.findOne(WIDGET_2_ID));
	}

	@Test
	public void testDeleteByAsList() {
		Dashboard firstDashboard = dashboardRepository.findOne(DASHBOARD_3_ID);
		Dashboard secondDashboard = dashboardRepository.findOne(DASHBOARD_4_ID);

		List<Dashboard> dashboards = new ArrayList<>();
		dashboards.add(firstDashboard);
		dashboards.add(secondDashboard);
		WidgetObject firstWidget = firstDashboard.getWidgets().iterator().next();
		WidgetObject secondWidget = secondDashboard.getWidgets().iterator().next();

		Assert.assertNotNull(widgetRepository.findOne(firstWidget.getWidgetId()));
		Assert.assertNotNull(widgetRepository.findOne(secondWidget.getWidgetId()));

		dashboardRepository.delete(dashboards);

		Assert.assertNull(dashboardRepository.findOne(DASHBOARD_3_ID));
		Assert.assertNull(dashboardRepository.findOne(DASHBOARD_4_ID));

		Assert.assertNull(widgetRepository.findOne(firstWidget.getWidgetId()));
		Assert.assertNull(widgetRepository.findOne(secondWidget.getWidgetId()));
	}

}