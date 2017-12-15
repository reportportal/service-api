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

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Pavel Bortnik
 */
@RunWith(MockitoJUnitRunner.class)
public class TestItemsHistoryServiceTest {

	private static final Date now = Date.from(Instant.ofEpochMilli(1502165103000L));
	private static final String PROJECT = "PROJECT";

	@Mock
	private TestItemResourceAssembler itemResourceAssembler;

	@Mock
	private LaunchRepository launchRepository;

	@InjectMocks
	private TestItemsHistoryService historyService = new TestItemsHistoryService();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void loadLaunchesNull() {
		String launchId = "1";
		when(launchRepository.findNameNumberAndModeById(launchId)).thenReturn(null);
		List<Launch> launches = historyService.loadLaunches(1, launchId, PROJECT, true);
		verify(launchRepository, times(1)).findNameNumberAndModeById(launchId);
		Assert.assertTrue("List of launches should be empty", launches.isEmpty());
	}

	@Test
	public void loadLaunchesDebug() {
		Launch launch = launch();
		launch.setMode(Mode.DEBUG);
		when(launchRepository.findNameNumberAndModeById(launch.getId())).thenReturn(launch);
		List<Launch> launches = historyService.loadLaunches(1, launch.getId(), PROJECT, true);
		verify(launchRepository, times(1)).findNameNumberAndModeById(launch.getId());
		Assert.assertTrue(launches.size() == 1);
		Assert.assertEquals(launch, launches.get(0));
	}

	@Test
	public void loadLaunches() {
		Launch launch = launch();
		int count = 1;
		Sort start_time = new Sort(Sort.Direction.DESC, "number");
		Filter filter = HistoryUtils.getLaunchSelectionFilter(launch.getName(), PROJECT, "1", true);
		when(launchRepository.findNameNumberAndModeById(launch.getId())).thenReturn(launch);
		when(launchRepository.findIdsByFilter(eq(filter), eq(start_time), eq(count))).thenReturn(Collections.singletonList(launch));
		List<Launch> launches = historyService.loadLaunches(count, launch.getId(), PROJECT, true);
		Assert.assertTrue(launches.size() == 1);
		verify(launchRepository, times(1)).findNameNumberAndModeById(launch.getId());
		verify(launchRepository, times(1)).findIdsByFilter(eq(filter), eq(start_time), eq(count));
	}

	@Test
	public void buildHistoryElement() {
		final int count = 3;
		Launch launch = launch();
		List<TestItem> testItems = fromItems(count);
		List<TestItemResource> resourceItems = resourceItems(count);
		for (int i = 0; i < count; i++) {
			when(itemResourceAssembler.toResource(testItems.get(i), launch.getStatus().name())).thenReturn(resourceItems.get(i));
		}
		TestItemHistoryElement actual = historyService.buildHistoryElement(launch(), fromItems(count));
		TestItemHistoryElement expected = historyElement(count);
		verify(itemResourceAssembler, times(count)).toResource(anyObject(), anyString());
		Assert.assertEquals(expected.getLaunchId(), actual.getLaunchId());
		Assert.assertEquals(expected.getLaunchNumber(), actual.getLaunchNumber());
		Assert.assertEquals(expected.getStartTime(), actual.getStartTime());
		Assert.assertEquals(expected.getResources().size(), actual.getResources().size());
	}

	@Test
	public void validateItems() {
		final int count = 3;
		List<TestItem> items = fromItems(count);
		List<String> ids = itemsIds(count);
		when(launchRepository.find(items.stream().map(TestItem::getLaunchRef).collect(toList()))).thenReturn(
				Collections.singletonList(launch()));
		historyService.validateItems(items, ids, PROJECT);
	}

