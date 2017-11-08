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
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

public class DashboardBuilderTest extends BaseTest {

	@Autowired
	private Provider<DashboardBuilder> dashboardBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		dashboardBuilderProvider.get().addCreateDashboardRQ(null).addWidgets(null).addProject(null).build();
	}

	@Test
	public void testValues() {
		CreateDashboardRQ rq = new CreateDashboardRQ();
		rq.setName(BuilderTestsConstants.NAME);
		Dashboard result = dashboardBuilderProvider.get().addCreateDashboardRQ(rq).addProject(BuilderTestsConstants.PROJECT).build();
		Assert.assertEquals(result.getName(), rq.getName());
		Assert.assertEquals(result.getProjectName(), BuilderTestsConstants.PROJECT);
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue(
				"Dashboard builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(DashboardBuilder.class)[0])
		);
	}

}