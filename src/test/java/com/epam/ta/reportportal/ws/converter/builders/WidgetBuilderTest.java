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

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

public class WidgetBuilderTest extends BaseTest {

	@Autowired
	private Provider<WidgetBuilder> widgetBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		widgetBuilderProvider.get().addWidgetRQ(null).addContentParameters(null).addFilter(null).addProject(null).build();
	}

	@Test
	public void testValuesNullExtented() throws Exception {
		WidgetRQ widgetRQ = Utils.getWidgetRQ();
		widgetRQ.setContentParameters(null);
		Widget widget = new Widget();
		widget.setName(BuilderTestsConstants.NAME);
		validateWidgets(widget, widgetBuilderProvider.get().addWidgetRQ(widgetRQ).addFilter(null).build());
	}

	@Test
	public void testAllValues() throws Exception {
		WidgetRQ widgetRQ = Utils.getWidgetRQ();
		Widget expectedValue = Utils.getWidget();
		expectedValue.setApplyingFilterId("1234");
		expectedValue.setProjectName(BuilderTestsConstants.PROJECT);
		validateWidgets(
				expectedValue,
				widgetBuilderProvider.get().addWidgetRQ(widgetRQ).addFilter("1234").addProject(BuilderTestsConstants.PROJECT).build()
		);
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue(
				"Widget builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(WidgetBuilder.class)[0])
		);
	}

	private void validateWidgets(Widget expectedValue, Widget actualValue) {
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		Assert.assertEquals(expectedValue.getProjectName(), actualValue.getProjectName());
		Assert.assertEquals(expectedValue.getId(), actualValue.getId());
		Assert.assertEquals(expectedValue.getApplyingFilterId(), actualValue.getApplyingFilterId());
		Assert.assertEquals(expectedValue.getContentOptions(), actualValue.getContentOptions());
	}
}