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
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

public class UserFilterBuilderTest extends BaseTest {

	@Autowired
	private Provider<UserFilterBuilder> userFilterBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		userFilterBuilderProvider.get().addCreateRQ(null).addSelectionParamaters(null).addProject(null).build();
	}

	@Test
	public void testValuesNullExtended() throws Exception {
		CreateUserFilterRQ rq = new CreateUserFilterRQ();
		rq.setName(BuilderTestsConstants.NAME);
		rq.setObjectType(BuilderTestsConstants.LAUNCH);
		UserFilter userFilter = userFilterBuilderProvider.get().addCreateRQ(rq).addProject("default_project").build();
		UserFilter expectedValue = Utils.getUserFilter();
		expectedValue.setFilter(null);
		expectedValue.setProjectName("default_project");
		expectedValue.setSelectionOptions(null);
		validateFilters(expectedValue, userFilter);
	}

	@Test
	public void testAllValues() throws Exception {
		CreateUserFilterRQ rq = Utils.getUserFilterRQ();
		validateFilters(Utils.getUserFilter(), userFilterBuilderProvider.get().addCreateRQ(rq).build());
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue("Complex filter builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(UserFilterBuilder.class)[0])
		);
	}

	private void validateFilters(UserFilter expectedValue, UserFilter actualValue) {
		Assert.assertEquals(expectedValue.getProjectName(), actualValue.getProjectName());
		Assert.assertEquals(expectedValue.getName(), actualValue.getName());
		Assert.assertEquals(expectedValue.getId(), actualValue.getId());
		Assert.assertEquals(expectedValue.getFilter(), actualValue.getFilter());
		if (expectedValue.getSelectionOptions() != null) {
			Assert.assertEquals(
					expectedValue.getSelectionOptions().getOrders().get(0).isAsc(),
					actualValue.getSelectionOptions().getOrders().get(0).isAsc()
			);
			Assert.assertEquals(expectedValue.getSelectionOptions().getOrders().get(0).getSortingColumnName(),
					actualValue.getSelectionOptions().getOrders().get(0).getSortingColumnName()
			);
		} else {
			Assert.assertNull(actualValue.getSelectionOptions());
		}
	}

}