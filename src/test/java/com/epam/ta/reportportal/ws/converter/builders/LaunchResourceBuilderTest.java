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
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

public class LaunchResourceBuilderTest extends BaseTest {

	@Autowired
	private Provider<LaunchResourceBuilder> launchResourceBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testBeanScope() {
		Assert.assertTrue("Launch resource builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(LaunchResourceBuilder.class)[0]));
	}

	@Test(expected = NullPointerException.class)
	public void testNull() {
		LaunchResource actualResource = launchResourceBuilderProvider.get().addLaunch(null)
				.build();
		LaunchResource expectedResource = new LaunchResource();
		validateResources(expectedResource, actualResource);
	}

	@Test
	public void testValues() {
		LaunchResource actualResource = launchResourceBuilderProvider.get().addLaunch(Utils.getLaunch()).build();
		LaunchResource expectedResource = Utils.getLaunchResource();
		validateResources(expectedResource, actualResource);
	}

	private void validateResources(LaunchResource expectedResource, LaunchResource actualResource) {
		Assert.assertEquals(expectedResource.getLaunchId(), actualResource.getLaunchId());
		Assert.assertEquals(expectedResource.getName(), actualResource.getName());
		Assert.assertEquals(expectedResource.getDescription(), actualResource.getDescription());
		Assert.assertEquals(expectedResource.getStatus() == null ? null : expectedResource.getStatus(),
				expectedResource.getStatus() == null ? null : expectedResource.getStatus());
		Assert.assertEquals(expectedResource.getStartTime(), actualResource.getStartTime());
		Assert.assertEquals(expectedResource.getEndTime(), actualResource.getEndTime());
		Assert.assertEquals(expectedResource.getOwner(), actualResource.getOwner());
	}

}