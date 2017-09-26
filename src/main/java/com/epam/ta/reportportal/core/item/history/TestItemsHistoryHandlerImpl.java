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

package com.epam.ta.reportportal.core.item.history;

import com.epam.ta.reportportal.commons.DbUtils;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.model.ErrorType.TEST_ITEM_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.*;

/**
 * Default implementation of {@link TestItemsHistoryHandler}.
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service("testItemsHistoryHandler")
public class TestItemsHistoryHandlerImpl implements TestItemsHistoryHandler {

	private TestItemRepository testItemRepository;

	private ProjectRepository projectRepository;

	private TestItemResourceAssembler itemResourceAssembler;

	private ITestItemsHistoryService historyServiceStrategy;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Autowired
	public void setItemBuilder(TestItemResourceAssembler itemResourceAssembler) {
		this.itemResourceAssembler = itemResourceAssembler;
	}

	@Autowired
	public void setTestItemHistoryService(ITestItemsHistoryService testItemHistoryService) {
		this.historyServiceStrategy = testItemHistoryService;
	}

	@Override
	public List<TestItemHistoryElement> getItemsHistory(String projectName, String[] startPointsIds, int historyDepth,
			boolean showBrokenLaunches) {

		Project project = projectRepository.findOne(projectName);
		BusinessRule.expect(project, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);

		Predicate<Integer> greaterThan = t -> t > MIN_HISTORY_DEPTH_BOUND;
		Predicate<Integer> lessThan = t -> t < MAX_HISTORY_DEPTH_BOUND;
		String historyDepthMessage = "Items history depth should be greater than '" + MIN_HISTORY_DEPTH_BOUND + "' and lower than '"
				+ MAX_HISTORY_DEPTH_BOUND + "'";
		BusinessRule.expect(historyDepth, greaterThan.and(lessThan)).verify(UNABLE_LOAD_TEST_ITEM_HISTORY, historyDepthMessage);

		BusinessRule.expect(startPointsIds.length, t -> t < MAX_HISTORY_SIZE_BOUND).verify(UNABLE_LOAD_TEST_ITEM_HISTORY,
				"History size should be less than '" + MAX_HISTORY_SIZE_BOUND + "' test items.");

		//test items start point ids
		List<String> listIds = Lists.newArrayList(startPointsIds);

		List<TestItem> itemsForHistory = historyServiceStrategy.loadItems(listIds);
		historyServiceStrategy.validateItems(itemsForHistory, listIds, projectName);

		List<Launch> launches = historyServiceStrategy.loadLaunches(historyDepth, itemsForHistory.get(0).getLaunchRef(), projectName,
				showBrokenLaunches);
		List<String> historyLaunchesIds = launches.stream().map(Launch::getId).collect(Collectors.toList());

		List<TestItem> history = testItemRepository.loadItemsHistory(itemsForHistory, historyLaunchesIds,
				loadParentIds(itemsForHistory.get(0), historyLaunchesIds));

		Map<String, List<TestItem>> groupedItems = history.stream().collect(Collectors.groupingBy(TestItem::getLaunchRef));
		return launches.stream().map(launch -> buildHistoryElement(launch, groupedItems.get(launch.getId()))).collect(Collectors.toList());
	}

	TestItemHistoryElement buildHistoryElement(Launch launch, List<TestItem> testItems) {
		List<TestItemResource> resources = new ArrayList<>();
		if (testItems != null) {
			resources = testItems.stream().map(item ->
					itemResourceAssembler.toResource(item, launch.getStatus().name()))
					.collect(Collectors.toList());
		}
		TestItemHistoryElement testItemHistoryElement = new TestItemHistoryElement();
		testItemHistoryElement.setLaunchId(launch.getId());
		testItemHistoryElement.setLaunchNumber(launch.getNumber().toString());
		testItemHistoryElement.setStartTime(String.valueOf(launch.getStartTime().getTime()));
		testItemHistoryElement.setResources(resources);
		testItemHistoryElement.setLaunchStatus(launch.getStatus().name());
		return testItemHistoryElement;
	}

	private List<String> loadParentIds(TestItem testItem, List<String> launchesIds) {
		// root item doesn't have parents
		if (testItem == null || testItem.getParent() == null) {
			return null;
		}
		TestItem parent = testItemRepository.findOne(testItem.getParent());
		BusinessRule.expect(parent, Predicates.notNull()).verify(TEST_ITEM_NOT_FOUND,
				"Unable to find parent for '" + testItem.getId() + "' with ID '" + testItem.getParent() + "'.");
		List<TestItem> history = testItemRepository.loadItemsHistory(Lists.newArrayList(parent), launchesIds, null);
		return DbUtils.toIds(history);
	}
}
