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

package com.epam.ta.reportportal.database.triggers;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

/**
 * Tests to check audition configured fine
 *
 * @author Andrei Varabyeu
 */
public class AuditingEnabledTest extends BaseTest {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Test
	public void saveLaunchWithLastModified() {
		Launch launch = new Launch();
		launch.setDescription("some description");
		launch.setStartTime(Calendar.getInstance().getTime());
		launch.setName("some name");

		launchRepository.save(launch);
		Assert.assertNotNull("Last modified audition doesn't work for launches", launch.getLastModified());
	}

	@Test
	public void saveTestItemWithLastModified() {
		TestItem testItem = new TestItem();

		testItem.setStartTime(Calendar.getInstance().getTime());
		testItem.setName("some name");

		testItemRepository.save(testItem);
		Assert.assertNotNull("Last modified audition doesn't work for test items", testItem.getLastModified());
	}
}