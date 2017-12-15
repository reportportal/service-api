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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEBUG;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * History loading service for regular test items (is_root property was set to
 * false).
 *
 * @author Aliaksei_Makayed
 */
@Service
public class TestItemsHistoryService implements ITestItemsHistoryService {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TestItemResourceAssembler itemResourceAssembler;

	@Override
	public List<Launch> loadLaunches(int quantity, String startingLaunchId, String projectName, boolean showBrokenLaunches) {
		Launch startingLaunch = launchRepository.findNameNumberAndModeById(startingLaunchId);
		if (startingLaunch == null) {
			return Collections.emptyList();
		}
		if (startingLaunch.getMode() == DEBUG) {
			return Collections.singletonList(startingLaunch);
		}
		Filter filter = HistoryUtils.getLaunchSelectionFilter(startingLaunch.getName(), projectName, startingLaunch.getNumber().toString(),
				showBrokenLaunches
		);
		return launchRepository.findIdsByFilter(filter, new Sort(DESC, "number"), quantity);
	}

	@Override
	public TestItemHistoryElement buildHistoryElement(Launch launch, List<TestItem> testItems) {
		List<TestItemResource> resources = new ArrayList<>();
		if (testItems != null) {
			resources = testItems.stream()
					.map(item -> itemResourceAssembler.toResource(item, launch.getStatus().name()))
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

	@Override
	public void validateHistoryRequest(String projectName, String[] startPointsIds, int historyDepth) {
		Project project = projectRepository.findOne(projectName);
		BusinessRule.expect(project, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);

		Predicate<Integer> greaterThan = t -> t > MIN_HISTORY_DEPTH_BOUND;
		Predicate<Integer> lessThan = t -> t < MAX_HISTORY_DEPTH_BOUND;
		String historyDepthMessage =
				"Items history depth should be greater than '" + MIN_HISTORY_DEPTH_BOUND + "' and lower than '" + MAX_HISTORY_DEPTH_BOUND
						+ "'";
		BusinessRule.expect(historyDepth, greaterThan.and(lessThan)).verify(UNABLE_LOAD_TEST_ITEM_HISTORY, historyDepthMessage);

		BusinessRule.expect(startPointsIds.length, t -> t < MAX_HISTORY_SIZE_BOUND)
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "History size should be less than '" + MAX_HISTORY_SIZE_BOUND + "' test items.");
	}

	@Override
	public void validateItems(List<TestItem> itemsForHistory, List<String> ids, String projectName) {
		// check all items loaded
		BusinessRule.expect(itemsForHistory, Preconditions.NOT_EMPTY_COLLECTION)
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + ids + "'.");

		Set<String> projectIds = launchRepository.find(itemsForHistory.stream().map(TestItem::getLaunchRef).collect(toList()))
				.stream()
				.map(Launch::getProjectRef)
				.collect(toSet());

		BusinessRule.expect((projectIds.size() == 1) && (projectIds.contains(projectName)), Predicates.equalTo(TRUE))
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + ids + "'.");

		ids.removeAll(itemsForHistory.stream().map(TestItem::getId).collect(toList()));

		BusinessRule.expect(ids.isEmpty(), Predicates.equalTo(TRUE))
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + ids + "'.");

		// check all items is siblings
		checkItemsIsSiblings(itemsForHistory);
	}

	private void checkItemsIsSiblings(List<TestItem> itemsForHistory) {
		String parentId = itemsForHistory.get(0).getParent();
		/*
		 * If parent field is present check it - for example step, test if
		 * parent is empty check launch id - for example suite
		 */
		if (parentId == null) {
			String launchRef = itemsForHistory.get(0).getLaunchRef();
			for (TestItem testItem : itemsForHistory) {
				BusinessRule.expect(testItem, Preconditions.hasSameLaunch(launchRef))
						.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings.");
			}
		} else {
			/* Validate that items do not contains different parents */
			BusinessRule.expect(
					itemsForHistory, Predicates.not(Preconditions.contains(Predicates.not(Preconditions.hasSameParent(parentId)))))
					.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings.");
		}
	}
}
