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

package com.epam.ta.reportportal.core.item.impl.history.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import com.epam.ta.reportportal.core.item.impl.history.provider.HistoryProvider;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_HASH;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_UNIQUE_ID;

/**
 * Required for retrieving {@link TestItemHistory} content using {@link Launch#getId()} as baseline for {@link TestItemHistory} selection.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchBaselineHistoryProvider implements HistoryProvider {

	private final LaunchRepository launchRepository;
	private final LaunchAccessValidator launchAccessValidator;
	private final TestItemRepository testItemRepository;

	public LaunchBaselineHistoryProvider(LaunchRepository launchRepository, LaunchAccessValidator launchAccessValidator,
			TestItemRepository testItemRepository) {
		this.launchRepository = launchRepository;
		this.launchAccessValidator = launchAccessValidator;
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Page<TestItemHistory> provide(Queryable filter, Pageable pageable, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, boolean usingHash) {
		return historyRequestParams.getLaunchId().map(launchId -> {
			Launch launch = launchRepository.findById(launchId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
			launchAccessValidator.validate(launch.getId(), projectDetails, user);

			return historyRequestParams.getHistoryType()
					.filter(HistoryRequestParams.HistoryTypeEnum.LINE::equals)
					.map(type -> getHistoryLine(filter,
							pageable,
							projectDetails.getProjectId(),
							launch.getName(),
							historyRequestParams.getHistoryDepth(),
							usingHash
					))
					.orElseGet(() -> getHistoryTable(filter,
							pageable,
							projectDetails.getProjectId(),
							historyRequestParams.getHistoryDepth(),
							usingHash
					));
		}).orElseGet(() -> Page.empty(pageable));
	}

	private Page<TestItemHistory> getHistoryLine(Queryable filter, Pageable pageable, Long projectId, String launchName, int historyDepth,
			boolean usingHash) {

		Page<String> historyBaseline = testItemRepository.loadHistoryBaseline(filter, pageable, projectId, launchName, usingHash);

		List<TestItemHistory> itemHistories = historyBaseline.getContent().stream().map(value -> {
			List<Long> itemIds = testItemRepository.loadHistoryItem(getHistoryFilter(filter, usingHash, value),
					pageable,
					projectId,
					launchName
			).map(itemId -> getHistoryIds(itemId, usingHash, projectId, launchName, historyDepth)).orElseGet(Collections::emptyList);
			return new TestItemHistory(value, itemIds);
		}).collect(Collectors.toList());

		return new PageImpl<>(itemHistories, pageable, historyBaseline.getTotalElements());
	}

	private Page<TestItemHistory> getHistoryTable(Queryable filter, Pageable pageable, Long projectId, int historyDepth,
			boolean usingHash) {
		Page<String> historyBaseline = testItemRepository.loadHistoryBaseline(filter, pageable, projectId, usingHash);

		List<TestItemHistory> itemHistories = historyBaseline.getContent().stream().map(value -> {
			List<Long> itemIds = testItemRepository.loadHistoryItem(getHistoryFilter(filter, usingHash, value), pageable, projectId)
					.map(itemId -> getHistoryIds(itemId, usingHash, projectId, historyDepth))
					.orElseGet(Collections::emptyList);
			return new TestItemHistory(value, itemIds);
		}).collect(Collectors.toList());

		return new PageImpl<>(itemHistories, pageable, historyBaseline.getTotalElements());
	}

	private Filter getHistoryFilter(Queryable filter, boolean usingHash, String historyValue) {
		List<ConvertibleCondition> commonConditions = filter.getFilterConditions();
		return new Filter(filter.getTarget().getClazz(), Lists.newArrayList()).withConditions(commonConditions)
				.withCondition(usingHash ?
						FilterCondition.builder().eq(CRITERIA_TEST_CASE_HASH, historyValue).build() :
						FilterCondition.builder().eq(CRITERIA_UNIQUE_ID, historyValue).build());
	}

	private List<Long> getHistoryIds(Long itemId, boolean usingHash, Long projectId, String launchName, int historyDepth) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		List<Long> historyIds = usingHash ?
				testItemRepository.loadHistory(testItem.getStartTime(),
						testItem.getItemId(),
						testItem.getTestCaseHash(),
						projectId,
						launchName,
						historyDepth
				) :
				testItemRepository.loadHistory(testItem.getStartTime(),
						testItem.getItemId(),
						testItem.getUniqueId(),
						projectId,
						launchName,
						historyDepth
				);
		historyIds.add(0, testItem.getItemId());
		return historyIds;
	}

	private List<Long> getHistoryIds(Long itemId, boolean usingHash, Long projectId, int historyDepth) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		List<Long> historyIds = usingHash ?
				testItemRepository.loadHistory(testItem.getStartTime(),
						testItem.getItemId(),
						testItem.getTestCaseHash(),
						projectId,
						historyDepth
				) :
				testItemRepository.loadHistory(testItem.getStartTime(),
						testItem.getItemId(),
						testItem.getUniqueId(),
						projectId,
						historyDepth
				);
		historyIds.add(0, testItem.getItemId());
		return historyIds;
	}

}
