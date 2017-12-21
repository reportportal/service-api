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
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@SpringFixture("testItemsHistroyTest")
public class TestItemHistoryTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	public static final String LAUNCH_1_ID = "51824cc1553de743b3e5aa2c";
	public static final String LAUNCH_2_ID = "5187cba4553d2fdd93969fcd";
	public static final String PARENT_ITEM_ID = "44524cc1553de743b3e5aa26";
	public static final String PARENT_ITEM_ID_2 = "44524cc1553de743b3e5aa30";

	@Autowired
	private TestItemRepository testItemRepository;

	private static List<String> launchesIds = Lists.newArrayList(LAUNCH_1_ID, LAUNCH_2_ID);

	@Test
	public void testloadHistory() {

		List<TestItem> children = testItemRepository.findAllDescendants(PARENT_ITEM_ID);
		List<String> uniqueIds = children.stream().map(TestItem::getUniqueId).collect(Collectors.toList());
		Assert.assertEquals(2, children.size());

		List<TestItem> history = testItemRepository.loadItemsHistory(uniqueIds, launchesIds);
		Assert.assertNotNull(history);
		Assert.assertEquals(5, history.size());
	}

	@Test
	public void testNullValues() {
		List<TestItem> history = testItemRepository.loadItemsHistory(null, null);
		Assert.assertNotNull(history);
		Assert.assertEquals(history.size(), 0);
	}
}