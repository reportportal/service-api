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

package com.epam.ta.reportportal.core.item.impl.provider;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.utils.DefaultLaunchFilterProvider;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.project.ProjectRole.OPERATOR;
import static com.epam.ta.reportportal.ws.controller.TestItemController.IS_LATEST_LAUNCHES_REQUEST_PARAM;
import static com.epam.ta.reportportal.ws.controller.TestItemController.LAUNCHES_LIMIT_REQUEST_PARAM;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class FilterDataProviderImpl implements DataProviderHandler {

	private static final String FILTER_ID_PARAM = "filterId";

	@Autowired
	private GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	@Autowired
	private TestItemRepository testItemRepository;

	@Override
	public Set<Statistics> accumulateStatistics(Queryable filter, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Map<String, String> params) {
		validateProjectRole(projectDetails, user);
		Optional.ofNullable(params.get(FILTER_ID_PARAM))
				.map(ControllerUtils::safeParseLong)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Filter id must be provided for filter based items provider"
				));
		return testItemRepository.accumulateStatisticsByFilter(filter);
	}

	@Override
	public Page<TestItem> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, Map<String, String> params) {
		validateProjectRole(projectDetails, user);

		Long launchFilterId = Optional.ofNullable(params.get(FILTER_ID_PARAM))
				.map(ControllerUtils::safeParseLong)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Filter id must be provided for filter based items provider"
				));

		Integer launchesLimit = Optional.ofNullable(params.get(LAUNCHES_LIMIT_REQUEST_PARAM))
				.map(ControllerUtils::safeParseInt)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Launches limit must be provided for filter based items provider"
				));

		Boolean isLatest = Optional.ofNullable(params.get(IS_LATEST_LAUNCHES_REQUEST_PARAM)).map(Boolean::parseBoolean).orElse(false);

		Pair<Queryable, Pageable> queryablePair = DefaultLaunchFilterProvider.createDefaultLaunchQueryablePair(projectDetails,
				getShareableEntityHandler.getPermitted(launchFilterId, projectDetails),
				launchesLimit
		);

		return testItemRepository.findByFilter(isLatest, queryablePair.getKey(), filter, queryablePair.getValue(), pageable);
	}

	protected void validateProjectRole(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(projectDetails.getProjectRole() == OPERATOR, Predicate.isEqual(false)).verify(ACCESS_DENIED);
		}
	}
}
