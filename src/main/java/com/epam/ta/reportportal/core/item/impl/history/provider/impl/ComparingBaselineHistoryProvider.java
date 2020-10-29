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
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import com.epam.ta.reportportal.core.item.impl.history.provider.HistoryProvider;
import com.epam.ta.reportportal.core.item.utils.DefaultLaunchFilterProvider;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * * Required for retrieving {@link TestItemHistory} content using {@link Launch} IDs as baseline for {@link TestItemHistory} selection.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ComparingBaselineHistoryProvider implements HistoryProvider {

	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	public ComparingBaselineHistoryProvider(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			GetShareableEntityHandler<UserFilter> getShareableEntityHandler) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.getShareableEntityHandler = getShareableEntityHandler;
	}

	@Override
	public Page<TestItemHistory> provide(Queryable filter, Pageable pageable, HistoryRequestParams historyRequestParams,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, boolean usingHash) {

		return historyRequestParams.getFilterParams().map(filterParams -> {
			Pair<Queryable, Pageable> launchQueryablePair = DefaultLaunchFilterProvider.createDefaultLaunchQueryablePair(projectDetails,
					getShareableEntityHandler.getPermitted(filterParams.getFilterId(), projectDetails),
					filterParams.getLaunchesLimit()
			);

			List<Long> launchIds = launchRepository.findAllLatestByFilter(launchQueryablePair.getLeft(), launchQueryablePair.getRight())
					.getContent()
					.stream()
					.map(Launch::getId)
					.collect(toList());

			return testItemRepository.loadItemsHistoryPage(filter,
					pageable,
					projectDetails.getProjectId(),
					launchIds,
					historyRequestParams.getHistoryDepth(),
					usingHash
			);

		}).orElseGet(() -> Page.empty(pageable));
	}

}
