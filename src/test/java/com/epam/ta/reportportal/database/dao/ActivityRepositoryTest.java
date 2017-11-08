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
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.search.Filter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;

/**
 * @author Dzmitry_Kavalets
 */
@SpringFixture("activityRepositoryTest")
public class ActivityRepositoryTest extends BaseTest {

	public static final String TEST_ITEM_ID = "44524cc1553de753b3e5bb2f";

	@Autowired
	private ActivityRepository activityRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void findActivitiesByTestItem() {
		List<Activity> activities = activityRepository.findActivitiesByTestItemId(
				TEST_ITEM_ID, new Filter(Activity.class, new HashSet<>()), null);
		Assert.assertNotNull(activities);
		Assert.assertEquals(4, activities.size());
	}
}