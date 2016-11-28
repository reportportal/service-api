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

import java.util.Iterator;

import com.epam.ta.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.Link;

import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;

public class UserFilterResourceBuilderTest extends BaseTest {

	@Autowired
	@Qualifier("userFilterResourceBuilder.reference")
	private LazyReference<UserFilterResourceBuilder> filterResourceBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		filterResourceBuilderProvider.get().addUserFilter(null).addLink(new Link(BuilderTestsConstants.LINK)).build();
	}

	@Test
	public void testNullExtended() {
		UserFilter userFilter = Utils.getUserFilter();
		userFilter.setFilter(null);
		userFilter.setSelectionOptions(null);
		UserFilterResource actualValue = filterResourceBuilderProvider.get().addUserFilter(userFilter).build();
		UserFilterResource expectedValue = Utils.getUserFilterResource();
		expectedValue.setEntities(null);
		expectedValue.setObjectType(null);
		expectedValue.setSelectionParameters(null);
		validate(expectedValue, actualValue);
	}

	@Test
	public void testValues() {
		UserFilter userFilter = Utils.getUserFilter();
		userFilter.setId(BuilderTestsConstants.BINARY_DATA_ID);
		UserFilterResource actualValue = filterResourceBuilderProvider.get().addUserFilter(userFilter)
				.addLink(new Link(BuilderTestsConstants.LINK)).build();
		UserFilterResource expectedValue = Utils.getUserFilterResource();
		expectedValue.add(new Link(BuilderTestsConstants.LINK));
		expectedValue.setFilterId(BuilderTestsConstants.BINARY_DATA_ID);
		validate(expectedValue, actualValue);
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue("User filter resource builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(UserFilterResourceBuilder.class)[0]));
	}

	private void validate(UserFilterResource expectedValue, UserFilterResource actualValue) {
		Assert.assertEquals(expectedValue.getFilterId(), actualValue.getFilterId());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		Assert.assertEquals(expectedValue.getObjectType(), actualValue.getObjectType());
		Assert.assertEquals(expectedValue.getLinks(), actualValue.getLinks());
		if (expectedValue.getEntities() == null) {
			return;
		}
		Iterator<UserFilterEntity> expectedEntityIter = expectedValue.getEntities().iterator();
		Iterator<UserFilterEntity> actualEntityIter = actualValue.getEntities().iterator();

		Assert.assertEquals(expectedValue.getSelectionParameters(), actualValue.getSelectionParameters());

		while (expectedEntityIter.hasNext()) {
			UserFilterEntity expectedEntity = expectedEntityIter.next();
			if (!actualEntityIter.hasNext()) {
				Assert.fail("Filter entities aren't equal");
			}
			UserFilterEntity actualEntity = actualEntityIter.next();
			Assert.assertEquals(expectedEntity.getCondition(), actualEntity.getCondition());
			Assert.assertEquals(expectedEntity.getFilteringField(), actualEntity.getFilteringField());
			Assert.assertEquals(expectedEntity.getValue(), actualEntity.getValue());
		}
	}

}