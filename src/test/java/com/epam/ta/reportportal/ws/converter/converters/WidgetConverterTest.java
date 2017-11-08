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

import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.builders.BuilderTestsConstants;
import com.epam.ta.reportportal.ws.converter.builders.Utils;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel_Bortnik
 */
public class WidgetConverterTest {

	@Test(expected = NullPointerException.class)
	public void testNull() {
		WidgetConverter.TO_RESOURCE.apply(null);
	}

	@Test
	public void testNullExtended() {
		Widget widget = Utils.getWidget();
		widget.setId(BuilderTestsConstants.BINARY_DATA_ID);
		widget.setApplyingFilterId(null);
		widget.setContentOptions(null);
		WidgetResource actualValue = WidgetConverter.TO_RESOURCE.apply(widget);
		WidgetResource expectedValue = new WidgetResource();
		expectedValue.setName(BuilderTestsConstants.NAME);
		expectedValue.setWidgetId(BuilderTestsConstants.BINARY_DATA_ID);
		validate(expectedValue, actualValue);
	}

	@Test
	public void testValues() {
		WidgetResource actualValue = WidgetConverter.TO_RESOURCE.apply(Utils.getWidget());
		WidgetResource expectedValue = new WidgetResource();
		expectedValue.setContentParameters(new ContentParameters());
		expectedValue.setName(BuilderTestsConstants.NAME);
		expectedValue.setFilterId("1234");
		validate(expectedValue, actualValue);
	}

	private void validate(WidgetResource expectedValue, WidgetResource actualValue) {
		Assert.assertEquals(expectedValue.getFilterId(), actualValue.getFilterId());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		Assert.assertEquals(expectedValue.getWidgetId(), actualValue.getWidgetId());
		Assert.assertEquals(expectedValue.getContentParameters(), actualValue.getContentParameters());
		Assert.assertEquals(expectedValue.getDescription(), actualValue.getDescription());

	}
}
