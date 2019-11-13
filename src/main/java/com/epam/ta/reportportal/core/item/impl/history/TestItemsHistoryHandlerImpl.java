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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.core.item.impl.AbstractGetTestItemHandler;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_HISTORY_DEPTH_BOUND;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_HISTORY_DEPTH_BOUND;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

/**
 * Creating items history based on {@link TestItem#uniqueId} field
 *
 * @author Pavel Bortnik
 */
@Service
public class TestItemsHistoryHandlerImpl extends AbstractGetTestItemHandler implements TestItemsHistoryHandler {

	private final TestItemRepository testItemRepository;
	private final TestItemResourceAssembler itemResourceAssembler;

	@Autowired
	public TestItemsHistoryHandlerImpl(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			TestItemResourceAssembler itemResourceAssembler, GetShareableEntityHandler<UserFilter> getShareableEntityHandler) {
		super(launchRepository, getShareableEntityHandler);
		this.testItemRepository = testItemRepository;
		this.itemResourceAssembler = itemResourceAssembler;
	}

	@Override
	public Iterable<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Queryable filter, Pageable pageable, @Nullable Long filterId, boolean isLatest, int launchesLimit, int historyDepth) {

		validateHistoryDepth(historyDepth);
		Optional<Long> filterIdOptional = ofNullable(filterId);

		filter.getFilterConditions()
				.add(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId())).build());
		Page<TestItemHistory> testItemHistoryPage = testItemRepository.loadItemsHistoryPage(filter,
				pageable,
				projectDetails.getProjectId(),
				historyDepth
		);

		//		Page<TestItemHistory> testItemPage = filterIdOptional.map(launchFilterId -> {
		//			validateProjectRole(projectDetails, user);
		//			return getItemsWithLaunchesFiltering(filter, pageable, projectDetails, launchFilterId, isLatest, launchesLimit);
		//		}).orElseGet(() -> {
		//			filter.getFilterConditions()
		//					.add(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId())).build());
		//			return testItemRepository.loadItemsHistoryPage(filter, pageable, projectDetails.getProjectId(), historyDepth);
		//		});

		return buildHistoryElements(testItemHistoryPage, pageable);

	}

	private void validateHistoryDepth(int historyDepth) {
		Predicate<Integer> greaterThan = t -> t > MIN_HISTORY_DEPTH_BOUND;
		Predicate<Integer> lessThan = t -> t < MAX_HISTORY_DEPTH_BOUND;
		String historyDepthMessage = Suppliers.formattedSupplier("Items history depth should be greater than '{}' and lower than '{}'",
				MIN_HISTORY_DEPTH_BOUND,
				MAX_HISTORY_DEPTH_BOUND
		).get();
		BusinessRule.expect(historyDepth, greaterThan.and(lessThan)).verify(UNABLE_LOAD_TEST_ITEM_HISTORY, historyDepthMessage);
	}

	private Page<TestItem> getItemsWithLaunchesFiltering(Queryable testItemFilter, Pageable testItemPageable,
			ReportPortalUser.ProjectDetails projectDetails, Long launchFilterId, boolean isLatest, int launchesLimit) {
		Pair<Queryable, Pageable> queryablePair = createQueryablePair(projectDetails, launchFilterId, launchesLimit);
		return testItemRepository.findByFilter(isLatest,
				queryablePair.getKey(),
				testItemFilter,
				queryablePair.getRight(),
				testItemPageable
		);
	}

	private Iterable<TestItemHistoryElement> buildHistoryElements(Page<TestItemHistory> testItemHistoryPage, Pageable pageable) {

		List<TestItem> testItems = testItemRepository.findAllById(testItemHistoryPage.getContent()
				.stream()
				.flatMap(history -> history.getItemIds().stream())
				.collect(toList()));

		Map<Long, PathName> pathNamesMapping = testItemRepository.selectPathNames(testItems.stream()
				.map(TestItem::getItemId)
				.collect(toList()));

		Map<Integer, Map<Long, TestItemResource>> itemsMapping = testItems.stream()
				.map(item -> itemResourceAssembler.toResource(item, pathNamesMapping.get(item.getItemId())))
				.collect(groupingBy(TestItemResource::getTestCaseId, toMap(TestItemResource::getItemId, res -> res)));

		List<TestItemHistoryElement> testItemHistoryElements = testItemHistoryPage.getContent()
				.stream()
				.map(history -> ofNullable(itemsMapping.get(history.getTestCaseId())).map(mapping -> {
					TestItemHistoryElement historyResource = new TestItemHistoryElement();
					historyResource.setTestCaseId(history.getTestCaseId());
					historyResource.setResources(ofNullable(history.getItemIds()).map(itemIds -> itemIds.stream()
							.map(mapping::get)
							.collect(toList())).orElseGet(Collections::emptyList));
					return historyResource;
				}))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());

		return PagedResourcesAssembler.<TestItemHistoryElement>pageConverter().apply(PageableExecutionUtils.getPage(testItemHistoryElements,
				pageable,
				testItemHistoryPage::getTotalElements
		));

	}
}
