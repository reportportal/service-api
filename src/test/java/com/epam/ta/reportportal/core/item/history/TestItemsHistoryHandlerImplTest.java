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

package com.epam.ta.reportportal.core.item.history;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Pavel Bortnik
 */
public class TestItemsHistoryHandlerImplTest {

	private static final String PROJECT = "project";

	@Mock
	private ITestItemsHistoryService historyService;

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private TestItemsHistoryHandler historyHandler = new TestItemsHistoryHandlerImpl();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getTestItemHistory() {
		int count = 5;
		int depth = 2;
		String[] startPointIds = startPointIds(count);
		List<TestItem> startTestItems = testItems("1", count);
		List<Launch> historyLaunches = historyLaunches(depth);

		List<TestItem> historyItems = Lists.newArrayList(testItems("2", count));

		doNothing().when(historyService).validateHistoryRequest(PROJECT, startPointIds, depth);
		when(testItemRepository.findByIds(eq(Lists.newArrayList(startPointIds)),
				eq(Lists.newArrayList("launchRef", "uniqueId"))
		)).thenReturn(startTestItems);
		doNothing().when(historyService).validateItems(startTestItems, Lists.newArrayList(startPointIds), PROJECT);
		when(historyService.loadLaunches(depth, startTestItems.get(0).getLaunchRef(), PROJECT, true)).thenReturn(historyLaunches);

		List<String> collect1 = startTestItems.stream().map(TestItem::getUniqueId).collect(toList());
		List<String> collect2 = historyLaunches.stream().map(Launch::getId).collect(toList());
		when(testItemRepository.loadItemsHistory(collect1, collect2)).thenReturn(history(depth, count));

		for (int i = 0; i < depth; i++) {
			List<TestItem> itms = testItems(String.valueOf(depth - i), count);
			when(historyService.buildHistoryElement(historyLaunches.get(i), itms)).thenReturn(historyElement(historyLaunches.get(i), itms));
		}

		List<TestItemHistoryElement> itemsHistory = historyHandler.getItemsHistory(PROJECT, startPointIds, depth, true);
		assertEquals("History size should be equals to depth", depth, itemsHistory.size());
		assertEquals(Integer.parseInt(itemsHistory.get(0).getLaunchNumber()), depth);
		itemsHistory.forEach(it -> assertEquals(count, it.getResources().size()));

		verify(historyService, times(1)).validateHistoryRequest(PROJECT, startPointIds, depth);
		verify(historyService, times(1)).validateItems(startTestItems, Lists.newArrayList(startPointIds), PROJECT);
		verify(historyService, times(2)).buildHistoryElement(anyObject(), anyListOf(TestItem.class));
		verify(testItemRepository, times(1)).findByIds(eq(Lists.newArrayList(startPointIds)),
				eq(Lists.newArrayList("launchRef", "uniqueId"))
		);
		verify(testItemRepository, times(1)).loadItemsHistory(collect1, collect2);

	}

	private List<TestItem> history(int depth, int count) {
		List<TestItem> res = new ArrayList<>(depth * count);
		for (int i = 0; i < depth; i++) {
			res.addAll(testItems(String.valueOf(depth - i), count));
		}
		return res;
	}

	private List<TestItem> testItems(String launchRefPrefix, int count) {
		List<TestItem> items = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TestItem testItem = new TestItem();
			testItem.setId(String.valueOf(i));
			testItem.setLaunchRef(launchRefPrefix);
			testItem.setUniqueId("unique" + "1" + i);
			items.add(testItem);
		}
		return items;
	}

	private TestItemHistoryElement historyElement(Launch launch, List<TestItem> history) {
		TestItemHistoryElement historyElement = new TestItemHistoryElement();
		historyElement.setLaunchId(launch.getId());
		historyElement.setLaunchNumber(String.valueOf(launch.getNumber()));
		historyElement.setResources(history.stream().map(TestItemConverter.TO_RESOURCE).collect(toList()));
		return historyElement;
	}

	private String[] startPointIds(int count) {
		String[] ids = new String[count];
		for (int i = 0; i < count; i++) {
			ids[i] = String.valueOf(i);
		}
		return ids;
	}

	private List<Launch> historyLaunches(int depth) {
		List<Launch> launches = new ArrayList<>(depth);
		for (int i = 0; i < depth; i++) {
			Launch launch = new Launch();
			launch.setId(String.valueOf(depth - i));
			launch.setProjectRef(PROJECT);
			launch.setNumber((long) depth - i);
			launches.add(launch);
		}
		return launches;
	}

}