/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.ws.converter.builders.BuilderTestsConstants;
import com.epam.ta.reportportal.ws.converter.builders.Utils;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource.WidgetObjectModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Pavel_Bortnik
 */
public class DashboardConverterTest {

	@Test(expected = NullPointerException.class)
	public void testNull() {
		DashboardConverter.TO_RESOURCE.apply(null);
	}

	@Test
	public void testNullExtended() {
		Dashboard dashboard = Utils.getDashboard();
		dashboard.setWidgets(null);
		dashboard.setId(BuilderTestsConstants.BINARY_DATA_ID);
		DashboardResource actualValue = DashboardConverter.TO_RESOURCE.apply(dashboard);
		validate(Utils.getDashboardResource(), actualValue);
	}

	@Test
	public void testValues() {
		Dashboard dashboard = Utils.getDashboard();
		dashboard.setId(BuilderTestsConstants.BINARY_DATA_ID);
		DashboardResource actualValue = DashboardConverter.TO_RESOURCE.apply(dashboard);
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

	private void validate(DashboardResource expectedValue, DashboardResource actualValue) {
		Assert.assertEquals(expectedValue.getDashboardId(), actualValue.getDashboardId());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		if (null != expectedValue.getWidgets() && !expectedValue.getWidgets().isEmpty()) {
			Assert.assertEquals(expectedValue.getWidgets().get(0).getWidgetId(), actualValue.getWidgets().get(0).getWidgetId());
			Assert.assertEquals(expectedValue.getWidgets().get(0).getWidgetPosition(), actualValue.getWidgets().get(0).getWidgetPosition());
			Assert.assertEquals(expectedValue.getWidgets().get(0).getWidgetSize(), actualValue.getWidgets().get(0).getWidgetSize());
		} else {
			Assert.assertEquals(expectedValue.getWidgets(), actualValue.getWidgets());
		}
	}
}
