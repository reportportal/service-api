/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemHistoryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_HISTORY_DEPTH_BOUND;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_HISTORY_DEPTH_BOUND;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

/**
 * Creating items history based on {@link TestItem#uniqueId} field
 *
 * @author Pavel Bortnik
 */
@Service
public class TestItemsHistoryHandlerImpl implements TestItemsHistoryHandler {

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	private GetTestItemHandler getTestItemHandler;

	private TestItemResourceAssembler itemResourceAssembler;

	@Autowired
	public TestItemsHistoryHandlerImpl(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			GetTestItemHandler getTestItemHandler, TestItemResourceAssembler itemResourceAssembler) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.getTestItemHandler = getTestItemHandler;
		this.itemResourceAssembler = itemResourceAssembler;
	}

	@Override
	public List<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Queryable filter, Pageable pageable, @Nullable Long launchId, @Nullable Long filterId, int launchesLimit, int historyDepth) {

		validateHistoryDepth(historyDepth);
		Page<TestItem> testItemsPage = getTestItemHandler.getTestItemsPage(projectDetails, user, filter, pageable, launchId, filterId, launchesLimit);

		List<TestItem> itemsForHistory = new ArrayList<>(testItemsPage.getContent());
		validateItems(itemsForHistory, projectDetails.getProjectId());

		TestItem itemForHistory = itemsForHistory.get(0);
		Launch launch = launchRepository.findById(itemForHistory.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, itemForHistory.getLaunchId()));
		List<Launch> launchesHistory = launchRepository.findLaunchesHistory(historyDepth,
				launch.getId(),
				launch.getName(),
				projectDetails.getProjectId()
		);

		List<TestItem> itemsHistory = testItemRepository.loadItemsHistory(itemsForHistory.stream()
				.map(TestItem::getUniqueId)
				.collect(Collectors.toList()), launchesHistory.stream().map(Launch::getId).collect(toList()));

		Map<Long, List<TestItem>> groupedByLaunch = itemsHistory.stream().collect(Collectors.groupingBy(TestItem::getLaunchId));
		return launchesHistory.stream().map(l -> buildHistoryElement(l, groupedByLaunch.get(l.getId()))).collect(toList());
	}

	private void validateHistoryDepth(int historyDepth) {
		Predicate<Integer> greaterThan = t -> t > MIN_HISTORY_DEPTH_BOUND;
		Predicate<Integer> lessThan = t -> t < MAX_HISTORY_DEPTH_BOUND;
		String historyDepthMessage =
				"Items history depth should be greater than '" + MIN_HISTORY_DEPTH_BOUND + "' and lower than '" + MAX_HISTORY_DEPTH_BOUND
						+ "'";
		BusinessRule.expect(historyDepth, greaterThan.and(lessThan)).verify(UNABLE_LOAD_TEST_ITEM_HISTORY, historyDepthMessage);
	}

	private void validateItems(List<TestItem> itemsForHistory, Long projectId) {
		List<Long> testItemsIds = itemsForHistory.stream().map(TestItem::getItemId).collect(toList());

		BusinessRule.expect(itemsForHistory, Preconditions.NOT_EMPTY_COLLECTION)
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + testItemsIds + "'.");

		Set<Long> projectIds = launchRepository.findAllById(itemsForHistory.stream().map(TestItem::getLaunchId).collect(Collectors.toSet()))
				.stream()
				.map(Launch::getProjectId)
				.collect(Collectors.toSet());

		BusinessRule.expect((projectIds.size() == 1) && (projectIds.contains(projectId)), Predicates.equalTo(TRUE))
				.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "Unable to find history for items '" + testItemsIds + "'.");

		// check all items is siblings
		checkItemsIsSiblings(itemsForHistory);
	}

	private void checkItemsIsSiblings(List<TestItem> itemsForHistory) {
		/*
		 * If parent field is present check it - for example step, test if
		 * parent is empty check launch id - for example suite
		 */
		if (null == itemsForHistory.get(0).getParent()) {
			Long launchId = itemsForHistory.get(0).getLaunchId();
			itemsForHistory.forEach(it -> BusinessRule.expect(it.getLaunchId(), launch -> Objects.equals(launch, launchId))
					.verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings."));
		} else {
			/* Validate that items do not contains different parents */
			itemsForHistory.forEach(it -> BusinessRule.expect(it.getParent().getItemId(),
					parent -> Objects.equals(parent, itemsForHistory.get(0).getParent().getItemId())
			).verify(UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings."));
		}
	}

	private TestItemHistoryElement buildHistoryElement(Launch launch, List<TestItem> testItems) {
		List<TestItemHistoryResource> resources = new ArrayList<>();

		if (testItems != null) {
			Map<Long, PathName> pathNamesMapping = testItemRepository.selectPathNames(testItems.stream().map(TestItem::getItemId).collect(toList()));
			resources = testItems.stream().map(item -> itemResourceAssembler.toHistoryResource(item, pathNamesMapping.get(item.getItemId()))).collect(Collectors.toList());
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
