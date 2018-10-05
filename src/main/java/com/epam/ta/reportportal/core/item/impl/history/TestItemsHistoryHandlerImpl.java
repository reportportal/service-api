/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.item.history.ITestItemsHistoryService;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Creating items history based on {@link TestItem#uniqueId} field
 *
 * @author Pavel Bortnik
 */
@Service
public class TestItemsHistoryHandlerImpl implements TestItemsHistoryHandler {
//
//	private static final String LAUNCH_REF = "launchRef";
//	private static final String UNIQUE_ID = "uniqueId";
//
//	private TestItemRepository testItemRepository;
//
//	private ITestItemsHistoryService historyService;
//
//	@Autowired
//	public void setTestItemRepository(TestItemRepository testItemRepository) {
//		this.testItemRepository = testItemRepository;
//	}
//
//	@Autowired
//	public void setHistoryService(ITestItemsHistoryService historyService) {
//		this.historyService = historyService;
//	}

	@Override
	public List<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, Long[] startPointsIds, int historyDepth,
			boolean showBrokenLaunches) {
		//@formatter:off
//		historyService.validateHistoryRequest(projectName, startPointsIds, historyDepth);
//
//		List<String> itemsIds = Lists.newArrayList(startPointsIds);
//		List<TestItem> itemsForHistory = testItemRepository.findByIds(itemsIds, Lists.newArrayList(LAUNCH_REF, UNIQUE_ID));
//		historyService.validateItems(itemsForHistory, itemsIds, projectName);
//
//		List<Launch> historyLaunches = historyService.loadLaunches(
//				historyDepth,
//				itemsForHistory.get(0).getLaunchRef(),
//				projectName,
//				showBrokenLaunches
//		);
//
//		List<TestItem> historyItems = testItemRepository.loadItemsHistory(
//				itemsForHistory.stream().map(TestItem::getUniqueId).collect(toList()),
//				historyLaunches.stream().map(Launch::getId).collect(toList())
//		);
//
//		Map<String, List<TestItem>> groupedItems = historyItems.stream().collect(Collectors.groupingBy(TestItem::getLaunchRef));
//		return historyLaunches.stream()
//				.map(launch -> historyService.buildHistoryElement(launch, groupedItems.get(launch.getId())))
//				.collect(Collectors.toList());
		//@formatter:on
		throw new UnsupportedOperationException("No implementation");
	}
}
