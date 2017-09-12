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

package com.epam.ta.reportportal.core.launch.meta;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel Bortnik
 */
public class LaunchMetadataUtilTest {

	@Test
	public void addBuildNumberPositive() {
		Launch launch = new Launch();
		launch.setTags(ImmutableSet.<String>builder().add("build:1").build());
		LaunchMetadataUtil.addBuildNumber(launch);
		Assert.assertNotNull(launch.getMetadata());
		Assert.assertEquals(1L, launch.getMetadata().getBuild().longValue());
	}

	@Test(expected = ReportPortalException.class)
	public void addBuildNumberNegative() {
		Launch launch = new Launch();
		launch.setTags(ImmutableSet.<String>builder().add("build:1").add("build:2").build());
		LaunchMetadataUtil.addBuildNumber(launch);
	}

	@Test
	public void withoutTag() {
		Launch launch = new Launch();
		launch.setTags(ImmutableSet.<String>builder().add("build").build());
		LaunchMetadataUtil.addBuildNumber(launch);
		Assert.assertNull(launch.getMetadata());
	}

}