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
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class TestItemResourceBuilderTest extends BaseTest {

	@Autowired
	private TestItemResourceAssembler itemResourceAssembler;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@Ignore
	public void testNull() {
		itemResourceAssembler.toResource(null, null);
	}

	@Test
	public void testValues() {
		TestItemResource actualValue = itemResourceAssembler.toResource(Utils.getTestItem(), "PASSED");
		validate(actualValue, Utils.getTestItemResource());
	}

	@Test
	@Ignore
	public void testBeanScope() {
		Assert.assertTrue(
				"User filter resource builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(TestItemResourceAssembler.class)[0])
		);
	}

	private void validate(TestItemResource actualValue, TestItemResource expectedValue) {
		Assert.assertEquals(expectedValue.getDescription(), actualValue.getDescription());
		Assert.assertEquals(expectedValue.getItemId(), actualValue.getItemId());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		Assert.assertEquals(expectedValue.getParent(), actualValue.getParent());
		Assert.assertEquals(expectedValue.getStatus(), actualValue.getStatus());
		Assert.assertEquals(expectedValue.getType(), actualValue.getType());
		Assert.assertEquals(expectedValue.getEndTime(), actualValue.getEndTime());
		Assert.assertEquals(expectedValue.getPathNames(), actualValue.getPathNames());
		Assert.assertEquals(expectedValue.getStartTime(), actualValue.getStartTime());
	}

}
