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
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.Link;

import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;

public class WidgetResourceBuilderTest extends BaseTest {

	@Autowired
	@Qualifier("widgetResourceBuilder.reference")
	private LazyReference<WidgetResourceBuilder> widgetResourceBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		widgetResourceBuilderProvider.get().addWidget(null).addLink(new Link(BuilderTestsConstants.LINK)).build();
	}

	@Test
	public void testNullExtended() {
		Widget widget = Utils.getWidget();
		widget.setId(BuilderTestsConstants.BINARY_DATA_ID);
		widget.setApplyingFilterId(null);
		widget.setContentOptions(null);
		WidgetResource actualValue = widgetResourceBuilderProvider.get().addWidget(widget).addLink(new Link(BuilderTestsConstants.LINK))
				.build();
		WidgetResource expectedValue = new WidgetResource();
		expectedValue.setName(BuilderTestsConstants.NAME);
		expectedValue.add(new Link(BuilderTestsConstants.LINK));
		expectedValue.setWidgetId(BuilderTestsConstants.BINARY_DATA_ID);
		validate(expectedValue, actualValue);
	}

	@Test
	public void testValues() {
		WidgetResource actualValue = widgetResourceBuilderProvider.get().addWidget(Utils.getWidget())
				.addLink(new Link(BuilderTestsConstants.LINK)).build();
		WidgetResource expectedValue = new WidgetResource();
		expectedValue.setContentParameters(new ContentParameters());
		expectedValue.setName(BuilderTestsConstants.NAME);
		expectedValue.add(new Link(BuilderTestsConstants.LINK));
		expectedValue.setApplyingFilterID("1234");
		validate(expectedValue, actualValue);
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue("Widget resource builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(WidgetResourceBuilder.class)[0]));
	}

	private void validate(WidgetResource expectedValue, WidgetResource actualValue) {
		Assert.assertEquals(expectedValue.getApplyingFilterID(), actualValue.getApplyingFilterID());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		Assert.assertEquals(expectedValue.getWidgetId(), actualValue.getWidgetId());
		Assert.assertEquals(expectedValue.getContentParameters(), actualValue.getContentParameters());
		Assert.assertEquals(expectedValue.getLinks(), actualValue.getLinks());
		Assert.assertEquals(expectedValue.getDescription(), actualValue.getDescription());

	}
}