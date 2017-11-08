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

package com.epam.ta.reportportal.database.dao;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.builders.BuilderTestsConstants;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@SpringFixture("dashboardTriggerTest")
public class WidgetRepositoryTest extends BaseTest {

	public static final String ID = "520e1f3818127ca383339f31";

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private WidgetRepository widgetRepository;

	@Test
	public void testFindByUserAndId() {
		Widget widget = widgetRepository.findByUserAndId(BuilderTestsConstants.USER, ID);
		Assert.assertNotNull(widget);
		Assert.assertEquals("widget1", widget.getName());
	}

	@Test
	@Ignore // widget doesn't contains any filter criteria, findByFilter method
	// tested with user filter object
	public void testFindByFilter() {
		FilterCondition secondCondition = new FilterCondition(Condition.EQUALS, false, "widget1", "name");
		Set<FilterCondition> conditions = Sets.newHashSet(secondCondition);
		Filter filter = new Filter(Widget.class, conditions);
		List<Widget> widgets = widgetRepository.findByFilter(filter);
		Assert.assertNotNull(widgets);
		Assert.assertNotEquals(widgets.size(), 0);
		Assert.assertEquals("widget1", widgets.get(0).getName());
	}
}