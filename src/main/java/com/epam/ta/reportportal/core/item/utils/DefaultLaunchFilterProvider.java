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

package com.epam.ta.reportportal.core.item.utils;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_STATUS;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DefaultLaunchFilterProvider {

	public static Pair<Queryable, Pageable> createDefaultLaunchQueryablePair(ReportPortalUser.ProjectDetails projectDetails,
			UserFilter userFilter, int launchesLimit) {
		Queryable launchFilter = createLaunchFilter(projectDetails, userFilter);
		Pageable launchPageable = createLaunchPageable(userFilter, launchesLimit);
		return Pair.of(launchFilter, launchPageable);
	}

	private static Filter createLaunchFilter(ReportPortalUser.ProjectDetails projectDetails, UserFilter launchFilter) {

		validateLaunchFilterTarget(launchFilter);

		Filter filter = Filter.builder()
				.withTarget(launchFilter.getTargetClass().getClassObject())
				.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId())).build())
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.NOT_EQUALS)
						.withSearchCriteria(CRITERIA_LAUNCH_STATUS)
						.withValue(StatusEnum.IN_PROGRESS.name())
						.build())
				.withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_MODE, Mode.DEFAULT.toString()).build())
				.build();
		filter.getFilterConditions().addAll(launchFilter.getFilterCondition());
		return filter;
	}

	private static void validateLaunchFilterTarget(UserFilter launchFilter) {
		BusinessRule.expect(launchFilter, f -> ObjectType.Launch.equals(f.getTargetClass()))
				.verify(ErrorType.BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Incorrect filter target - '{}'. Allowed: '{}'",
								launchFilter.getTargetClass(),
								ObjectType.Launch
						)
				);
	}

	private static Pageable createLaunchPageable(UserFilter launchFilter, int launchesLimit) {

		BusinessRule.expect(launchesLimit, limit -> limit > 0)
				.verify(ErrorType.BAD_REQUEST_ERROR, "Launches limit should be greater than 0");

		Sort sort = ofNullable(launchFilter.getFilterSorts()).map(sorts -> Sort.by(sorts.stream()
				.map(s -> Sort.Order.by(s.getField()).with(s.getDirection()))
				.collect(toList()))).orElseGet(Sort::unsorted);
		return PageRequest.of(0, launchesLimit, sort);
	}
}
