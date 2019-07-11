/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.loader.util;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class FilterUtils {

	private FilterUtils() {
		//static only
	}

	public static Filter buildLatestLaunchFilter(Long projectId, String launchName) {
		return Filter.builder()
				.withTarget(Launch.class)
				.withCondition(new FilterCondition(Condition.EQUALS, false, String.valueOf(projectId), CRITERIA_PROJECT_ID))
				.withCondition(new FilterCondition(Condition.EQUALS, false, launchName, CRITERIA_NAME))
				.build();
	}

	public static Filter buildLatestLaunchFilter(Filter filter, String launchName) {
		return buildLatestLaunchFilter(
				filter.getFilterConditions()
						.stream()
						.filter(condition -> CRITERIA_PROJECT_ID.equalsIgnoreCase(condition.getSearchCriteria()))
						.map(condition -> Long.parseLong(condition.getValue()))
						.findAny()
						.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_FILTER_PARAMETERS,
								"Project id should be specified."
						)),
				launchName
		);
	}
}
