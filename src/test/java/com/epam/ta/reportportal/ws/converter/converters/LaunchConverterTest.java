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

import com.epam.ta.reportportal.ws.converter.builders.Utils;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel Bortnik
 */
public class LaunchConverterTest {

	@Test(expected = NullPointerException.class)
	public void testNull() {
		LaunchResource actualResource = LaunchConverter.TO_RESOURCE.apply(null);
		LaunchResource expectedResource = new LaunchResource();
		validateResources(expectedResource, actualResource);
	}

	@Test
	public void testValues() {
		LaunchResource actualResource = LaunchConverter.TO_RESOURCE.apply(Utils.getLaunch());
		LaunchResource expectedResource = Utils.getLaunchResource();
		validateResources(expectedResource, actualResource);
	}

	private void validateResources(LaunchResource expectedResource, LaunchResource actualResource) {
		Assert.assertEquals(expectedResource.getLaunchId(), actualResource.getLaunchId());
		Assert.assertEquals(expectedResource.getName(), actualResource.getName());
		Assert.assertEquals(expectedResource.getDescription(), actualResource.getDescription());
		Assert.assertEquals(
				expectedResource.getStatus() == null ? null : expectedResource.getStatus(),
				expectedResource.getStatus() == null ? null : expectedResource.getStatus()
		);
		Assert.assertEquals(expectedResource.getStartTime(), actualResource.getStartTime());
		Assert.assertEquals(expectedResource.getEndTime(), actualResource.getEndTime());
		Assert.assertEquals(expectedResource.getOwner(), actualResource.getOwner());
	}

}