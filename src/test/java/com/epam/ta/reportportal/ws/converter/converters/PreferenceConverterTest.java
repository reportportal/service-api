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

import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel_Bortnik
 */
public class PreferenceConverterTest {

	@Test
	public void testConvert() {
		UserPreference userPreference = new UserPreference();
		userPreference.setId("id");
		UserPreference.LaunchTabs tabs = new UserPreference.LaunchTabs();
		tabs.setActive("active");
		tabs.setFilters(ImmutableList.<String>builder().add("filter").build());
		userPreference.setLaunchTabs(tabs);
		userPreference.setProjectRef("project");
		userPreference.setUserRef("user");
		PreferenceResource resource = PreferenceConverter.TO_RESOURCE.apply(userPreference);

		Assert.assertEquals(userPreference.getProjectRef(), resource.getProjectRef());
		Assert.assertEquals(userPreference.getLaunchTabs().getFilters(), resource.getFilters());
		Assert.assertEquals(userPreference.getLaunchTabs().getActive(), resource.getActive());
		Assert.assertEquals(userPreference.getUserRef(), resource.getUserRef());
	}

}