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
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
@Service("uniqueIdBasedHistoryHandler")
public class UniqueIdBasedHistoryHandlerImpl implements TestItemsHistoryHandler {

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private ITestItemsHistoryService historyService;

	@Override
	public List<TestItemHistoryElement> getItemsHistory(String projectName, String[] startPointsIds, int historyDepth,
			boolean showBrokenLaunches) {
		//@formatter:off
		historyService.validateHistoryRequest(projectName, startPointsIds, historyDepth);

		List<String> itemsIds = Lists.newArrayList(startPointsIds);
		List<TestItem> itemsForHistory = testItemRepository.loadItemsForHistory(itemsIds);
		historyService.validateItems(itemsForHistory, itemsIds, projectName);
		List<Launch> launches = historyService.loadLaunches(
				historyDepth,
				itemsForHistory.get(0).getLaunchRef(),
				projectName,
				showBrokenLaunches
		);

		List<TestItem> historyItems = testItemRepository.loadHistoryItems(
				itemsForHistory.stream().map(TestItem::getUniqueId).collect(toList()),
				launches.stream().map(Launch::getId).collect(toList())
		);

		Map<String, List<TestItem>> groupedItems = historyItems.stream().collect(Collectors.groupingBy(TestItem::getLaunchRef));
		return launches.stream()
				.map(launch -> historyService.buildHistoryElement(launch, groupedItems.get(launch.getId())))
				.collect(Collectors.toList());
		//@formatter:on
	}
}
