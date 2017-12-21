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
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class LaunchBuilderTest extends BaseTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		Launch actualLaunch = new LaunchBuilder().addUser(null).addStatus(null).addStartRQ(null).addProject(null).addEndTime(null).get();
		Launch expectedLaunch = new Launch();
		validateLaunches(expectedLaunch, actualLaunch);
	}

	@Test
	public void testValues() {
		Launch actualLaunch = new LaunchBuilder().addStartRQ(Utils.getStartLaunchRQ())
				.addEndTime(BuilderTestsConstants.DATE_END)
				.addProject(Utils.getProject().getId())
				.addStatus(Status.IN_PROGRESS)
				.addUser(Utils.getUser().getId())
				.get();
		Launch expectedLaunch = Utils.getLaunch();
		validateLaunches(expectedLaunch, actualLaunch);
	}

	private void validateLaunches(Launch expectedLaunch, Launch actualLaunch) {
		Assert.assertEquals(expectedLaunch.getDescription(), actualLaunch.getDescription());
		Assert.assertEquals(expectedLaunch.getName(), actualLaunch.getName());
		Assert.assertEquals(expectedLaunch.getStartTime(), actualLaunch.getStartTime());
		Assert.assertEquals(expectedLaunch.getTags(), actualLaunch.getTags());
		Assert.assertEquals(expectedLaunch.getId(), actualLaunch.getId());
		Assert.assertEquals(expectedLaunch.getId(), actualLaunch.getId());
		Assert.assertEquals(expectedLaunch.getEndTime(), actualLaunch.getEndTime());
		Assert.assertEquals(expectedLaunch.getProjectRef(), actualLaunch.getProjectRef());
		Assert.assertEquals(expectedLaunch.getStatus(), actualLaunch.getStatus());
		Assert.assertEquals(expectedLaunch.getUserRef(), actualLaunch.getUserRef());
	}

}