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
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SpringFixture("logRepositoryTests")
public class LogRepositoryTest extends BaseTest {

	public static final String TEST_ITEM_ID = "44524cc1553de753b3e5ab2f";

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private LogRepository logRepository;

	@Test
	public void testGetNumberOfLogByTestItem() {
		TestItem testItem = new TestItem();
		testItem.setId(TEST_ITEM_ID);
		long count = logRepository.getNumberOfLogByTestItem(testItem);
		Assert.assertEquals(3, count);
	}

	@Test
	public void testFindByTestItemRef() {
		List<Log> result = logRepository.findByTestItemRef(TEST_ITEM_ID, 2, false);
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		Assert.assertTrue(result.get(0).getLogTime().before(result.get(1).getLogTime()));
		Assert.assertNull(result.get(0).getBinaryContent());
	}

	@Test
	public void testFindByTestItemRefNull() {
		List<Log> resultEmptyLogs = logRepository.findByTestItemRef(null, 2, false);
		Assert.assertNotNull(resultEmptyLogs);
		Assert.assertEquals(0, resultEmptyLogs.size());
		resultEmptyLogs = logRepository.findByTestItemRef("", 0, false);
		Assert.assertNotNull(resultEmptyLogs);
		Assert.assertEquals(0, resultEmptyLogs.size());
	}

}