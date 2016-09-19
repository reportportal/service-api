/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.util.HashMap;

import com.epam.ta.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.Link;

import com.epam.ta.reportportal.ws.model.TestItemResource;

public class TestItemResourceBuilderTest extends BaseTest {

	@Autowired
	private TestItemResourceBuilder firstBuilder;

	@Autowired
	private TestItemResourceBuilder secondBuilder;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		firstBuilder.addTestItem(null, null).addPathNames(null).addLink(new Link(BuilderTestsConstants.LINK)).build();
	}

	@Test
	public void testValues() {
		TestItemResource actualValue = secondBuilder.addTestItem(Utils.getTestItem(), "PASSED").addPathNames(new HashMap<>()).build();
		validate(actualValue, Utils.getTestItemResource());
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue("User filter resource builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(TestItemResourceBuilder.class)[0]));
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