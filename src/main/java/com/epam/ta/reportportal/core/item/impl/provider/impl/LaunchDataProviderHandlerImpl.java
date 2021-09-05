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

package com.epam.ta.reportportal.core.item.impl.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.*;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.impl.provider.DataProviderHandler;
import com.epam.ta.reportportal.dao.IssueTypeRepositoryCustom;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE_ID;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class LaunchDataProviderHandlerImpl implements DataProviderHandler {

	private static final String LAUNCH_ID_PARAM = "launchId";

	@Autowired
	private LaunchAccessValidator launchAccessValidator;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private IssueTypeRepositoryCustom issueTypeRepository;

	@Override
	public Page<TestItem> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, Map<String, String> params) {
		filter = updateFilter(filter, projectDetails, user, params);
		return testItemRepository.findByFilter(filter, pageable);
	}

	@Override
	public Set<Statistics> accumulateStatistics(Queryable filter, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Map<String, String> params) {
		filter = updateFilter(filter, projectDetails, user, params);
		return testItemRepository.accumulateStatisticsByFilter(filter);
	}

	private Queryable updateFilter(Queryable filter, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Map<String, String> params) {
		Long launchId = Optional.ofNullable(params.get(LAUNCH_ID_PARAM))
				.map(ControllerUtils::safeParseLong)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Launch id must be provided for launch based items provider"
				));
		launchAccessValidator.validate(launchId, projectDetails, user);
		Queryable launchBasedFilter = Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_ID, String.valueOf(launchId)).build())
				.build();

		// Added to fix performance issue.
		List<String> issueTypeLocators = filter.getFilterConditions().stream()
				.map(ConvertibleCondition::getAllConditions)
				.flatMap(List::stream)
				.filter(c -> CRITERIA_ISSUE_TYPE.equals(c.getSearchCriteria())
						&& !c.isNegative() && Condition.IN.equals(c.getCondition())
				).map(FilterCondition::getValue)
				.flatMap(c -> Stream.of(c.split(",")))
				.collect(Collectors.toList());

		String issueTypeIdsString = issueTypeRepository.getIssueTypeIdsByLocators(issueTypeLocators)
				.stream().map(String::valueOf).collect(Collectors.joining(","));

		FilterCondition oldIssueTypeCondition = new FilterCondition(Condition.IN, false, null, CRITERIA_ISSUE_TYPE);
		FilterCondition issueTypeIdCondition = new FilterCondition(Condition.IN, false, issueTypeIdsString, CRITERIA_ISSUE_TYPE_ID);
		filter.replaceSearchCriteria(oldIssueTypeCondition, issueTypeIdCondition);

		return new CompositeFilter(Operator.AND, filter, launchBasedFilter);
	}
}