	@Test
	public void validateEmpty() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unable to load test item history. Unable to find history for items");
		historyService.validateItems(Collections.emptyList(), itemsIds(2), PROJECT);
	}

	@Test
	public void validateDifferentProjects() {
		final int count = 3;
		TestItem item = anotherItem();
		List<TestItem> items = fromItems(count);
		List<String> ids = itemsIds(count);
		items.add(item);
		ids.add(item.getId());
		when(launchRepository.find(items.stream().map(TestItem::getLaunchRef).collect(toList()))).thenReturn(
				Lists.newArrayList(launch(), anotherLaunch()));
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unable to load test item history. Unable to find history for items");
		historyService.validateItems(items, ids, PROJECT);
	}

	@Test
	public void validateDifferentCounts() {
		List<TestItem> items = fromItems(2);
		List<String> ids = itemsIds(4);
		when(launchRepository.find(items.stream().map(TestItem::getLaunchRef).collect(toList()))).thenReturn(Lists.newArrayList(launch()));
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unable to load test item history. Unable to find history for items");
		historyService.validateItems(items, ids, PROJECT);
	}

	@Test
	public void validateNotSiblings() {
		List<TestItem> items = fromItems(3);
		List<String> ids = itemsIds(3);
		items.stream().forEach(it -> it.setParent(null));
		TestItem item = anotherItem();
		items.add(item);
		ids.add(item.getId());
		when(launchRepository.find(items.stream().map(TestItem::getLaunchRef).collect(toList()))).thenReturn(Lists.newArrayList(launch()));
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unable to load test item history. All test items should be siblings");
		historyService.validateItems(items, ids, PROJECT);
	}

	@Test
	public void validateNotSiblings2() {
		List<TestItem> items = fromItems(3);
		List<String> ids = itemsIds(3);
		items.get(0).setParent("fail");
		when(launchRepository.find(items.stream().map(TestItem::getLaunchRef).collect(toList()))).thenReturn(Lists.newArrayList(launch()));
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unable to load test item history. All test items should be siblings");
		historyService.validateItems(items, ids, PROJECT);
	}

	private Launch launch() {
		Launch launch = new Launch();
		launch.setId("1");
		launch.setNumber(1L);
		launch.setMode(Mode.DEFAULT);
		launch.setStatus(Status.PASSED);
		launch.setStartTime(now);
		launch.setProjectRef(PROJECT);
		return launch;
	}

	private Launch anotherLaunch() {
		Launch launch = new Launch();
		launch.setId("2");
		launch.setNumber(1L);
		launch.setMode(Mode.DEFAULT);
		launch.setStatus(Status.PASSED);
		launch.setStartTime(now);
		launch.setProjectRef("ANOTHER_PROJECT");
		return launch;
	}

	private TestItem anotherItem() {
		TestItem testItem = new TestItem();
		testItem.setId(String.valueOf(3));
		testItem.setLaunchRef("2");
		return testItem;
	}

	private List<TestItem> fromItems(int count) {
		List<TestItem> testItems = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TestItem testItem = new TestItem();
			testItem.setId(String.valueOf(i));
			testItem.setLaunchRef("1");
			testItem.setParent("parent");
			testItems.add(testItem);
		}
		return testItems;
	}

	private List<String> itemsIds(int count) {
		List<String> ids = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ids.add(String.valueOf(i));
		}
		return ids;
	}

	private TestItemHistoryElement historyElement(int count) {
		TestItemHistoryElement historyElement = new TestItemHistoryElement();
		historyElement.setLaunchId("1");
		historyElement.setLaunchNumber("1");
		historyElement.setLaunchStatus("PASSED");
		historyElement.setStartTime(String.valueOf(now.getTime()));
		historyElement.setResources(resourceItems(count));
		return historyElement;
	}

	private List<TestItemResource> resourceItems(int count) {
		List<TestItemResource> itemResources = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TestItemResource testItemResource = new TestItemResource();
			testItemResource.setItemId(String.valueOf(i));
			testItemResource.setLaunchId("1");
			testItemResource.setParent(i + "0");
			itemResources.add(testItemResource);
		}
		return itemResources;
	}

}