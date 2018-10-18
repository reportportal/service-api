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
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_HISTORY_DEPTH_BOUND;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_HISTORY_DEPTH_BOUND;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Creating items history based on {@link TestItem#uniqueId} field
 *
 * @author Pavel Bortnik
 */
@Service
public class TestItemsHistoryHandlerImpl implements TestItemsHistoryHandler {

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	@Autowired
	public TestItemsHistoryHandlerImpl(TestItemRepository testItemRepository, LaunchRepository launchRepository) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
	}

	@Override
	public List<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, Long[] startPointsIds,
			int historyDepth, boolean showBrokenLaunches) {

		validateHistoryDepth(historyDepth);
		List<Long> itemIds = Lists.newArrayList(startPointsIds);
		List<TestItem> itemsForHistory = testItemRepository.findAllById(itemIds);
		validateItems(itemsForHistory, itemIds, projectDetails.getProjectId());
		List<Launch> launchesHistory = launchRepository.findLaunchesHistory(historyDepth,
				itemsForHistory.get(0).getLaunch().getId(),
				itemsForHistory.get(0).getLaunch().getName(),
				projectDetails.getProjectId()
		);

		List<TestItem> itemsHistory = testItemRepository.loadItemsHistory(itemsForHistory.stream()
						.map(TestItem::getUniqueId)
						.collect(Collectors.toList()),
				launchesHistory.stream().map(Launch::getId).collect(toList())
		);
		Map<Long, List<TestItem>> groupedByLaunch = itemsHistory.stream().collect(Collectors.groupingBy(it -> it.getLaunch().getId()));
		return launchesHistory.stream().map(l -> buildHistoryElement(l, groupedByLaunch.get(l.getId()))).collect(toList());
	}

	public void validateItems(List<TestItem> itemsForHistory, List<Long> ids, Long projectId) {
		BusinessRule.expect(itemsForHistory, Preconditions.NOT_EMPTY_COLLECTION)
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + ids + "'.");

		Set<Long> projectIds = itemsForHistory.stream().map(item -> item.getLaunch().getProjectId()).collect(toSet());
		BusinessRule.expect((projectIds.size() == 1) && (projectIds.contains(projectId)), Predicates.equalTo(TRUE))
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + ids + "'.");

		ids.removeAll(itemsForHistory.stream().map(TestItem::getItemId).collect(Collectors.toList()));
		BusinessRule.expect(ids.isEmpty(), Predicates.equalTo(TRUE))
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + ids + "'.");

		// check all items is siblings
		checkItemsIsSiblings(itemsForHistory);
	}

	private void checkItemsIsSiblings(List<TestItem> itemsForHistory) {
		/*
		 * If parent field is present check it - for example step, test if
		 * parent is empty check launch id - for example suite
		 */
		if (null == itemsForHistory.get(0).getParent()) {
			Long launchId = itemsForHistory.get(0).getLaunch().getId();
			itemsForHistory.forEach(it -> BusinessRule.expect(it.getLaunch().getId(), launch -> Objects.equals(launch, launchId))
					.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings."));
		} else {
			/* Validate that items do not contains different parents */
			itemsForHistory.forEach(it -> BusinessRule.expect(it.getParent().getItemId(),
					parent -> Objects.equals(parent, itemsForHistory.get(0).getParent().getItemId())
			)
					.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings."));
		}
	}

	private void validateHistoryDepth(int historyDepth) {
		Predicate<Integer> greaterThan = t -> t > MIN_HISTORY_DEPTH_BOUND;
		Predicate<Integer> lessThan = t -> t < MAX_HISTORY_DEPTH_BOUND;
		String historyDepthMessage =
				"Items history depth should be greater than '" + MIN_HISTORY_DEPTH_BOUND + "' and lower than '" + MAX_HISTORY_DEPTH_BOUND
						+ "'";
		BusinessRule.expect(historyDepth, greaterThan.and(lessThan)).verify(UNABLE_LOAD_TEST_ITEM_HISTORY, historyDepthMessage);
	}

	private TestItemHistoryElement buildHistoryElement(Launch launch, List<TestItem> testItems) {
		List<TestItemResource> resources = new ArrayList<>();
		if (testItems != null) {
			resources = testItems.stream().map(TestItemConverter.TO_RESOURCE).collect(Collectors.toList());
		}
		TestItemHistoryElement testItemHistoryElement = new TestItemHistoryElement();
		testItemHistoryElement.setLaunchId(launch.getId());
		testItemHistoryElement.setLaunchNumber(launch.getNumber().toString());
		testItemHistoryElement.setStartTime(launch.getStartTime().toString());
		testItemHistoryElement.setResources(resources);
		testItemHistoryElement.setLaunchStatus(launch.getStatus().name());
		return testItemHistoryElement;
	}
}
