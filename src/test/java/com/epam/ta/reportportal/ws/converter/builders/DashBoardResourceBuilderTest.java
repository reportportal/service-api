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
 
package com.epam.ta.reportportal.ws.converter.builders;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.epam.ta.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource.WidgetObjectModel;

import javax.inject.Provider;

public class DashBoardResourceBuilderTest extends BaseTest {

	
	@Autowired
	private Provider<DashboardResourceBuilder> dashboardResourceBuilderProvider;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Test
	public void testNull() {
		dashboardResourceBuilderProvider.get().addDashboard(null).build();
	}
	
	@Test
	public void testNullExtended() {
		Dashboard dashboard = Utils.getDashboard();
		dashboard.setWidgets(null);
		dashboard.setId(BuilderTestsConstants.BINARY_DATA_ID);
		DashboardResource actualValue = dashboardResourceBuilderProvider.get().addDashboard(dashboard).build();
		validate(Utils.getDashboardResource(), actualValue);
	}
	
	@Test
	public void testValues() {
		Dashboard dashboard = Utils.getDashboard();
		dashboard.setId(BuilderTestsConstants.BINARY_DATA_ID);
		DashboardResource actualValue = dashboardResourceBuilderProvider.get().addDashboard(dashboard).build();
		List<WidgetObjectModel> actualWidgets = new LinkedList<>();
		List<Integer> size = new ArrayList<>();
		size.add(500);
		size.add(300);
		List<Integer> position = new ArrayList<>();
		position.add(0);
		position.add(0);
		WidgetObjectModel actualWidget = new WidgetObjectModel("12345678", size, position);
		actualWidgets.add(actualWidget);
		actualValue.setWidgets(actualWidgets);
		
		DashboardResource expectedValue = Utils.getDashboardResource();
		List<WidgetObjectModel> widgets = new LinkedList<>();
		WidgetObjectModel widget = new WidgetObjectModel("12345678", size, position);
		widgets.add(widget);
		expectedValue.setWidgets(widgets);
		validate(expectedValue, actualValue);
	}
	
	@Test
	public void testBeanScope() {
		Assert.assertTrue(
				"Dashboard resource builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext
						.getBeanNamesForType(DashboardResourceBuilder.class)[0]));
	}

	private void validate(DashboardResource expectedValue, DashboardResource actualValue) {
		Assert.assertEquals(expectedValue.getDashboardId(), actualValue.getDashboardId());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		if(null != expectedValue.getWidgets()) {
			Assert.assertEquals(expectedValue.getWidgets().get(0).getWidgetId(), actualValue.getWidgets().get(0).getWidgetId());
			Assert.assertEquals(expectedValue.getWidgets().get(0).getWidgetPosition(), actualValue.getWidgets().get(0).getWidgetPosition());
			Assert.assertEquals(expectedValue.getWidgets().get(0).getWidgetSize(), actualValue.getWidgets().get(0).getWidgetSize());
		}
		else
			Assert.assertEquals(expectedValue.getWidgets(), actualValue.getWidgets());
	}
}
